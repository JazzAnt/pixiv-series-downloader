package org.jazzant.pixivseriesdownloader;

public class ParserSeriesDoesNotExistException extends ParserException{
    public ParserSeriesDoesNotExistException(String message) {
        super(message);
    }
}
