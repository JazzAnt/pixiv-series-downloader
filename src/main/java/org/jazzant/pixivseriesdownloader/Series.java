package org.jazzant.pixivseriesdownloader;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Series {
    private static final String PIXIV_URL = "https://www.pixiv.net";
    private static final String SERIES_URL_REGEX = "^(http://|https://)www\\.pixiv\\.net/user/(\\d+)/series/(\\d+)$";
    private String directoryGroup = "";
    private String directoryTitle = "";
    private String title = "";
    private String artist = "";
    private SeriesStatus status = SeriesStatus.ONGOING;
    private int artistID;
    private int seriesID;
    private int latestChapterID;

    public boolean isValid(){
        if(directoryTitle.isBlank() ||
                title.isBlank() ||
                artist.isBlank() ||
                artistID < 1 ||
                seriesID < 1
        ) return false;
        return true;
    }
    public static boolean checkSeriesURLFormat(String seriesURL){
        Pattern pattern = Pattern.compile(SERIES_URL_REGEX);
        Matcher matcher = pattern.matcher(seriesURL.trim());
        if(matcher.find()){
            try {
                Integer.parseInt(matcher.group(2));
                Integer.parseInt(matcher.group(3));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }
    public boolean setSeriesIDAndArtistIDsFromSeriesURL(String seriesURL){
        Pattern pattern = Pattern.compile(SERIES_URL_REGEX);
        Matcher matcher = pattern.matcher(seriesURL.trim());
        if(matcher.find()){
            try {
                artistID = Integer.parseInt(matcher.group(2));
                seriesID = Integer.parseInt(matcher.group(3));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        return false;
    }
    public String getSeriesURL(){
        return PIXIV_URL + "/user/" + artistID + "/series/" + seriesID;
    }
    public String getLatestChapterURL(){
        return  PIXIV_URL + "/artworks/" + latestChapterID;
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
