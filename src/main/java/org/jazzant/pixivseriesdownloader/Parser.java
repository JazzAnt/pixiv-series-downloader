package org.jazzant.pixivseriesdownloader;

import org.openqa.selenium.*;
import org.openqa.selenium.Point;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.awt.Toolkit;
import java.awt.Dimension;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Parser {
    private static final String PIXIV_URL = "https://www.pixiv.net";
    private static boolean initialized = false;
    private static WebDriver driver = null;
    private static WebDriver.Window window = null;
    private static WebDriverWait driverWait = null;
    private static WebDriverWait driverLongWait = null;
    private static boolean isLoggedIn;
    private static Point screenCenter;
    //set by setters
    private static Series series;
    private static Chapter chapter;
    private static String pixivUsername;
    private static String pixivPassword;
    private static int waitTime;

    private Parser(){}

    public static void initialize(){
        if (!initialized) {
            FirefoxOptions options = new FirefoxOptions();
            options.addArguments("--width=400");
            options.addArguments("--height=500");
            driver = new FirefoxDriver(options);
            window = driver.manage().window();
            driverWait = new WebDriverWait(driver, Duration.ofSeconds(10));
            driverLongWait = new WebDriverWait(driver, Duration.ofSeconds(99));
            setWaitTime(10);
            isLoggedIn = false;
            pixivUsername = "";
            pixivPassword = "";
            Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
            int x = screenSize.width / 2;
            int y = screenSize.height / 2;
            screenCenter = new Point(x, y);
            windowMinimize();
            initialized = true;
        }
    }
    public static void setSeries(Series seriesObject){series = seriesObject;}
    public static void setChapter(Chapter chapterObject){chapter = chapterObject;}
    public static void setPixivUsername(String username){pixivUsername = username;}
    public static void setPixivPassword(String password){pixivPassword = password;}
    /**
     * Modifies the amount of wait time the driver will wait for various operation attempts (such as waiting for
     * a webpage or web element to load) before giving up
     * @param driverWaitTime the amount of wait time in seconds (Default is 10 seconds)
     */
    public static void setWaitTime(int driverWaitTime){
        if(driverWaitTime<0) driverWaitTime = 0;
        waitTime = driverWaitTime;
        driverWait.withTimeout(Duration.ofSeconds(waitTime));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(waitTime));
    }
    public static boolean loginPixiv() {
        if(isLoggedIn) return true;
        goToLoginPage();
        if(enterUsernameField() && enterPasswordField()) {
            clickLoginButton();
            isLoggedIn = waitForLoginShort();
            if(isLoggedIn) return true;

            if(detectReCAPTCHA()){
                windowToFront();
                isLoggedIn = waitForLoginLong();
            }
        }
        else {
            windowToFront();
            isLoggedIn = waitForLoginLong();
        }
        return isLoggedIn;
    }
    public static void goToSeries(){
        driver.get(series.getSeriesLink());
    }
    /**
     * Check if the current series exists. Must be done in while the driver is in the series page.
     * @return true if the series exists, false if it's not.
     */
    public static boolean seriesExists(){
        return !driver.findElement(By.tagName("h1")).getText().equals("Page not found");
    }
    /**
     * Goes to the next chapter, or the first chapter if the current page is the series page.
     */
    public static void goToNextChapter(){
        String nextChapterLink;
        if(Objects.equals(driver.getCurrentUrl(), series.getSeriesLink())){
            nextChapterLink = PIXIV_URL + driver
                    .findElement(By.className("gtm-manga-series-first-story")).findElement(By.tagName("a"))
                    .getDomAttribute("href");
        }
        else {
            nextChapterLink = PIXIV_URL + driver
                    .findElement(By.className("gtm-series-next-work-button-in-illust-detail"))
                    .getDomAttribute("href");
        }
        driver.get(nextChapterLink);
    }
    /**
     * Checks if the parser is currently in the latest chapter.
     * @return true if it's the latest chapter, and false if not.
     */
    public static boolean isLatestChapter(){
        if(Objects.equals(driver.getCurrentUrl(), series.getSeriesLink())) return false;
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        boolean isLatestChapter = driver.findElements(By.className("gtm-series-next-work-button-in-illust-detail")).isEmpty();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(waitTime));
        return isLatestChapter;
    }
    public static void parseSeriesDetails(){
        parseSeriesTitle();
        parseSeriesArtist();
    }
    public static void parseChapterDetails(){
        parseChapterTitle();
        parseChapterID();
        parseChapterUploadDate();
        parseChapterNumber();
        parseChapterPageCount();
        parseImageLinks();
    }

    private static void windowMinimize(){
        window.minimize();
    }
    private static void windowToFront(){
        window.minimize();
        window.setPosition(screenCenter);
    }
    private static void goToLoginPage(){
        driver.get("https://accounts.pixiv.net/login");
    }
    private static boolean enterUsernameField(){
        if(pixivUsername.isBlank()) return false;
        driver.findElements(By.tagName("fieldset")).get(0)
                .findElement(By.tagName("input"))
                .sendKeys(pixivUsername);
        return true;
    }
    private static boolean enterPasswordField(){
        if(pixivPassword.isBlank()) return false;
        driver.findElements(By.tagName("fieldset")).get(1)
                .findElement(By.tagName("input"))
                .sendKeys(pixivPassword);
        return true;
    }
    private static void clickLoginButton(){
        driver.findElement(By.id("app-mount-point"))
                .findElements(By.tagName("button")).get(5)
                .click();
    }
    private static boolean waitForLoginShort(){
        try {
            driverWait.until(d -> driver.getCurrentUrl().contains("www.pixiv.net"));
            return true;
        }
        catch (TimeoutException _){
            return false;
        }
    }
    private static boolean waitForLoginLong(){
        try {
            driverLongWait.until(d -> driver.getCurrentUrl().contains("www.pixiv.net"));
            return true;
        }
        catch (TimeoutException _){
            return false;
        }
    }
    private static boolean detectReCAPTCHA(){
        List<WebElement> iframes = driver.findElements(By.tagName("iframe"));
        for(WebElement iframe : iframes){
            if(iframe.getDomAttribute("title") != null && iframe.getDomAttribute("title").equals("reCAPTCHA")){
                new Actions(driver).moveToElement(iframe).perform();
                return true;
            }
        }
        return false;
    }
    private static void parseSeriesTitle(){
        String title;
        if(Objects.equals(driver.getCurrentUrl(), series.getSeriesLink())){
            title = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[5]/div[1]/div/div[1]/main/div[last()]/section/div/div[1]/div[1]"))
                    .getText();
        }
        else {
            String[] tempArray;
            if(isLoggedIn)
                tempArray = driver
                        .findElement(By.xpath("/html/body/div[1]/div/div[2]/div[5]/div[1]/div/div[1]/main/section/div[1]/div/figcaption/div[2]/div/div[2]/a"))
                        .getText().split("#");
            else
                tempArray = driver
                        .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[1]/div/figcaption/div[2]/div/div[2]/a"))
                        .getText().split("#");
            tempArray[tempArray.length - 1] = "";
            title = String.join("", tempArray).trim();
        }
        series.setTitle(title);
    }
    private static void parseSeriesArtist(){
        String artist;
        if(Objects.equals(driver.getCurrentUrl(), series.getSeriesLink()))
            artist = driver
                    .findElement(By.xpath("//div[contains(@class, 'gtm-manga-series-profile')]/div/div/a/div"))
                    .getText();
        else if(isLoggedIn){
            artist = driver
                    .findElement(By.xpath("/html/body/div[1]/div/div[2]/div[5]/div[1]/div/div[1]/main/section/div[2]/div[1]/div/div[1]/h2/div/div/a/div"))
                    .getText();
        }
        else{
            artist = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[2]/div/div/div[1]/h2/div/div/a/div"))
                    .getText();
        }
        series.setArtist(artist);
    }
    private static void parseChapterTitle(){
        String chapterTitle = driver.findElement(By.tagName("h1")).getText();
        chapter.setTitle(chapterTitle);
    }
    private static void parseChapterID(){
        String[] tempArray = driver.getCurrentUrl().split("/");
        int chapterPixivID = Integer.parseInt(tempArray[tempArray.length-1]);
        chapter.setPixivID(chapterPixivID);
    }
    private static void parseChapterUploadDate(){
        String chapterUploadDate = driver.findElement(By.tagName("time"))
                .getDomAttribute("datetime").split("T")[0];
        chapter.setUploadDate(chapterUploadDate);
    }
    private static void parseChapterNumber(){
        String[] tempArray;
        if(isLoggedIn){
            tempArray = driver
                    .findElement(By.xpath("/html/body/div[1]/div/div[2]/div[5]/div[1]/div/div[1]/main/section/div[1]/div/figcaption/div[2]/div/div[2]/a"))
                    .getText().split("#");
        }
        else {
            tempArray = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[1]/div/figcaption/div[2]/div/div[2]/a"))
                    .getText().split("#");
        }
        int chapterNumber = Integer.parseInt(tempArray[tempArray.length - 1]);
        chapter.setChapterNumber(chapterNumber);
    }
    private static void parseChapterPageCount(){
        int chapterPageAmount;
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        List<WebElement> tempList = driver.findElements(By.className("gtm-manga-viewer-open-preview"));
        if(tempList.isEmpty()){ //this code is done because the page counter doesn't appear on single-page chapters
            chapterPageAmount = 1;
        }
        else{
            String[] tempArray = tempList.getFirst()
                    .findElement(By.tagName("div"))
                    .findElement(By.tagName("span"))
                    .getText().split("/");
            chapterPageAmount = Integer.parseInt(tempArray[tempArray.length - 1]);
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(waitTime));
        chapter.setPageAmount(chapterPageAmount);
    }
    private static void parseImageLinks(){
        ArrayList<String> imageLinks = new ArrayList<>();
        int chapterPageAmount = chapter.getPageAmount();
        String temp;
        if(chapterPageAmount == 1 && isLoggedIn){
            temp = driver
                    .findElement(By.xpath("/html/body/div[1]/div/div[2]/div[5]/div[1]/div/div[1]/main/section/div[1]/div/figure/div[1]/div[1]/div/a/img"))
                    .getDomAttribute("src");
        }
        else if(chapterPageAmount > 1 && isLoggedIn){
            temp = driver
                    .findElement(By.xpath("/html/body/div[1]/div/div[2]/div[5]/div[1]/div/div[1]/main/section/div[1]/div/figure/div/div[2]/div/a/img"))
                    .getDomAttribute("src");
        }
        else if(chapterPageAmount == 1 && !isLoggedIn){
            temp = driver
                    .findElement(By.xpath("/html/body/div[1]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[1]/div/figure/div[1]/div[1]/div/div/img"))
                    .getDomAttribute("src");
        }
        else{ //if(chapterPageAmount > 1 && !isLoggedIn)
            temp = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[1]/div/figure/div/div[2]/div/div/img"))
                    .getDomAttribute("src");
        }

        for(int page = 0; page< chapterPageAmount; page++){
            assert temp != null;
            imageLinks.add(temp
                    .replace("0_master1200", page+"")
                    .replace("master", "original")
                    .replace(".jpg", ".png"));
        }
        chapter.setImageLinks(imageLinks);
    }
}