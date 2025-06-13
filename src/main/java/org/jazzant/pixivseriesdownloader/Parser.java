package org.jazzant.pixivseriesdownloader;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class Parser {
    private final String PIXIV_URL = "https://www.pixiv.net";
    private final WebDriver driver;
    //set by setters
    private int implicitWaitTime;
    private String seriesLink;
    private String chapterNameFormat;
    //set by getSeriesDetailsFromSeriesPage()
    private String seriesTitle;
    private String artist;
    //set by getCurrentChapterDetails()
    private String currentChapterLink;
    private String currentChapterTitle;
    private String currentChapterPixivID;
    private String currentChapterUploadDate;
    private int currentChapterNumber;
    private int currentChapterPageAmount;
    private ArrayList<String> currentChapterImageLinks;
    private boolean isLatestChapter;

    public Parser(){
        driver = new ChromeDriver();
        setImplicitWaitTime(15);
        chapterNameFormat = "";
    }

    /**
     * Modifies the amount of wait time the driver will wait for the JS elements to appear before failing (Default is 15 seconds)
     * @param implicitWaitTime the amount of wait time in seconds
     */
    public void setImplicitWaitTime(int implicitWaitTime){
        this.implicitWaitTime = implicitWaitTime;
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(this.implicitWaitTime));
    }

    /**
     * Goes to the series page.
     * @param seriesLink The series link, the one that's formatted (pixiv.net/user/___/series/___)
     */
    public void setSeriesFromLink(String seriesLink){
        this.seriesLink = seriesLink;
        driver.get(this.seriesLink);
        currentChapterLink = "seriesPage";
        isLatestChapter = false;
    }

    /**
     * Sets the formatting of the chapter
     * @param chapterNameFormat
     */
    public void setChapterNameFormat(String chapterNameFormat){
        this.chapterNameFormat = chapterNameFormat;
    }

    /**
     * Check if the current series exists, only use if the current page is the series page. Use to check if the artist
     * had deleted this particular series. Works by checking for the existence of the "page not found" element.
     * @return true if the series exists, false if it's not.
     */
    public boolean seriesExists(){
        return driver.findElements(By.className("sc-13hg6mj-1")).isEmpty();
    }

    /**
     * Get the series details from the series link, only use if the current page is the series page.
     */
    public void getSeriesDetailsFromSeriesPage(){
        seriesTitle = driver.findElement(By.className("sc-ad36b09f-1")).getText();
        artist = driver.findElement(By.className("sc-jXbUNg")).findElement(By.tagName("div")).getText();
    }

    /**
     * Goes to the next chapter, or the first chapter if the current page is the series page.
     */
    public void goToNextChapter(){
        if(currentChapterLink.equals("seriesPage")){
            currentChapterLink = PIXIV_URL + driver.findElement(By.className("sc-55fb7329-1")).getDomAttribute("href");
        }
        else {
            currentChapterLink = PIXIV_URL + driver.findElement(By.className("gtm-series-next-work-button-in-illust-detail")).getDomAttribute("href");
        }
        driver.get(currentChapterLink);
    }

    /**
     * Fetches the current chapter's title, chapter number, chapter pixiv ID, page amount, image links, and checks whether this is
     * the final chapter.
     */
    public void getCurrentChapterDetails(){
        //Temporary variables to store stuff, also clears the image link list to be filled
        String temp;
        String[] tempArray;
        List<WebElement> tempList;
        currentChapterImageLinks.clear();
        //Find chapter title
        currentChapterTitle = driver.findElement(By.className("sc-e892487e-3")).getText();
        //Find chapter pixiv ID
        tempArray = currentChapterLink.split("/");
        currentChapterPixivID = tempArray[tempArray.length-1];
        //Find chapter upload date
        currentChapterUploadDate = driver.findElement(By.tagName("time")).getDomAttribute("datetime").split("T")[0];
        //Find chapter number
        temp = driver.findElement(By.className("sc-e892487e-15")).getText();
        currentChapterNumber = temp.charAt(temp.length()-1) - '0';
        //Find chapter page count, the timeout wait is set to 0, as this also checks if the page
        //counter even exists, because it doesn't exist for single page chapters
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(0));
        tempList = driver.findElements(By.className("gtm-manga-viewer-open-preview"));
        if(!tempList.isEmpty()){
            temp = tempList.get(0).findElement(By.tagName("span")).getText();
            currentChapterPageAmount = temp.charAt(temp.length()-1) - '0';
        }
        else {
            currentChapterPageAmount = 1;
        }
        //Get the image link and split it in two
        temp = driver.findElement(By.className("sc-e1dc2ae6-1")).getDomAttribute("src");
        tempArray = temp.split("0_master1200");
        //Fill the image links list, using the above image link and inserting the page number in between each
        //because that's how pixiv links are formatted
        for(int page=0; page<currentChapterPageAmount; page++){
            temp = tempArray[0] + page + tempArray[1];
            currentChapterImageLinks.add(temp);
        }
        //checks if this is the latest chapter, then finally return the implicit wait to the default
        isLatestChapter = driver.findElements(By.className("gtm-series-next-work-button-in-illust-detail")).isEmpty();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(implicitWaitTime));
    }

    /**
     * Checks if the current chapter is the latest chapter. getCurrentChapterDetails does this too but this one won't
     * bother checking the other details. Use when trying to update an existing library to check if any new chapters have
     * arrived.
     * @return true if it's the latest chapter, and false if not.
     */
    public boolean isLatestChapter(){
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        isLatestChapter = driver.findElements(By.className("gtm-series-next-work-button-in-illust-detail")).isEmpty();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(15));
        return isLatestChapter;
    }

    /**
     * Generates the chapter name for the file download, by default formatted as something like (Chapter123-title) but
     * can be given a formatter to make a custom filename. The formatter must contain either the chapter title, number,
     * id, or upload date or else it will use the default format (because if it doesn't contain at least one of the
     * aforementioned parameters, it might end up using the same name for every chapter).
     * @return formatted filename
     */
    public String generateFileName(){
        if(chapterNameFormat.contains("{chapter_title}")
        || chapterNameFormat.contains("{chapter_number}")
        || chapterNameFormat.contains("{chapter_id}")
        || chapterNameFormat.contains("{upload_date}"))
            return chapterNameFormat
               .replace("{chapter_title}", currentChapterTitle)
               .replace("{chapter_number}", currentChapterNumber+"")
               .replace("{chapter_id}", currentChapterPixivID)
               .replace("{upload_date}", currentChapterUploadDate)
               .replace("{page_amount}", currentChapterPageAmount+"")
               .replace("{artist}", artist)
               .replace("{series_title}", seriesTitle);

        return "Chapter" + currentChapterNumber + "_" + currentChapterTitle;
    }
}
