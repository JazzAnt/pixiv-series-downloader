package org.jazzant.pixivseriesdownloader.JavaFxAddSeries;

import javafx.beans.property.*;
import org.jazzant.pixivseriesdownloader.Parser.Series;
import org.jazzant.pixivseriesdownloader.Database.SeriesStatus;

public class SeriesModel {
    private final BooleanProperty okToSave = new SimpleBooleanProperty();
    //directory stuff
    private final StringProperty directoryGroup = new SimpleStringProperty();
    private final StringProperty directoryTitle = new SimpleStringProperty();
    //series details
    private final StringProperty title = new SimpleStringProperty();
    private final StringProperty artist = new SimpleStringProperty();
    private final IntegerProperty status = new SimpleIntegerProperty();
    //pixiv links
    private final IntegerProperty artistID = new SimpleIntegerProperty();
    private final IntegerProperty seriesID = new SimpleIntegerProperty();
    private final IntegerProperty latestChapterID = new SimpleIntegerProperty();

    public Series getSeries(){
        Series series = new Series();
        series.setDirectoryGroup(getDirectoryGroup());
        series.setDirectoryTitle(getDirectoryTitle());
        series.setTitle(getTitle());
        series.setArtist(getArtist());
        series.setStatus(SeriesStatus.getStatusFromCode(getStatus()));
        series.setArtistID(getArtistId());
        series.setSeriesID(getSeriesId());
        series.setLatestChapterID(getLatestChapterId());
        return series;
    }
    public void setPropertiesFromSeries(Series series){
        setDirectoryGroup(series.getDirectoryGroup());
        setDirectoryTitle(series.getDirectoryTitle());
        setTitle(series.getTitle());
        setArtist(series.getArtist());
        setStatus(series.getStatus().getCode());
        setArtistId(series.getArtistID());
        setSeriesId(series.getSeriesID());
        setLatestChapterId(series.getLatestChapterID());
    }

    public String getDirectoryGroup(){return directoryGroup.get();}
    public void setDirectoryGroup(String group){this.directoryGroup.set(group);}
    public StringProperty getDirectoryGroupProperty(){return directoryGroup;}

    public String getDirectoryTitle(){return directoryTitle.get();}
    public void setDirectoryTitle(String title){this.directoryTitle.set(title);}
    public StringProperty getDirectoryTitleProperty(){return directoryTitle;}

    public String getTitle(){return title.get();}
    public void setTitle(String title){this.title.set(title);}
    public StringProperty getTitleProperty(){return title;}

    public String getArtist(){return artist.get();}
    public void setArtist(String artist){this.artist.set(artist);}
    public StringProperty getArtistProperty(){return artist;}

    public int getStatus(){return status.get();}
    public void setStatus(int status){this.status.set(status);}
    public IntegerProperty getStatusProperty(){return status;}

    public int getArtistId(){return artistID.get();}
    public void setArtistId(int artistId){this.artistID.set(artistId);}
    public IntegerProperty getArtistIdProperty(){return artistID;}

    public int getSeriesId(){return seriesID.get();}
    public void setSeriesId(int seriesId){this.seriesID.set(seriesId);}
    public IntegerProperty getSeriesIdProperty(){return seriesID;}

    public int getLatestChapterId(){return latestChapterID.get();}
    public void setLatestChapterId(int latestChapterId){this.latestChapterID.set(latestChapterId);}
    public IntegerProperty getLatestChapterIdProperty(){return latestChapterID;}

    public boolean getOkToSave(){return okToSave.get();}
    public void setOkToSave(boolean bool){this.okToSave.set(bool);}
    public BooleanProperty getOkToSaveProperty(){return okToSave;}
}
