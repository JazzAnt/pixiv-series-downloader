package org.jazzant.pixivseriesdownloader;

import org.jazzant.pixivseriesdownloader.Exceptions.SeriesAlreadyInDatabaseException;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;

public class SeriesDatabase {
    private String databaseUrl = "jdbc:sqlite:series.db";

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
        String sql = "CREATE TABLE IF NOT EXISTS Series ("
                + " SeriesID INTEGER PRIMARY KEY,"
                + " ArtistID INTEGER NOT NULL,"
                + " GroupDirectory NVARCHAR(100) NOT NULL,"
                + " TitleDirectory NVARCHAR(100) NOT NULL,"
                + " Title NVARCHAR(100) NOT NULL,"
                + " Artist NVARCHAR(100) NOT NULL,"
                + " Status INTEGER NOT NULL,"
                + " LatestChapterID INTEGER NOT NULL"
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
        if(seriesExists(SeriesId)) throw new SeriesAlreadyInDatabaseException(SeriesId);
        String sql = "INSERT INTO Series(GroupDirectory, TitleDirectory, Title, Artist, Status, ArtistID, SeriesID, LatestChapterID) " +
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

    public ArrayList<String> selectAllGroups(){
        ArrayList<String> groups = new ArrayList<>();
        String sql = "SELECT \"DirectoryGroup\" FROM Series";
        try(Connection connection = DriverManager.getConnection(databaseUrl);
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)){
            while(resultSet.next()){
                groups.add(resultSet.getString("DirectoryGroup"));
            }
        } catch (SQLException e){
            throw new RuntimeException(e);
        }
        return groups;
    }
}
