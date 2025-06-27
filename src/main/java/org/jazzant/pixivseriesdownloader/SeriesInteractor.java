package org.jazzant.pixivseriesdownloader;

public class SeriesInteractor {
    private SeriesModel model;

    public SeriesInteractor(SeriesModel model){
        this.model = model;
    }

    public void saveSeries(){
        System.out.println("Saving Series to Database: "
                + "\nDir Group: " + model.getDirectoryGroup()
                + "\nDir Title: " + model.getDirectoryTitle()
                + "\nTitle: " + model.getTitle()
                + "\nArtist: " + model.getArtist()
                + "\nStatus: " + model.getStatus()
                + "\nArtistID: " + model.getArtistId()
                + "\nSeriesID: " + model.getSeriesId()
                + "\nLatestChapterID: " + model.getLatestChapterId());
    }
}
