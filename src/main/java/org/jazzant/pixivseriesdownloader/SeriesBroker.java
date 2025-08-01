package org.jazzant.pixivseriesdownloader;

import java.util.ArrayList;

public class SeriesBroker {
    private final SeriesDAO dao = new SeriesDAO();

    public boolean createRecord(Series series){
        return dao.createRecord(createDTOFromSeries(series));
    }

    public ArrayList<String> selectAllGroups(){return dao.selectAllGroups();}

    public ArrayList<Series> selectAll(){
        ArrayList<Series> seriesList = new ArrayList<>();
        for(SeriesDTO seriesDTO : dao.selectAll()){
            seriesList.add(createSeriesFromDTO(seriesDTO));
        }
        return seriesList;
    }

    public boolean deleteRecord(int seriesId){return dao.deleteRecord(seriesId);}

    public boolean updateRecordStatus(int seriesId, SeriesStatus status){
        return dao.updateRecordStatus(seriesId, status.getCode());
    }

    private Series createSeriesFromDTO(SeriesDTO seriesDTO){
        Series series = new Series();
        series.setDirectoryGroup(seriesDTO.getDirectoryGroup());
        series.setDirectoryTitle(seriesDTO.getDirectoryTitle());
        series.setTitle(seriesDTO.getTitle());
        series.setArtist(seriesDTO.getArtist());
        series.setStatus(SeriesStatus.getStatusFromCode(seriesDTO.getStatus()));
        series.setArtistID(seriesDTO.getArtistID());
        series.setSeriesID(seriesDTO.getSeriesID());
        series.setLatestChapterID(seriesDTO.getLatestChapterID());
        return series;
    }
    private SeriesDTO createDTOFromSeries(Series series){
        return new SeriesDTO(
                series.getDirectoryGroup(),
                series.getDirectoryTitle(),
                series.getTitle(),
                series.getArtist(),
                series.getStatus().getCode(),
                series.getArtistID(),
                series.getSeriesID(),
                series.getLatestChapterID()
        );
    }
}
