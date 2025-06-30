package org.jazzant.pixivseriesdownloader;

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
                + " ID INTEGER PRIMARY KEY,"
                + " DirectoryGroup NVARCHAR(100) NOT NULL,"
                + " DirectoryTitle NVARCHAR(100) NOT NULL,"
                + " Title NVARCHAR(100) NOT NULL,"
                + " Artist NVARCHAR(100) NOT NULL,"
                + " Status INTEGER NOT NULL,"
                + " ArtistId INTEGER NOT NULL,"
                + " SeriesId INTEGER NOT NULL,"
                + " LatestChapterId INTEGER NOT NULL"
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
        String sql = "INSERT INTO series(DirectoryGroup, DirectoryTitle, Title, Artist, Status, ArtistId, SeriesId, LatestChapterId) " +
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
}
