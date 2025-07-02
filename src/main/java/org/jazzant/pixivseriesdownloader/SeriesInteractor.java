package org.jazzant.pixivseriesdownloader;

public class SeriesInteractor {
    private final SeriesModel model;
    private final SeriesBroker broker = new SeriesBroker();

    public SeriesInteractor(SeriesModel model){
        this.model = model;
    }

    public void saveSeries(){
        broker.createRecord(createSeriesFromModel());
    }

    public Series createSeriesFromModel(){
        Series series = new Series();
        series.setDirectoryGroup(model.getDirectoryGroup());
        series.setDirectoryTitle(model.getDirectoryTitle());
        series.setTitle(model.getTitle());
        series.setArtist(model.getArtist());
        series.setStatus(SeriesStatus.getStatusFromCode(model.getStatus()));
        series.setArtistID(model.getArtistId());
        series.setSeriesID(model.getSeriesId());
        series.setLatestChapterID(model.getLatestChapterId());
        return series;
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
        model.setStatus(series.getStatus().getCode());
        model.setArtistId(series.getArtistID());
        model.setSeriesId(series.getSeriesID());
        model.setLatestChapterId(series.getLatestChapterID());
    }
}
