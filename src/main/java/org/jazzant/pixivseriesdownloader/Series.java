package org.jazzant.pixivseriesdownloader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Series {
    private static final String PIXIV_URL = "https://www.pixiv.net";
    private static final String SERIES_LINK_REGEX = "/user/(\\d+)/series/(\\d+)$";
    private String directoryGroup;
    private String directoryTitle;
    private String title;
    private String artist;
    private SeriesStatus status;
    private int artistID;
    private int seriesID;
    private int latestChapterID;

    public boolean isValid(){
        if(directoryGroup == null ||
                directoryTitle == null || directoryTitle.isBlank() ||
                title == null || title.isBlank() ||
                artist == null || artist.isBlank() ||
                status == null ||
                artistID < 1 ||
                seriesID < 1
        ) return false;
        return true;
    }
    public boolean checkSeriesLinkFormat(String seriesLink){
        return seriesLink.trim().matches(SERIES_LINK_REGEX);
    }
    public boolean setSeriesLink(String seriesLink){
        Pattern pattern = Pattern.compile(SERIES_LINK_REGEX);
        Matcher matcher = pattern.matcher(seriesLink.trim());
        if(matcher.find()){
            artistID = Integer.parseInt(matcher.group(1));
            seriesID = Integer.parseInt(matcher.group(2));
            return true;
        }
        else return false;
    }
    public String getSeriesLink(){
        return PIXIV_URL + "/user/" + artistID + "/series/" + seriesID;
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

    public SeriesStatus getStatus() {
        return status;
    }

    public void setStatus(SeriesStatus status) {
        this.status = status;
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
