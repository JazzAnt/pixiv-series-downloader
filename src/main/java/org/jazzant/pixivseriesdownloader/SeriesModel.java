package org.jazzant.pixivseriesdownloader;

import javafx.beans.property.*;

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
    private final StringProperty seriesLink = new SimpleStringProperty();
    private final IntegerProperty artistID = new SimpleIntegerProperty();
    private final IntegerProperty seriesID = new SimpleIntegerProperty();
    private final IntegerProperty latestChapterID = new SimpleIntegerProperty();

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

    public String getSeriesLink(){return seriesLink.get();}
    public void setSeriesLink(String seriesLink){this.seriesLink.set(seriesLink);}
    public StringProperty getSeriesLinkProperty(){return seriesLink;}

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
