package org.jazzant.pixivseriesdownloader;

public class SeriesDTO {
    private String directoryGroup;
    private String directoryTitle;
    private String title;
    private String artist;
    private int status;
    private int artistID;
    private int seriesID;
    private int latestChapterID;

    public SeriesDTO(){}


    public SeriesDTO(String directoryGroup, String directoryTitle, String title, String artist, int status, int artistID, int seriesID, int latestChapterID) {
        this.directoryGroup = directoryGroup;
        this.directoryTitle = directoryTitle;
        this.title = title;
        this.artist = artist;
        this.status = status;
        this.artistID = artistID;
        this.seriesID = seriesID;
        this.latestChapterID = latestChapterID;
    }

    public String getDirectoryGroup() {
        return directoryGroup;
    }

    public void setDirectoryGroup(String directoryGroup) {
        this.directoryGroup = directoryGroup;
    }

    public String getDirectoryTitle() {
        return directoryTitle;
    }

    public void setDirectoryTitle(String directoryTitle) {
        this.directoryTitle = directoryTitle;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public int getStatus(){
        return status;
    }

    public SeriesStatus getStatusEnum(){
        return SeriesStatus.getStatusFromCode(status);
    }

    public void setStatus(SeriesStatus status) {
        this.status = status.getCode();
    }

    public int getArtistID() {
        return artistID;
    }

    public void setArtistID(int artistID) {
        this.artistID = artistID;
    }

    public int getSeriesID() {
        return seriesID;
    }

    public void setSeriesID(int seriesID) {
        this.seriesID = seriesID;
    }

    public int getLatestChapterID() {
        return latestChapterID;
    }

    public void setLatestChapterID(int latestChapterID) {
        this.latestChapterID = latestChapterID;
    }
}
