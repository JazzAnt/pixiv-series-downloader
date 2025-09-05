package org.jazzant.pixivseriesdownloader.JavaFxDownload;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.text.Text;
import org.jazzant.pixivseriesdownloader.Database.SeriesBroker;
import org.jazzant.pixivseriesdownloader.Database.SeriesStatus;
import org.jazzant.pixivseriesdownloader.Downloader.Downloader;
import org.jazzant.pixivseriesdownloader.Parser.Chapter;
import org.jazzant.pixivseriesdownloader.Parser.Parser;
import org.jazzant.pixivseriesdownloader.Parser.ParserBlockedArtworkException;
import org.jazzant.pixivseriesdownloader.Parser.Series;

import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

public class DownloadController {
    private Parser parser;
    private Downloader downloader;
    private SeriesBroker broker;
    private final double ALERT_WRAP_WIDTH = 340;
    private final double ALERT_WIDTH = 360;
    private final double ALERT_PADDING = 10;

    @FXML protected Text progressInfo;
    @FXML protected ProgressBar progressBar;
    @FXML protected Button downloadButton;
    @FXML protected Button redownloadButton;
    @FXML protected Button cancelButton;
    @FXML protected ListView<String> logListView;

    public void setParser(Parser parser) {
        this.parser = parser;
    }

    public void setDownloader(Downloader downloader) {
        this.downloader = downloader;
    }

    public void setBroker(SeriesBroker broker) {
        this.broker = broker;
    }

    @FXML
    protected void handleDownloadButton(){
        downloadAll(false);
    }
    @FXML
    protected void handleRedownloadButton(){
        if(confirmationAlert("This button redownloads all series from the first chapter. It should only be used if " +
                "you've changed the configurations or altered the files manually. Are you sure?"))
            downloadAll(true);
    }

    private Task<Void> createDownloadTask(ArrayList<Series> seriesList, boolean redownloadAll, Runnable onTaskCompleted){
        Task<Void> task = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                for(int i=0; i<seriesList.size(); i++){
                    updateProgress(i, seriesList.size());
                    Series series = seriesList.get(i);
                    if(this.isCancelled()) break;
                    if(!parser.seriesExists(series.getSeriesURL())){
                        broker.updateRecordStatus(series.getSeriesID(), SeriesStatus.DELETED);
                        updateLog("(!) Detected that the series " + describeSeries(series) + " is no longer available on Pixiv");
                        continue;
                    }
                    int latestChapterId = series.getLatestChapterID();
                    if(redownloadAll || latestChapterId==0)
                        parser.goToSeries(series.getSeriesURL());
                    else
                        parser.goToChapter(series.getLatestChapterURL());

                    while(!parser.isLatestChapter()){
                        if(this.isCancelled()) break;
                        Chapter chapter;
                        try {
                            String chapterURL = parser.getNextChapterURL();
                            updateLog("Parsing " + chapterURL);
                            chapter = parser.parseChapter(chapterURL);
                            latestChapterId = chapter.getPixivID();
                            updateLog("Downloading " + describeChapter(series,chapter));

                            boolean skipSeries = false;
                            while(true){
                                boolean downloadSuccess = downloader.downloadChapter(series,chapter);

                                if(downloadSuccess){
                                    updateLog("Downloaded " + describeChapter(series,chapter));
                                    broker.updateRecordLatestChapterId(series.getSeriesID(), latestChapterId);
                                    break;
                                }
                                else {
                                    updateLog("(!) Failed to download " + describeChapter(series,chapter) + " for unknown reasons");
                                    FutureTask<Integer> futureTask = new FutureTask<>(
                                            new SkipAlert("Failed to download " + describeChapter(series,chapter) + " for unknown reasons. What would you like to do?", true)
                                    );
                                    Platform.runLater(futureTask);
                                    int choice = futureTask.get();
                                    if(choice == 0){
                                        updateLog("Retrying download of " + describeChapter(series,chapter));
                                        continue;
                                    }
                                    else if(choice == 1) {
                                        updateLog("Skipping " + describeChapter(series,chapter));
                                        break;
                                    }
                                    else if(choice == 2) {
                                        updateLog("Skipping " + describeSeries(series) + " series");
                                        skipSeries=true;
                                        break;
                                    }
                                    else {
                                        updateLog("Cancelling download");
                                        this.cancel();
                                    }
                                }
                            }
                            if(skipSeries)break;

                        } catch (ParserBlockedArtworkException e) {
                            updateLog("Chapter is blocked :" + e.getMessage());
                            FutureTask<Integer> futureTask = new FutureTask<>(new SkipAlert(e.getMessage() + " What would you like to do?", false));
                            Platform.runLater(futureTask);
                            int choice = futureTask.get();
                            if(choice == 1) {
                                updateLog("Skipping this chapter");
                                continue;
                            }
                            else if(choice == 2) {
                                updateLog("Skipping " + describeSeries(series) + " series");
                                break;
                            }
                            else {
                                updateLog("Cancelling download");
                                this.cancel();
                            }
                        }
                    }
                    updateLog("Downloaded all available chapters from " + describeSeries(series));
                }
                return null;
            }
        };
        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(task.progressProperty());
        task.setOnSucceeded(event-> onTaskCompleted.run());
        task.setOnFailed(event->{throw new RuntimeException(task.getException());});
        task.setOnCancelled(event->onTaskCompleted.run());
        return task;
    }

    private void downloadAll(boolean redownloadAll){
        logListView.getItems().clear();
        ArrayList<Series> seriesList = broker.selectAllOngoing();
        if(seriesList.isEmpty()){
            Alert alert = new Alert(Alert.AlertType.ERROR);
            Text text = new Text("There is no series with the ONGOING status in your database.");
            text.setWrappingWidth(ALERT_WRAP_WIDTH);
            alert.getDialogPane().setContent(text);
            alert.setWidth(ALERT_WIDTH);
            alert.getDialogPane().setPadding(new Insets(ALERT_PADDING));
            alert.show();
            return;
        }
        toggleButtons(false);
        Task<Void> task = createDownloadTask(seriesList, redownloadAll, ()->toggleButtons(true));
        cancelButton.setOnAction(actionEvent ->{
            if(confirmationAlert("Are you sure you want to cancel the downloads?")) task.cancel();
        });
        new Thread(task).start();
    }

    private boolean confirmationAlert(String message){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        Text text = new Text(message);
        text.setWrappingWidth(ALERT_WRAP_WIDTH);
        alert.getDialogPane().setContent(text);
        alert.setWidth(ALERT_WIDTH);
        alert.getDialogPane().setPadding(new Insets(ALERT_PADDING));
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }
    private class SkipAlert implements Callable<Integer> {
        private final String message;
        private final boolean retryable;
        public SkipAlert(String message, boolean retryable){
            this.message = message;
            this.retryable = retryable;
        }
        @Override
        public Integer call() throws Exception {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.getButtonTypes().clear();
            ButtonType retryButton = new ButtonType("Retry Download");
            ButtonType skipChapterButton = new ButtonType("Skip Chapter");
            ButtonType skipSeriesButton = new ButtonType("Skip Series");
            ButtonType stopButton = new ButtonType("Stop Download");
            if(retryable) alert.getButtonTypes().add(retryButton);
            alert.getButtonTypes().add(skipChapterButton);
            alert.getButtonTypes().add(skipSeriesButton);
            alert.getButtonTypes().add(stopButton);
            Text text = new Text(message);
            text.setWrappingWidth(580);
            alert.getDialogPane().setContent(text);
            alert.getDialogPane().setPadding(new Insets(ALERT_PADDING));
            alert.setWidth(600);
            alert.showAndWait();
            if(alert.getResult().equals(retryButton)) return 0;
            if(alert.getResult().equals(skipChapterButton)) return 1;
            if(alert.getResult().equals(skipSeriesButton)) return 2;
            if(alert.getResult().equals(stopButton)) return 3;
            return -1;
        }
    }

    private String describeSeries(Series series){
        return "「" + series.getTitle() + "」";
    }

    private String describeChapter(Series series, Chapter chapter){
        return "「" + series.getTitle() + "」 Chapter " + chapter.getChapterNumber() + " : " + chapter.getTitle();
    }

    private void updateLog(String message){
        progressInfo.setText(message);
        logListView.getItems().addFirst(message);
        logListView.refresh();
    }
    private void toggleButtons(boolean downloadButtonEnabled){
        downloadButton.setVisible(downloadButtonEnabled);
        downloadButton.setManaged(downloadButtonEnabled);
        redownloadButton.setVisible(downloadButtonEnabled);
        redownloadButton.setManaged(downloadButtonEnabled);

        cancelButton.setVisible(!downloadButtonEnabled);
        cancelButton.setManaged(!downloadButtonEnabled);
    }
}
