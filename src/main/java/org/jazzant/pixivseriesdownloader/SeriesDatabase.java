package org.jazzant.pixivseriesdownloader;

import org.jazzant.pixivseriesdownloader.Exceptions.SeriesAlreadyInDatabaseException;

import java.sql.*;

public class SeriesDatabase {
    private final String databaseUrl = "jdbc:sqlite:series.db";

    public boolean testConnection(){
        try(Connection connection = DriverManager.getConnection(databaseUrl)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public void checkCollation(){
        String sql = "SELECT * FROM fn_helpcollations() WHERE name LIKE N'Japanese%';";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)) {

            while(resultSet.next()){
                System.out.println(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }

    }

    public void createTable(){
        String sql = "CREATE TABLE IF NOT EXISTS series ("
                + " SeriesID INTEGER PRIMARY KEY,"
                + " DirectoryGroup NVARCHAR(100) NOT NULL,"
                + " DirectoryTitle NVARCHAR(100) NOT NULL,"
                + " Title NVARCHAR(100) NOT NULL,"
                + " Artist NVARCHAR(100) NOT NULL,"
                + " Status INTEGER NOT NULL,"
                + " ArtistID INTEGER NOT NULL,"
                + " LatestChapterID INTEGER NOT NULL"
                + ");";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            Statement statement = connection.createStatement()) {
            statement.execute(sql);
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public int createRecord(String DirectoryGroup, String DirectoryTitle, String Title, String Artist,
                             int Status, int ArtistId, int SeriesId, int LatestChapterId){
        if(seriesExists(SeriesId)) throw new SeriesAlreadyInDatabaseException(SeriesId);
        String sql = "INSERT INTO series(DirectoryGroup, DirectoryTitle, Title, Artist, Status, ArtistID, SeriesID, LatestChapterID) " +
                "VALUES(?,?,?,?,?,?,?,?);";

        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            preparedStatement.setString(1, DirectoryGroup);
            preparedStatement.setString(2, DirectoryTitle);
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

    public boolean seriesExists(int SeriesId){
        boolean result = false;
        String sql = "SELECT CASE WHEN EXISTS (SELECT * FROM series WHERE SeriesID = ?) " +
                "THEN 1 ELSE 0 END";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            PreparedStatement preparedStatement = connection.prepareStatement(sql)){
            preparedStatement.setInt(1, SeriesId);
            ResultSet resultSet = preparedStatement.executeQuery();
            result = resultSet.getBoolean(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }
}
