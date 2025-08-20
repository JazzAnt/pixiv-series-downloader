package org.jazzant.pixivseriesdownloader;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.text.Text;

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
                for(Series series : seriesList){
                    if(this.isCancelled()) break;
                    if(!parser.seriesExists(series.getSeriesURL())){
                        broker.updateRecordStatus(series.getSeriesID(), SeriesStatus.DELETED);
                        updateLog("(!) Detected that the series 「" + series.getTitle() + "」 is no longer available on Pixiv");
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
                            chapter = parser.parseChapter(parser.getNextChapterURL());
                            latestChapterId = chapter.getPixivID();
                            updateProgressInfo("Downloading 「" + series.getTitle() + "」 Chapter No." + chapter.getChapterNumber());

                            downloadChapter(series, chapter, ()->{
                                        updateLog("Downloaded 「" + series.getTitle() + "」Chapter" +chapter.getChapterNumber()+": "+chapter.getTitle());
                                    },
                                    ()->{
                                        updateLog("(!) Failed to download 「" + series.getTitle() + "」Chapter" +chapter.getChapterNumber()+": "+chapter.getTitle() + " " +
                                                "for unknown reasons");
                                    });
                            broker.updateRecordLatestChapterId(series.getSeriesID(), latestChapterId);
                        } catch (ParserBlockedArtworkException e) {
                            FutureTask<Integer> futureTask = new FutureTask<>(new SkipAlert(e.getMessage() + " What would you like to do?"));
                            Platform.runLater(futureTask);
                            int choice = futureTask.get();
                            if(choice == 0) continue;
                            if(choice == 1) break;
                            else this.cancel();
                        }
                    }
                    updateLog("Downloaded all available chapters from " + series.getTitle());
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

    private void downloadChapter(Series series, Chapter chapter, Runnable onDownloadSuccess, Runnable onDownloadFailed){
        Task<Boolean> task = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return downloader.downloadChapter(series, chapter);
            }
        };
        task.setOnSucceeded(event->{
            if(task.getValue()) onDownloadSuccess.run();
            else onDownloadFailed.run();

        });
        Thread thread = new Thread(task);
        thread.start();
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
        public SkipAlert(String message){
            this.message = message;
        }
        @Override
        public Integer call() throws Exception {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.getButtonTypes().clear();
            ButtonType skipChapterButton = new ButtonType("Skip This Chapter");
            ButtonType skipSeriesButton = new ButtonType("Skip This Series");
            ButtonType stopButton = new ButtonType("Stop Download");
            alert.getButtonTypes().add(skipChapterButton);
            alert.getButtonTypes().add(skipSeriesButton);
            alert.getButtonTypes().add(stopButton);
            Text text = new Text(message);
            text.setWrappingWidth(580);
            alert.getDialogPane().setContent(text);
            alert.getDialogPane().setPadding(new Insets(ALERT_PADDING));
            alert.setWidth(600);
            alert.showAndWait();
            if(alert.getResult().equals(skipChapterButton)) return 0;
            if(alert.getResult().equals(skipSeriesButton)) return 1;
            if(alert.getResult().equals(stopButton)) return 2;
            return -1;
        }
    }

    private void updateProgressInfo(String message){
        progressInfo.setText(message);
    }
    private void updateLog(String message){
        logListView.getItems().add(message);
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
