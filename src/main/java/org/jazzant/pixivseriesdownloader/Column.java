package org.jazzant.pixivseriesdownloader;

public enum Column {
    SERIES_ID("SeriesID"),
    ARTIST_ID("ArtistID"),
    GROUP_DIRECTORY("GroupDirectory"),
    TITLE_DIRECTORY("TitleDirectory"),
    TITLE("Title"),
    ARTIST("Artist"),
    STATUS("Status"),
    LATEST_CHAPTER_ID("LatestChapterID");

    private final String columnName;
    Column(String columnName){this.columnName = columnName;}
    public String getColumnName(){return columnName;}

    @Override
    public String toString() {
        return columnName;
    }
}
