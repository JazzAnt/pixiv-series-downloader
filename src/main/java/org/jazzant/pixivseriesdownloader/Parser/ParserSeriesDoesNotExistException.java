package org.jazzant.pixivseriesdownloader.Parser;

public class ParserSeriesDoesNotExistException extends ParserException{
    public ParserSeriesDoesNotExistException(String message) {
        super(message);
    }
}
