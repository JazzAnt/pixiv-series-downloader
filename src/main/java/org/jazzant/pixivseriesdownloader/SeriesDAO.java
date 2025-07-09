package org.jazzant.pixivseriesdownloader;

import java.util.ArrayList;

public class SeriesDAO {

    private static SeriesDatabase database = new SeriesDatabase();

    public boolean createRecord(SeriesDTO seriesDTO){
        int result = database.createRecord(
                seriesDTO.getDirectoryGroup(),
                seriesDTO.getDirectoryTitle(),
                seriesDTO.getTitle(),
                seriesDTO.getArtist(),
                seriesDTO.getStatus(),
                seriesDTO.getArtistID(),
                seriesDTO.getSeriesID(),
                seriesDTO.getLatestChapterID()
        );
        return result > 0;
    }

    public ArrayList<SeriesDTO> selectAll(){
        return database.selectAll();
    }

    public ArrayList<String> selectAllGroups(){
        return database.selectAllGroups();
    }
}
