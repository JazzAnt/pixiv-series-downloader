package org.jazzant.pixivseriesdownloader;

import java.util.ArrayList;

public class SeriesBroker {
    private final SeriesDAO dao = new SeriesDAO();

    public boolean createRecord(Series series){
        return dao.createRecord(createDTOfromSeries(series));
    }

    public ArrayList<String> selectAllGroups(){return dao.selectAllGroups();}

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
