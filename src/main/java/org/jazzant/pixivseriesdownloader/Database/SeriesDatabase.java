package org.jazzant.pixivseriesdownloader.Database;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

public class SeriesDatabase {
    private final String TABLE_NAME = "Series";
    private String databaseUrl = "jdbc:sqlite:PixivSeriesDownloader.db";

    public SeriesDatabase(){
        this.createTable();
    }

    public SeriesDatabase(String databaseDirectory){
        if(directoryExists(databaseDirectory)){
            this.databaseUrl = "jdbc:sqlite:" + databaseDirectory + "series.db";
        }
        this.createTable();
    }

    private boolean directoryExists(String directory){
        Path path = Paths.get(directory);
        return Files.exists(path) && Files.isDirectory(path);
    }

    public void createTable(){
        String sql = "CREATE TABLE IF NOT EXISTS "+ TABLE_NAME + " ( "
                + Column.SERIES_ID + " INTEGER PRIMARY KEY, "
                + Column.ARTIST_ID + " INTEGER NOT NULL, "
                + Column.GROUP_DIRECTORY + " NVARCHAR(100) NOT NULL, "
                + Column.TITLE_DIRECTORY + " NVARCHAR(100) UNIQUE NOT NULL, "
                + Column.TITLE + " NVARCHAR(100) NOT NULL, "
                + Column.ARTIST + " NVARCHAR(100) NOT NULL, "
                + Column.STATUS + " INTEGER NOT NULL, "
                + Column.LATEST_CHAPTER_ID + " INTEGER NOT NULL "
                + ");";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public int createRecord(String GroupDirectory, String TitleDirectory, String Title, String Artist,
                             int Status, int ArtistId, int SeriesId, int LatestChapterId){
        String sql = "INSERT INTO " + TABLE_NAME + " (" +
                Column.GROUP_DIRECTORY + ", " +
                Column.TITLE_DIRECTORY + ", " +
                Column.TITLE + ", " +
                Column.ARTIST + ", " +
                Column.STATUS + ", " +
                Column.ARTIST_ID + ", " +
                Column.SERIES_ID + ", " +
                Column.LATEST_CHAPTER_ID +
                ") " +
                "VALUES(?,?,?,?,?,?,?,?);";

        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, GroupDirectory);
            preparedStatement.setString(2, TitleDirectory);
            preparedStatement.setString(3, Title);
            preparedStatement.setString(4, Artist);
            preparedStatement.setInt(5, Status);
            preparedStatement.setInt(6, ArtistId);
            preparedStatement.setInt(7, SeriesId);
            preparedStatement.setInt(8, LatestChapterId);
            return preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return -1;
        }
    }

    public boolean valueExists(int value, Column column){
        String sql = "SELECT CASE WHEN EXISTS " +
                "(SELECT * FROM " + TABLE_NAME + " WHERE " + column + " = ?) " +
                "THEN 1 ELSE 0 END";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, value);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean valueExists(String value, Column column){
        String sql = "SELECT CASE WHEN EXISTS " +
                "(SELECT * FROM " + TABLE_NAME + " WHERE " + column + " = ?) " +
                "THEN 1 ELSE 0 END";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, value);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean valueCombinationExists(String value1, Column column1, String value2, Column column2){
        String sql = "SELECT CASE WHEN EXISTS " +
                "(SELECT * FROM " + TABLE_NAME + " WHERE " + column1 + " = ? AND " + column2 + " =?)" +
                "THEN 1 ELSE 0 END";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, value1);
            preparedStatement.setString(2, value2);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean valueCombinationExists(int value1, Column column1, String value2, Column column2){
        String sql = "SELECT CASE WHEN EXISTS " +
                "(SELECT * FROM " + TABLE_NAME + " WHERE " + column1 + " = ? AND " + column2 + " =?)" +
                "THEN 1 ELSE 0 END";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, value1);
            preparedStatement.setString(2, value2);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean valueCombinationExists(String value1, Column column1, int value2, Column column2){
        String sql = "SELECT CASE WHEN EXISTS " +
                "(SELECT * FROM " + TABLE_NAME + " WHERE " + column1 + " = ? AND " + column2 + " =?)" +
                "THEN 1 ELSE 0 END";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setString(1, value1);
            preparedStatement.setInt(2, value2);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean valueCombinationExists(int value1, Column column1, int value2, Column column2){
        String sql = "SELECT CASE WHEN EXISTS " +
                "(SELECT * FROM " + TABLE_NAME + " WHERE " + column1 + " = ? AND " + column2 + " =?)" +
                "THEN 1 ELSE 0 END";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, value1);
            preparedStatement.setInt(2, value2);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public ArrayList<SeriesDTO> selectAll(){
        ArrayList<SeriesDTO> seriesList = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME;
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)){
            while(resultSet.next()){
                SeriesDTO seriesDTO = new SeriesDTO(
                       resultSet.getString(Column.GROUP_DIRECTORY.getColumnName()),
                       resultSet.getString(Column.TITLE_DIRECTORY.getColumnName()),
                       resultSet.getString(Column.TITLE.getColumnName()),
                       resultSet.getString(Column.ARTIST.getColumnName()),
                       resultSet.getInt(Column.STATUS.getColumnName()),
                       resultSet.getInt(Column.ARTIST_ID.getColumnName()),
                       resultSet.getInt(Column.SERIES_ID.getColumnName()),
                       resultSet.getInt(Column.LATEST_CHAPTER_ID.getColumnName())
                );
                seriesList.add(seriesDTO);
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
        return seriesList;
    }

    public ArrayList<SeriesDTO> selectAllWhere(int value, Column column){
        ArrayList<SeriesDTO> seriesList = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + column + " = ?";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1, value);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                SeriesDTO seriesDTO = new SeriesDTO(
                        resultSet.getString(Column.GROUP_DIRECTORY.getColumnName()),
                        resultSet.getString(Column.TITLE_DIRECTORY.getColumnName()),
                        resultSet.getString(Column.TITLE.getColumnName()),
                        resultSet.getString(Column.ARTIST.getColumnName()),
                        resultSet.getInt(Column.STATUS.getColumnName()),
                        resultSet.getInt(Column.ARTIST_ID.getColumnName()),
                        resultSet.getInt(Column.SERIES_ID.getColumnName()),
                        resultSet.getInt(Column.LATEST_CHAPTER_ID.getColumnName())
                );
                seriesList.add(seriesDTO);
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
        return seriesList;
    }

    public ArrayList<SeriesDTO> selectAllWhere(String value, Column column){
        ArrayList<SeriesDTO> seriesList = new ArrayList<>();
        String sql = "SELECT * FROM " + TABLE_NAME +
                " WHERE " + column + " = ?";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, value);
            ResultSet resultSet = statement.executeQuery();
            while(resultSet.next()){
                SeriesDTO seriesDTO = new SeriesDTO(
                        resultSet.getString(Column.GROUP_DIRECTORY.getColumnName()),
                        resultSet.getString(Column.TITLE_DIRECTORY.getColumnName()),
                        resultSet.getString(Column.TITLE.getColumnName()),
                        resultSet.getString(Column.ARTIST.getColumnName()),
                        resultSet.getInt(Column.STATUS.getColumnName()),
                        resultSet.getInt(Column.ARTIST_ID.getColumnName()),
                        resultSet.getInt(Column.SERIES_ID.getColumnName()),
                        resultSet.getInt(Column.LATEST_CHAPTER_ID.getColumnName())
                );
                seriesList.add(seriesDTO);
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
        return seriesList;
    }

    public ArrayList<String> selectAllValuesOfAColumn(Column column){
        ArrayList<String> groups = new ArrayList<>();
        String sql = "SELECT DISTINCT " + column + " FROM " + TABLE_NAME
                + " WHERE TRIM(" + column + ") != ''";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)){
            while(resultSet.next()){
                groups.add(resultSet.getString(Column.GROUP_DIRECTORY.getColumnName()));
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
        return groups;
    }

    public int deleteRecord(int seriesId){
        String sql = "DELETE " +
                "FROM " + TABLE_NAME + " " +
                "WHERE " + Column.SERIES_ID + "=?";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1, seriesId);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int updateRecord(int seriesId, Column column, int newValue){
        String sql = "UPDATE " + TABLE_NAME + " " +
                "SET " + column + "=?" + " " +
                "WHERE " + Column.SERIES_ID + "=?";

        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setInt(1, newValue);
            statement.setInt(2, seriesId);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public int updateRecord(int seriesId, Column column, String newValue){
        String sql = "UPDATE " + TABLE_NAME + " " +
                "SET " + column + "=?" + " " +
                "WHERE " + Column.SERIES_ID + "=?";

        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, newValue);
            statement.setInt(2, seriesId);
            return statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
