package org.jazzant.pixivseriesdownloader.Exceptions;

public class SeriesAlreadyInDatabaseException extends RuntimeException {
    public SeriesAlreadyInDatabaseException(String seriesId) {
        super("Series " + seriesId + " is already in the database");
    }
    public SeriesAlreadyInDatabaseException(int seriesId) {
        super("Series " + seriesId + " is already in the database");
    }
}
