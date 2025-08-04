package org.jazzant.pixivseriesdownloader;

import java.util.ArrayList;

public class SeriesDAO {
    private static final SeriesDatabase database = new SeriesDatabase();

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

    public boolean deleteRecord(int seriesId){return database.deleteRecord(seriesId) > 0;}

    public boolean updateRecord(int seriesId, Column column, int newValue){
        if(column.equals(Column.SERIES_ID))
            throw new DatabaseException("Series ID cannot be modified as it is the primary key");
        if (column.equals(Column.TITLE) || column.equals(Column.ARTIST) || column.equals(Column.GROUP_DIRECTORY) || column.equals(Column.TITLE_DIRECTORY))
            throw new DatabaseException("This Database Column only accepts String values");
        return database.updateRecord(seriesId, column, newValue) > 0;
    }
    public boolean updateRecord(int seriesId, Column column, String newValue){
        if(column.equals(Column.SERIES_ID))
            throw new DatabaseException("Series ID cannot be modified as it is the primary key");
        if (column.equals(Column.ARTIST_ID) || column.equals(Column.STATUS) || column.equals(Column.LATEST_CHAPTER_ID))
            throw new DatabaseException("This Database Column only accepts integer values");
        return database.updateRecord(seriesId, column, newValue) > 0;
    }

    public ArrayList<SeriesDTO> selectAll(){return database.selectAll();}

    public ArrayList<String> selectAllValuesOfAColumn(Column column){
        return database.selectAllValuesOfAColumn(column);
    }
}
