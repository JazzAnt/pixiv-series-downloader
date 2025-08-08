package org.jazzant.pixivseriesdownloader;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.text.Text;

import java.util.ArrayList;

public class DownloadController {
    private Parser parser;
    private Downloader downloader;
    private SeriesBroker broker;
    private boolean cancelDownload;

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
    @FXML
    protected void handleCancelButton(){
        if(confirmationAlert("Are you sure you want to stop the download?"))
            cancelDownload = true;
    }

    private void downloadAll(boolean redownloadAll){
        toggleButtons(false);
        cancelDownload = false;
        logListView.getItems().clear();
        ArrayList<Series> seriesList = broker.selectAllOngoing();
        progressBar.setProgress(0);
        double increment = 1 /(double)seriesList.size();

        for(Series series : seriesList){
            if(cancelDownload) break;
            updateProgressInfo("Downloading 「" + series.getTitle() + "」");
            if(!parser.seriesExists(series.getSeriesURL())){
                broker.updateRecordStatus(series.getSeriesID(), SeriesStatus.DELETED);
                incrementProgressBar(increment);
                updateLog("(!) Detected that the series 「" + series.getTitle() + "」 is no longer available on Pixiv");
                continue;
            }

            int latestChapterId = series.getLatestChapterID();
            if(redownloadAll || latestChapterId==0)
                parser.goToSeries(series.getSeriesURL());
            else
                parser.goToChapter(series.getLatestChapterURL());

            while(!parser.isLatestChapter()){
                if(cancelDownload) break;
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
                } catch (ParserSensitiveArtworkException e) {
                    int choice = skipAlert("The current chapter is blocked due to containing sensitive artwork and you're " +
                            "either not logged in or your Pixiv account has disabled displaying sensitive artworks. What would you " +
                            "like to do?");
                    if(choice == 0) continue;
                    if(choice == 1) break;
                    else cancelDownload=true;
                } catch (ParserMutedArtworkException e) {
                    int choice = skipAlert("The current chapter is blocked due to containing tags that are blocked " +
                            "by your Pixiv account. What would you like to do?");
                    if(choice == 0) continue;
                    if(choice == 1) break;
                    else cancelDownload=true;
                }
            }
            broker.updateRecordLatestChapterId(series.getSeriesID(), latestChapterId);

            incrementProgressBar(increment);
            updateLog("Downloaded all available chapters from " + series.getTitle());
        }
        if(cancelDownload) {
            updateProgressInfo("Download cancelled");
            updateLog("Download cancelled");
        }
        else {
            updateProgressInfo("Downloaded all available chapters from all ongoing series");
            updateLog("Download Finished");
            progressBar.setProgress(1);
        }
        toggleButtons(true);
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
        alert.setContentText(message);
        alert.showAndWait();
        return alert.getResult() == ButtonType.OK;
    }
    private int skipAlert(String message){
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.getButtonTypes().clear();
        ButtonType skipChapterButton = new ButtonType("Skip Current Chapter");
        ButtonType skipSeriesButton = new ButtonType("Skip Current Series");
        ButtonType stopButton = new ButtonType("Stop Current Download");
        alert.getButtonTypes().add(skipChapterButton);
        alert.getButtonTypes().add(skipSeriesButton);
        alert.getButtonTypes().add(stopButton);
        alert.setContentText(message);
        alert.showAndWait();
        if(alert.getResult().equals(skipChapterButton)) return 0;
        if(alert.getResult().equals(skipSeriesButton)) return 1;
        if(alert.getResult().equals(stopButton)) return 2;
        return -1;
    }

    private void updateProgressInfo(String message){
        progressInfo.setText(message);
    }
    private void updateLog(String message){
        logListView.getItems().add(message);
    }
    private void incrementProgressBar(double increment){
        progressBar.setProgress(progressBar.getProgress() + increment);
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
