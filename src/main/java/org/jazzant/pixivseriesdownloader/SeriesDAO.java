package org.jazzant.pixivseriesdownloader;

public class SeriesDAO {

    private static SeriesDatabase database = new SeriesDatabase();

    public boolean createRecord(SeriesDTO seriesDTO){
        int result = database.createRecord(
                seriesDTO.getDirectoryGroup(),
                seriesDTO.getDirectoryTitle(),
                seriesDTO.getTitle(),
                seriesDTO.getArtist(),
                seriesDTO.getStatus(),
                seriesDTO.getSeriesID(),
                seriesDTO.getArtistID(),
                seriesDTO.getLatestChapterID()
        );
        return result > 0;
    }
}
