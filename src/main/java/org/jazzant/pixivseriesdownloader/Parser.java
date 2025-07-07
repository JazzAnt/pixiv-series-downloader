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

/**
 * Static class that uses a Selenium WebDriver to parse through the pixiv URLs.
 */
public class Parser {
    private static boolean initialized = false;
    private static WebDriver driver;
    private static WebDriver.Window window;
    private static WebDriverWait driverWait;
    private static WebDriverWait driverLongWait;
    private static boolean isLoggedIn;
    private static Point screenCenter;
    private static String pixivUsername;
    private static String pixivPassword;
    private static int waitTime;

    /**
     * Static class, cannot be instantiated.
     */
    private Parser(){}

    /**
     * Initializes the Parser, creating the WebDriver and settings. Every other public method will fail if this method
     * haven't been called.
     */
    public static void initialize(){
        if(initialized) throw new ParserException("Parser can only be initialized once!");
        pixivUsername = "";
        pixivPassword = "";
        waitTime = 10;
        isLoggedIn = false;
        FirefoxOptions options = new FirefoxOptions();
        options.addArguments("--width=400");
        options.addArguments("--height=500");
        driver = new FirefoxDriver(options);
        window = driver.manage().window();
        driverWait = new WebDriverWait(driver, Duration.ofSeconds(waitTime));
        driverLongWait = new WebDriverWait(driver, Duration.ofSeconds(90));
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        int x = screenSize.width / 2;
        int y = screenSize.height / 2;
        screenCenter = new Point(x, y);
        windowMinimize();
        initialized = true;
    }

    /**
     * Closes the WebDriver.
     */
    public static void close(){
        validateInitialization();
        driver.close();
        initialized = false;
    }

    /**
     * Set's the email/username of the user's pixiv account.
     * @param username the user's pixiv account username/email.
     */
    public static void setPixivUsername(String username){
        validateInitialization();
        pixivUsername = username;
    }

    /**
     * Sets the password of the user's pixiv account.
     * @param password the user's pixiv account password.
     */
    public static void setPixivPassword(String password){
        validateInitialization();
        pixivPassword = password;
    }
    /**
     * Sets the amount of wait time the driver will wait for various operation attempts (such as waiting for
     * a webpage or web element to load) before giving up.
     * @param driverWaitTime the amount of wait time in seconds (Default is 10 seconds)
     */
    public static void setWaitTime(int driverWaitTime){
        validateInitialization();
        if(driverWaitTime<0) driverWaitTime = 0;
        waitTime = driverWaitTime;
        driverWait.withTimeout(Duration.ofSeconds(waitTime));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(waitTime));
    }

    /**
     * Opens the login page. If the user has set both a username and a password then the Parser will fill both fields and
     * attempt to automatically log-in. The screen will be brought to the front if the user hasn't set the username and
     * password, or if the user has but the login fails either because one or both of the credentials is wrong or because
     * a reCaptcha appears. In whichever case, if the login screen is brought to front the user will have to complete the
     * login process by themselves.
     * @return boolean true if the login is successful and false otherwise.
     */
    public static boolean loginPixiv() {
        validateInitialization();
        if(isLoggedIn) throw new ParserException("The user is already logged in.");
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
        if(!isLoggedIn) {
            windowToFront();
            isLoggedIn = waitForLoginLong();
        }
        return isLoggedIn;
    }

    /**
     * Parses through the series page to find various details regarding the series.
     * @param seriesURL the URL of the series page. The one formatted something like 'www.pixiv.net/user/00000/series/00000'.
     * @return a Series object containing the details of the parsed series. The group and title directories and by default
     * set as the series artist and title, the series status as ONGOIGN, and the latestChapterID as 0.
     * @throws ParserSeriesDoesNotExistException if the Parser can't find the series in the given URL, either because the URL
     * is wrong or because the series has been deleted.
     */
    public static Series parseSeries(String seriesURL)
            throws ParserSeriesDoesNotExistException{
        validateInitialization();
        if(!Series.checkSeriesURLFormat(seriesURL)) throw new ParserException("The series's URL format is incorrect.");
        driver.get(seriesURL);
        checkIfSeriesExists();

        Series series = new Series();
        series.setSeriesIDAndArtistIDsFromSeriesURL(seriesURL);
        series.setArtist(parseSeriesArtist());
        series.setTitle(parseSeriesTitle());
        series.setStatus(SeriesStatus.ONGOING);
        series.setDirectoryGroup(series.getArtist());
        series.setDirectoryTitle(series.getTitle());
        series.setLatestChapterID(0);
        return series;
    }

    /**
     * Parses through an chapter page to find various details regarding the chapter, including the image download links.
     * @param chapterURL the URL of the chapter page. The one formatted something like 'www.pixiv.net/artworks/000000'.
     * @return a Chapter object containing the details of the parsed chapter.
     * @throws ParserSensitiveArtworkException if the chapter is blocked because it contains sensitive content, which can
     * only be viewed while logged in to an account that allows sensitive content to proceed.
     * @throws ParserMutedArtworkException if the chapter is blocked because it contained a muted (blacklisted) tag by the
     * user's pixiv account.
     */
    public static Chapter parseChapter(String chapterURL)
            throws ParserSensitiveArtworkException, ParserMutedArtworkException{
        validateInitialization();
        if(!Chapter.checkChapterURLFormat(chapterURL)) throw new ParserException("The chapter's URL format is incorrect.");
        driver.get(chapterURL);
        checkIfChapterIsSensitive();
        checkIfChapterIsMuted();

        Chapter chapter = new Chapter();
        chapter.setTitle(parseChapterTitle());
        chapter.setPixivID(parseChapterID());
        chapter.setUploadDate(parseChapterUploadDate());
        chapter.setChapterNumber(parseChapterNumber());
        chapter.setPageAmount(parseChapterPageCount());
        chapter.setImageLinks(parseChapterImageURLs(chapter.getPageAmount()));
        return chapter;
    }

    /**
     * Checks if the parser is currently in the latest chapter.
     * @return true if it's the latest chapter, and false if not.
     */
    public static boolean isLatestChapter(){
        validateInitialization();
        if(inSeriesPage()) return false;
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        boolean isLatestChapter = driver.findElements(By.className("gtm-series-next-work-button-in-illust-detail")).isEmpty();
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(waitTime));
        return isLatestChapter;
    }

    /**
     * Parses the next chapter's URL, or the first chapter's URL if the Parser is currently in the series page.
     * @return the next chapter's URL.
     */
    public static String getNextChapterURL(){
        validateInitialization();
        String nextChapterLink;
        String PIXIV_URL = "https://www.pixiv.net";
        if(inSeriesPage()){
            nextChapterLink = PIXIV_URL + driver
                    .findElement(By.className("gtm-manga-series-first-story")).findElement(By.tagName("a"))
                    .getDomAttribute("href");
        }
        else {
            try {
                nextChapterLink = PIXIV_URL + driver
                        .findElement(By.className("gtm-series-next-work-button-in-illust-detail"))
                        .getDomAttribute("href");
            }
            catch (NoSuchElementException e){
                throw new ParserException("The Parser cannot find the next chapter's url. " +
                        "Try using the isLatestChapter() method to make sure the current chapter isn't the last one.");
            }
        }
        return nextChapterLink;
    }


    /* ********************************
     * SERIES PARSING PRIVATE METHODS *
     * ********************************/

    /**
     * Checks if the driver is currently in the series page.
     * @return true if it is.
     */
    private static boolean inSeriesPage(){
        return Series.checkSeriesURLFormat(Objects.requireNonNull(driver.getCurrentUrl()));
    }

    /**
     * Checks if the series exists, throwing an exception if it doesn't. Must be called while the Parser is in the series page.
     * @throws ParserSeriesDoesNotExistException
     */
    private static void checkIfSeriesExists(){
        if(!inSeriesPage()) throw new ParserException("This method can only be called while the driver is in the series page.");
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        List<WebElement> tempList = driver.findElements(By.className("gtm-manga-series-first-story"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(waitTime));
        if(tempList.isEmpty()) throw new ParserSeriesDoesNotExistException("The parser cannot find the series in the given url. " +
                "Either the url is incorrect or the series had been deleted.");
    }

    /**
     * Parses the series title.
     * @return the series title.
     */
    private static String parseSeriesTitle(){
        return driver
                .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[5]/div[1]/div/div[1]/main/div[last()]/section/div/div[1]/div[1]"))
                .getText();
    }

    /**
     * Parses the series artist.
     * @return the series artist.
     */
    private static String parseSeriesArtist(){
        return driver
                .findElement(By.xpath("//div[contains(@class, 'gtm-manga-series-profile')]/div/div/a/div"))
                .getText();
    }

    /* *********************************
     * CHAPTER PARSING PRIVATE METHODS *
     * *********************************/

    /**
     * Checks if the parser is currently in an artwork page, throwing an exception if it isn't.
     */
    private static void checkIfInArtworkPage(){
        if(!Chapter.checkChapterURLFormat(Objects.requireNonNull(driver.getCurrentUrl()))) throw new ParserException("This method can only be called while the driver is in an artwork page");
    }

    /**
     * Checks if the current chapter is blocked due to having sensitive content that the user isn't permitted to view,
     * throwing an exception if it does.
     * @throws ParserSensitiveArtworkException
     */
    private static void checkIfChapterIsSensitive() throws ParserSensitiveArtworkException{
        checkIfInArtworkPage();
        List<WebElement> tempList = driver.findElements(By.tagName("pixiv-icon"));
        if(Objects.equals(tempList.getLast().getDomAttribute("name"), "24/SensitiveHide")
        ) throw new ParserSensitiveArtworkException("The current chapter contains sensitive content. " +
                "It cannot be parsed unless the user is logged in.");
    }

    /**
     * Check if the current chapter is blocked due to containing a tag that is muted (blacklisted) by the current user,
     * throwing an exception if it does.
     * @throws ParserMutedArtworkException
     */
    private static void checkIfChapterIsMuted() throws ParserMutedArtworkException{
        if(!isLoggedIn) return;
        checkIfInArtworkPage();
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        List<WebElement> tempList = driver.findElements(By.className("dEfTUV"));
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(waitTime));
        if(tempList.isEmpty()) throw new ParserMutedArtworkException("The current chapter contains a tag " +
                "that is muted (blacklisted) by the current user.");
    }

    /**
     * Parses the chapter title.
     * @return the chapter title.
     */
    private static String parseChapterTitle(){
        return driver.findElement(By.tagName("h1")).getText();
    }

    /**
     * Parses the chapter pixiv ID.
     * @return the chapter pixiv ID.
     */
    private static int parseChapterID(){
        String[] tempArray = driver.getCurrentUrl().split("/");
        return Integer.parseInt(tempArray[tempArray.length-1]);
    }

    /**
     * Parses the chapter upload date.
     * @return the chapter upload date.
     */
    private static String parseChapterUploadDate(){
        return Objects.requireNonNull(
                        driver.findElement(By.tagName("time"))
                                .getDomAttribute("datetime"))
                .split("T")
                [0];
    }

    /**
     * parses the chapter number (it's numeric order in the series).
     * @return the chapter number.
     */
    private static int parseChapterNumber(){
        String[] tempArray;
        if(isLoggedIn){
            tempArray = driver
                    .findElement(By.xpath("/html/body/div[1]/div/div[2]/div[5]/div[1]/div/div[1]/main/section/div[1]/div/figcaption/div[2]/div/div[2]/a"))
                    .getText()
                    .split("#");
        }
        else {
            tempArray = driver
                    .findElement(By.xpath("//*[@id=\"__next\"]/div/div[2]/div[6]/div[1]/div/div[1]/main/section/div[1]/div/figcaption/div[2]/div/div[2]/a"))
                    .getText()
                    .split("#");
        }
        return Integer.parseInt(tempArray[tempArray.length - 1]);
    }

    /**
     * Parses the chapter page count.
     * @return the chapter page count.
     */
    private static int parseChapterPageCount(){
        int chapterPageAmount;
        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        List<WebElement> tempList = driver.findElements(By.className("gtm-manga-viewer-open-preview"));
        if(tempList.isEmpty()){ //the page counter doesn't appear on single-page chapters
            chapterPageAmount = 1;
        }
        else{
            String[] tempArray = tempList.getFirst()
                    .findElement(By.tagName("div"))
                    .findElement(By.tagName("span"))
                    .getText()
                    .split("/");
            chapterPageAmount = Integer.parseInt(tempArray[tempArray.length - 1]);
        }
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(waitTime));
        return chapterPageAmount;
    }

    /**
     * Parses the chapter image URLs as a String ArrayList.
     * @param chapterPageAmount the amount of pages (images) in this chapter.
     * @return an arraylist of image URLs
     * @throws InvalidArgumentException if given a page amount less than 1.
     */
    private static ArrayList<String> parseChapterImageURLs(int chapterPageAmount){
        if(chapterPageAmount < 1) throw new InvalidArgumentException("The page amount can't be lower than 1.");
        ArrayList<String> imageLinks = new ArrayList<>();
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
        else if(chapterPageAmount == 1){// && !isLoggedIn)
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
        return imageLinks;
    }

    /*
     * LOGIN PRIVATE METHODS
     */

    /**
     * Goes to the login page.
     */
    private static void goToLoginPage(){
        driver.get("https://accounts.pixiv.net/login");
    }

    /**
     * Fills in the username field if the username has been set in the Parser.
     * @return true if the username field has been successfully filled.
     */
    private static boolean enterUsernameField(){
        if(pixivUsername.isBlank()) return false;
        driver.findElements(By.tagName("fieldset")).get(0)
                .findElement(By.tagName("input"))
                .sendKeys(pixivUsername);
        return true;
    }

    /**
     * Fills in the password field if the password has been set in the Parser.
     * @return true if the password field has been successfully filled.
     */
    private static boolean enterPasswordField(){
        if(pixivPassword.isBlank()) return false;
        driver.findElements(By.tagName("fieldset")).get(1)
                .findElement(By.tagName("input"))
                .sendKeys(pixivPassword);
        return true;
    }

    /**
     * Clicks the login button.
     */
    private static void clickLoginButton(){
        driver.findElement(By.id("app-mount-point"))
                .findElements(By.tagName("button")).get(5)
                .click();
    }

    /**
     * Wait for a successful login. Waits for as long as the waitTime variable in seconds.
     * @return true if within the wait time the login is successful.
     */
    private static boolean waitForLoginShort(){
        try {
            driverWait.until(d -> driver.getCurrentUrl().contains("www.pixiv.net"));
            return true;
        }
        catch (TimeoutException _){
            return false;
        }
    }

    /**
     * Wait for a successful login for a longer time than waitForLoginShort. Check the initialize() method to see how
     * long it is.
     * @return true if within the wait time the login is successful.
     */
    private static boolean waitForLoginLong(){
        try {
            driverLongWait.until(d -> driver.getCurrentUrl().contains("www.pixiv.net"));
            return true;
        }
        catch (TimeoutException _){
            return false;
        }
    }

    /**
     * Detects if a reCAPTCHA is present on the screen.
     * @return true if there is a reCAPTCHA.
     */
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

    /*
     * MISC PRIVATE METHODS
     */

    /**
     * Minimizes the driver window.
     */
    private static void windowMinimize(){
        window.minimize();
    }

    /**
     * Brings the driver window to the front of the tabs in order to bring attention to it to the user.
     */
    private static void windowToFront(){
        window.minimize();
        window.setPosition(screenCenter);
    }

    /**
     * Checks if the Parser is initialized, throwing an exception if it isn't. This method should be called on every
     * public method except initialize() in order to make sure that none of them are called without the Parser being
     * initialized.
     */
    private static void validateInitialization(){
        if(!initialized) throw new ParserNotInitializedException(
                "The Parser has not been initialized. Please run Parser.initialize() before calling this method."
        );
    }
}