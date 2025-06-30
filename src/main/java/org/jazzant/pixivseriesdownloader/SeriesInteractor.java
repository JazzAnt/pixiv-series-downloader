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

    public void parseSeries() {
        Series series = new Series();

        Parser.setSeries(series);
        series.setSeriesLink(model.getSeriesLink());
        Parser.goToSeries();
        Parser.parseSeriesDetails();

        model.setDirectoryTitle(series.getTitle());
        model.setTitle(series.getTitle());
        model.setArtist(series.getArtist());
        model.setArtistId(series.getArtistID());
        model.setSeriesId(series.getSeriesID());
    }
}
