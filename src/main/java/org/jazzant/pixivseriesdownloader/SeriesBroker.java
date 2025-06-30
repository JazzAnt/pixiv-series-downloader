package org.jazzant.pixivseriesdownloader;

public class SeriesBroker {
    private final SeriesDAO dao = new SeriesDAO();

    public boolean createRecord(Series series){
        return dao.createRecord(createDTOfromSeries(series));
    }

    private SeriesDTO createDTOfromSeries(Series series){
        return new SeriesDTO(
                series.getDirectoryGroup(),
                series.getDirectoryTitle(),
                series.getTitle(),
                series.getArtist(),
                series.getStatus(),
                series.getArtistID(),
                series.getSeriesID(),
                series.getLatestChapterID()
        );
    }
}
