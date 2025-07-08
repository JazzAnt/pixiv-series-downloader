package org.jazzant.pixivseriesdownloader;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.io.*;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A static class to download pixiv series chapters as various file formats.
 */
public class Downloader{
    private static String libraryDir = System.getProperty("user.home") + "/Library";
    private static SaveAs fileFormat = SaveAs.ZIP;
    private static String filenameFormatter = "Chapter{chapter_number}_{chapter_title}";

    /**
     * Static class, cannot instantiate.
     */
    private Downloader(){}

    /**
     * Returns the library directory (where everything will be downloaded).
     * @return the directory of the download library.
     */
    public static String getLibraryDir() {
        return libraryDir;
    }

    /**
     * Returns the file format the downloaded chapters will be saved as.
     * @return the file format.
     */
    public static SaveAs getFileFormat() {
        return fileFormat;
    }

    /**
     * Returns the filename format of the downloaded chapters.
     * @return the filename format.
     */
    public static String getFilenameFormatter() {
        return filenameFormatter;
    }

    /**
     * Set's the directory for all the file and chapter downloads. If unset, default is 'home/Library' where home is the
     * user's home directory (e.g. in Windows it's usually C:/Users/Username).
     * @param libraryDirectory the directory of your library.
     * @throws DownloaderException if the downloader cannot find the directory given by the parameter for any reason.
     */
    public static void setLibraryDir(String libraryDirectory) throws DownloaderException{
        if(!Files.isDirectory(Paths.get(libraryDirectory))) throw new DownloaderException("Downloader cannot find the given directory of " + libraryDirectory);
        libraryDir = libraryDirectory;
    }

    /**
     * Sets the format of the chapter downloads. Choices are any of the options in the SaveAs enumeration. Default is zip.
     * @param saveAs the format in which the chapters will be saved.
     */
    public static void setFileFormat(SaveAs saveAs){fileFormat = saveAs;}

    /**
     * The format of the chapter names of your downloads. If unset, defaults to 'Chapter{chapter_number}_{chapter_title}'.
     * The filename can be given keywords which will be replaced with their corresponding values.
     * Possible keywords are: {series_title}, {artist}, {series_id}, {artist_id}, {chapter_number}, {chapter_title},
     * {chapter_id}, {upload_date}, {page_amount}.
     * Note that your chapter must contain either the keyword {chapter_id} or {chapter_number} in order to avoid
     * multiple chapters having the same name.
     * @param filenameFormat the file format of the downloaded chapters.
     * @throws DownloaderException if the filename format parameter doesn't contain either {chapter_id} or {chapter_number}.
     */
    public static void setFilenameFormat(String filenameFormat) throws DownloaderException{
        if(!filenameFormat.contains("{chapter_id}") && !filenameFormat.contains("{chapter_number}"))
            throw new DownloaderException("The filename format must contain either {chapter_id} or {chapter_number}.");
        filenameFormatter = filenameFormat;
    }

    /**
     * Downloads the chapter. Determines the directory and file format through internal parameters which can be changed
     * using setLibrary, setFileFormat, and setFilenameFormat. Will use default values if unset.
     * @param series the series containing the chapter.
     * @param chapter the chapter to be downloaded.
     * @return boolean true if the chapter is downloaded properly and false otherwise.
     */
    public static boolean downloadChapter(Series series, Chapter chapter) throws DownloaderException{
        if(!series.isValid()) throw new DownloaderException("The given Series does not have all the necessary variables set");
        if(!chapter.isValid()) throw new DownloaderException("The given Chapter does not have all the necessary variables set");
        new File(generateSeriesDirectory(series)).mkdirs();
        String fileDirectory = generateFileDirectory(series, chapter);
        ArrayList<String> imageURLs = chapter.getImageURLs();
        if(fileFormat == SaveAs.ZIP || fileFormat == SaveAs.CBZ) return downloadChapterAsZip(imageURLs, fileDirectory);
        if(fileFormat == SaveAs.FOLDER) return downloadChapterAsFolder(imageURLs, fileDirectory);
        if(fileFormat == SaveAs.PDF) return downloadChapterAsPdf(imageURLs, fileDirectory);
        return false;
    }

    /**
     * Defines the file directory and file formatting to be used by the downloadChapterAs... methods.
     * @param series the series containing the chapter.
     * @param chapter the chapter to be downloaded.
     * @return the file directory to be used by the downloadChapterAs... methods.
     */
    private static String generateFileDirectory(Series series, Chapter chapter){
        String fileDirectory = generateSeriesDirectory(series) + "/" + generateFileName(series, chapter);
        if(fileFormat == SaveAs.ZIP) fileDirectory += ".zip";
        else if(fileFormat == SaveAs.CBZ) fileDirectory += ".cbz";
        else if(fileFormat == SaveAs.PDF) fileDirectory += ".pdf";
        return fileDirectory;
    }

    /**
     * Downloads the chapter as a zip (either .zip or .cbz).
     * @param imageURLs a list of the image urls to be downloaded.
     * @param fileDirectory the directory of the download location, including the filename and file formatting. Should
     *                      be generated by generateFileDirectory method.
     * @return true if the download is successful and false otherwise.
     */
    private static boolean downloadChapterAsZip(ArrayList<String> imageURLs, String fileDirectory){
        try (FileOutputStream fos = new FileOutputStream(fileDirectory);
             ZipOutputStream zipOut = new ZipOutputStream(fos);)
        {
            for(String imageURL : imageURLs){
                try(InputStream inputStream = getInputStreamFromImageURL(imageURL)){
                    ZipEntry zipEntry = new ZipEntry(getImageIDFromImageURL(imageURL));
                    zipOut.putNextEntry(zipEntry);
                    zipOut.write(getByteArrayFromImageURL(imageURL));
                }
                catch (URISyntaxException e){
                    return false;
                }
            }
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Downloads the chapter as a folder containing the images in uncompressed png files.
     * @param imageURLs a list of the image urls to be downloaded.
     * @param fileDirectory the directory of the download location, including the filename and file formatting. Should
     *                      be generated by the generateFileDirectory method.
     * @return true if the download is successful and false otherwise.
     */
    private static boolean downloadChapterAsFolder(ArrayList<String> imageURLs, String fileDirectory){
        new File(fileDirectory).mkdirs();
        for(String imageURL : imageURLs){
            try(ReadableByteChannel rbc = Channels.newChannel(getInputStreamFromImageURL(imageURL));
                FileOutputStream fos = new FileOutputStream(fileDirectory + "/" + getImageIDFromImageURL(imageURL));
                FileChannel fileChannel = fos.getChannel();)
            {
                fileChannel.transferFrom(rbc,0, Long.MAX_VALUE);
            }
            catch (IOException | URISyntaxException e){
               return false;
            }
        }
        return true;
    }

    /**
     * Downloads the chapter as a pdf.
     * @param imageURLs a list of the image urls to be downloaded.
     * @param fileDirectory the directory of the download location, including the filename and file formatting. Should
     *                      be generated by the generateFileDirectory method.
     * @return true if the download is successful and false otherwise.
     */
    private static boolean downloadChapterAsPdf(ArrayList<String> imageURLs, String fileDirectory){
        PDDocument document = new PDDocument();
        for(String imageURL : imageURLs){
            try {
                PDImageXObject image = PDImageXObject.createFromByteArray(document, getByteArrayFromImageURL(imageURL), getImageIDFromImageURL(imageURL));
                PDPage page = new PDPage(new PDRectangle(image.getWidth(), image.getHeight()));
                document.addPage(page);
                PDPageContentStream contentStream = new PDPageContentStream(document, page);
                contentStream.drawImage(image, 0, 0);
                contentStream.close();
            }
            catch (URISyntaxException | IOException e){
                return false;
            }
        }
        try {
            document.save(fileDirectory);
        }
        catch (IOException e){
            return false;
        }
        return true;
    }

    /**
     * Connects to an image URL and retrieves the byte array of it's input stream.
     * @param imageURL the URL to obtain the image
     * @return a byte array containing the image bytes.
     * @throws URISyntaxException if the given URL is invalid.
     * @throws IOException if the attempt to open a connection fails.
     */
    private static byte[] getByteArrayFromImageURL(String imageURL) throws URISyntaxException, IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = getInputStreamFromImageURL(imageURL);
        int length;
        byte[] buffer = new byte[1024];
        while((length = inputStream.read(buffer)) != -1){
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Connects to an image URL and returns the input stream.
     * @param imageURL the URL to obtain the image
     * @return the input stream of the image URL
     * @throws URISyntaxException if the given URL is invalid.
     * @throws IOException if the attempt to open a connection fails.
     */
    public static InputStream getInputStreamFromImageURL(String imageURL) throws URISyntaxException, IOException {
        URLConnection connection = new URI(imageURL).toURL().openConnection();
        connection.setRequestProperty("Referer", "https://www.pixiv.net");
        return connection.getInputStream();
    }

    /**
     * Returns the image's Pixiv ID from its URL, which is simply the last series of digit on the URL.
     * @param imageURL the URL of the image to retrieve the ID from.
     * @return the image's Pixiv ID.
     */
    private static String getImageIDFromImageURL(String imageURL){
        String[] pathArray = imageURL.split("/");
        return pathArray[pathArray.length - 1];
    }

    /**
     * Retrieves the directory parameter of the Series object and assemble them into a directory path.
     * @param series the Series object containing the directory details.
     * @return the directory path as a String.
     */
    private static String generateSeriesDirectory(Series series){
        return libraryDir + "/" + series.getDirectoryGroup() + "/" + series.getDirectoryTitle();
    }

    /**
     * Generates the filename of the chapter to be downloaded. See the setFilenameFormat on details on how the filename
     * will be formatted and how the user can modify it.
     * @param series the Series containing the chapter.
     * @param chapter the Chapter to be downloaded.
     * @return the filename of the chapter to be downloaded.
     */
    private static String generateFileName(Series series, Chapter chapter){
        return filenameFormatter
                .replace("{series_title}", series.getTitle())
                .replace("{artist}", series.getArtist())
                .replace("{artist_id}", series.getArtistID()+"")
                .replace("{series_id}", series.getSeriesID()+"")
                .replace("{chapter_number}", chapter.getChapterNumber()+"")
                .replace("{chapter_title}", chapter.getTitle())
                .replace("{chapter_id}", chapter.getPixivID()+"")
                .replace("{upload_date}", chapter.getUploadDate())
                .replace("{page_amount}", chapter.getPageAmount()+"")
                ;
    }
}
