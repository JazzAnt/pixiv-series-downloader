package org.jazzant.pixivseriesdownloader;

import java.util.ArrayList;

public class SeriesDAO {
    private static final SeriesDatabase database = new SeriesDatabase();

    public boolean createRecord(SeriesDTO seriesDTO){
        if(valueExists(seriesDTO.getSeriesID(), Column.SERIES_ID)) throw new DAOException("This series already exists in the database");
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
            throw new DAOException("Series ID cannot be modified as it is the primary key");
        checkIfColumnAcceptsInt(column);
        return database.updateRecord(seriesId, column, newValue) > 0;
    }
    public boolean updateRecord(int seriesId, Column column, String newValue){
        if(column.equals(Column.SERIES_ID))
            throw new DAOException("Series ID cannot be modified as it is the primary key");
        checkIfColumnAcceptsString(column);
        return database.updateRecord(seriesId, column, newValue) > 0;
    }

    public boolean valueExists(int value, Column column){
        checkIfColumnAcceptsInt(column);
        return database.valueExists(value, column);
    }
    public boolean valueExists(String value, Column column){
        checkIfColumnAcceptsString(column);
        return database.valueExists(value, column);
    }

    public boolean valueCombinationExists(int value1, Column column1, int value2, Column column2){
        if(column1.equals(column2)) throw new DAOException("This method can only compare values between different columns!");
        checkIfColumnAcceptsInt(column1);
        checkIfColumnAcceptsInt(column2);
        return database.valueCombinationExists(value1, column1, value2, column2);
    }

    public boolean valueCombinationExists(String value1, Column column1, int value2, Column column2){
        if(column1.equals(column2)) throw new DAOException("This method can only compare values between different columns!");
        checkIfColumnAcceptsString(column1);
        checkIfColumnAcceptsInt(column2);
        return database.valueCombinationExists(value1, column1, value2, column2);
    }

    public boolean valueCombinationExists(int value1, Column column1, String value2, Column column2){
        if(column1.equals(column2)) throw new DAOException("This method can only compare values between different columns!");
        checkIfColumnAcceptsInt(column1);
        checkIfColumnAcceptsString(column2);
        return database.valueCombinationExists(value1, column1, value2, column2);
    }

    public boolean valueCombinationExists(String value1, Column column1, String value2, Column column2){
        if(column1.equals(column2)) throw new DAOException("This method can only compare values between different columns!");
        checkIfColumnAcceptsString(column1);
        checkIfColumnAcceptsString(column2);
        return database.valueCombinationExists(value1, column1, value2, column2);
    }

    public ArrayList<SeriesDTO> selectAll(){return database.selectAll();}

    public ArrayList<String> selectAllValuesOfAColumn(Column column){
        return database.selectAllValuesOfAColumn(column);
    }

    private void checkIfColumnAcceptsString(Column column){
        if (column.equals(Column.TITLE) || column.equals(Column.ARTIST) || column.equals(Column.GROUP_DIRECTORY) || column.equals(Column.TITLE_DIRECTORY)) return;
        else throw new DAOException("This Database Column doesn't accept String values");
    }
    private void checkIfColumnAcceptsInt(Column column){
        if (column.equals(Column.SERIES_ID) || column.equals(Column.ARTIST_ID) || column.equals(Column.STATUS) || column.equals(Column.LATEST_CHAPTER_ID)) return;
        else throw new DAOException("This Database Column doesn't accept integer values");

    }
}
