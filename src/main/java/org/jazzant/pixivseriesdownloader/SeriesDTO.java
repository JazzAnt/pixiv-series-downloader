package org.jazzant.pixivseriesdownloader;

public class SeriesDTO {
    private static final int STATUS_ONGOING = 0;
    private static final int STATUS_COMPLETED = 1;
    private static final int STATUS_HIATUS = 2;
    private static final int STATUS_PAUSED = 3;
    private static final int STATUS_DELETED = 4;

    private String directoryGroup;
    private String directoryTitle;
    private String title;
    private String artist;
    private int status;
    private int artistID;
    private int seriesID;
    private int latestChapterID;

    public SeriesDTO(){}

    public SeriesDTO(String directoryGroup, String directoryTitle, String title, String artist, SeriesStatus status, int artistID, int seriesID, int latestChapterID) {
        this.directoryGroup = directoryGroup;
        this.directoryTitle = directoryTitle;
        this.title = title;
        this.artist = artist;
        setStatus(status);
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

    public SeriesStatus getStatusEnum() throws Exception {
        switch (this.status){
            case STATUS_ONGOING -> {
                return SeriesStatus.ONGOING;
            }
            case STATUS_COMPLETED -> {
                return SeriesStatus.COMPLETED;
            }
            case STATUS_HIATUS -> {
                return SeriesStatus.HIATUS;
            }
            case STATUS_PAUSED -> {
                return SeriesStatus.PAUSED;
            }
            case STATUS_DELETED -> {
                return SeriesStatus.DELETED;
            }
            default -> {
                throw new Exception("The DTO's Status Variable is incompatible with any SeriesStatus Enum");
            }
        }
    }

    public void setStatus(SeriesStatus status) {
        switch (status){
            case ONGOING -> this.status=STATUS_ONGOING;
            case COMPLETED -> this.status=STATUS_COMPLETED;
            case HIATUS -> this.status=STATUS_HIATUS;
            case PAUSED -> this.status=STATUS_PAUSED;
            case DELETED -> this.status=STATUS_DELETED;
            default -> this.status=-1;
        }
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
