package org.jazzant.pixivseriesdownloader;

public class SeriesTreeItem {
    private final String directoryGroup;
    private Series series;

    public SeriesTreeItem(String groupName){
        this.directoryGroup = groupName;
    }
    public SeriesTreeItem(Series series){
        this.series = series;
        this.directoryGroup = series.getDirectoryGroup();
    }

    public Series getSeries() {
        return series;
    }

    public boolean isSeries(){
        if(series == null) return false;
        return true;
    }

    @Override
    public String toString() {
        if(series == null){
            return directoryGroup;
        }
        return series.getDirectoryTitle() + " (" + series.getStatus() + ")";
    }
}
