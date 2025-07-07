package org.jazzant.pixivseriesdownloader;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Chapter {
    private static final String ARTWORK_URL_REGEX = "www\\.pixiv\\.net/?e?n?/artworks/([0-9]+)$";
    private String title;
    private String uploadDate;
    private int pixivID;
    private int chapterNumber;
    private int pageAmount;
    private ArrayList<String> imageLinks;

    public boolean isValid(){
        if(title == null || title.isBlank() ||
                uploadDate == null || uploadDate.isBlank() ||
                pixivID < 1 ||
                chapterNumber < 1 ||
                pageAmount < 1 ||
                imageLinks == null || imageLinks.isEmpty()
        ) return false;
        return true;
    }

    public static boolean checkChapterURLFormat(String chapterURL){
        Pattern pattern = Pattern.compile(ARTWORK_URL_REGEX);
        Matcher matcher = pattern.matcher(chapterURL.trim());
        return matcher.find();
    }
    public boolean setPixivIDFromChapterURL(String chapterURL){
        Pattern pattern = Pattern.compile(ARTWORK_URL_REGEX);
        Matcher matcher = pattern.matcher(chapterURL.trim());
        if(matcher.find()){
            try {
                pixivID = Integer.parseInt(matcher.group(1));
                return true;
            } catch (NumberFormatException e) {
                return false;
            }
        }
        else return false;
    }
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(String uploadDate) {
        this.uploadDate = uploadDate;
    }

    public int getPixivID() {
        return pixivID;
    }

    public void setPixivID(int pixivID) {
        this.pixivID = pixivID;
    }

    public int getChapterNumber() {
        return chapterNumber;
    }

    public void setChapterNumber(int chapterNumber) {
        this.chapterNumber = chapterNumber;
    }

    public int getPageAmount() {
        return pageAmount;
    }

    public void setPageAmount(int pageAmount) {
        this.pageAmount = pageAmount;
    }

    public ArrayList<String> getImageURLs() {
        return imageLinks;
    }

    public void setImageLinks(ArrayList<String> imageLinks) {
        this.imageLinks = imageLinks;
    }
}
