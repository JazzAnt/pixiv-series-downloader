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
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Downloader{
    private static String libraryDir;
    private static SaveAs fileFormat;
    private static String filenameFormat;
    private static String fileDirectory;
    private static Series series;
    private static Chapter chapter;

    private Downloader(){}
    public static void initialize(){
        libraryDir = "C:\\Library";
        fileFormat = SaveAs.ZIP;
        filenameFormat = "";
    }
    public static void setLibraryDir(String libraryDirectory){libraryDir = libraryDirectory;}
    public static void setFileFormat(SaveAs saveAs){fileFormat = saveAs;}
    public static void setFilenameFormat(String filenameFormatting){filenameFormat = filenameFormatting;}
    public static void setSeries(Series seriesObject){series = seriesObject;}
    public static void setChapter(Chapter chapterObject){chapter = chapterObject;}
    public static boolean downloadChapter(){
        if(!checkValidity()){
            return false; //TODO: replace with throwing an exception
        }
        new File(generateSeriesDirectory()).mkdirs();
        prepareFileDirectory();
        if(fileFormat == SaveAs.ZIP || fileFormat == SaveAs.CBZ) return downloadChapterAsZip();
        if(fileFormat == SaveAs.FOLDER) return downloadChapterAsFolder();
        if(fileFormat == SaveAs.PDF) return downloadChapterAsPdf();
        return false;
    }

    private static boolean checkValidity(){
        if(libraryDir == null || libraryDir.isBlank() ||
                fileFormat == null ||
                filenameFormat == null ||
                series == null || !series.isValid() ||
                chapter == null || !chapter.isValid()
        ) return false;
        return true;
    }
    private static void prepareFileDirectory(){
        fileDirectory = generateSeriesDirectory() + "\\" + generateFileName();
        if(fileFormat == SaveAs.ZIP) fileDirectory += ".zip";
        else if(fileFormat == SaveAs.CBZ) fileDirectory += ".cbz";
        else if(fileFormat == SaveAs.PDF) fileDirectory += ".pdf";
    }
    private static boolean downloadChapterAsZip(){
        try (FileOutputStream fos = new FileOutputStream(fileDirectory);
             ZipOutputStream zipOut = new ZipOutputStream(fos);)
        {
            for(String link : chapter.getImageLinks()){
                try(InputStream inputStream = getInputStreamFromImageLink(link)){
                    ZipEntry zipEntry = new ZipEntry(getImageIDFromImageLink(link));
                    zipOut.putNextEntry(zipEntry);
                    zipOut.write(getByteArrayFromImageLink(link));
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
    private static boolean downloadChapterAsFolder(){
        new File(fileDirectory).mkdirs();
        for(String link : chapter.getImageLinks()){
            try(ReadableByteChannel rbc = Channels.newChannel(getInputStreamFromImageLink(link));
                FileOutputStream fos = new FileOutputStream(fileDirectory + "\\" + getImageIDFromImageLink(link));
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
    private static boolean downloadChapterAsPdf(){
        PDDocument document = new PDDocument();
        for(String link : chapter.getImageLinks()){
            try {
                PDImageXObject image = PDImageXObject.createFromByteArray(document, getByteArrayFromImageLink(link), getImageIDFromImageLink(link));
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
    private static byte[] getByteArrayFromImageLink(String imageLink) throws URISyntaxException, IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        InputStream inputStream = getInputStreamFromImageLink(imageLink);
        int length;
        byte[] buffer = new byte[1024];
        while((length = inputStream.read(buffer)) != -1){
            byteArrayOutputStream.write(buffer, 0, length);
        }
        return byteArrayOutputStream.toByteArray();
    }
    private static InputStream getInputStreamFromImageLink(String imageLink) throws URISyntaxException, IOException {
        URLConnection connection = new URI(imageLink).toURL().openConnection();
        connection.setRequestProperty("Referer", "https://www.pixiv.net");
        return connection.getInputStream();
    }
    private static String getImageIDFromImageLink(String imageLink){
        String[] pathArray = imageLink.split("/");
        return pathArray[pathArray.length - 1];
    }
    private static String generateSeriesDirectory(){
        return libraryDir + "\\" + series.getDirectoryGroup() + "\\" + series.getDirectoryTitle();
    }
    private static String generateFileName(){
        if(filenameFormat.contains("{chapter_title}")
                || filenameFormat.contains("{chapter_number}")
                || filenameFormat.contains("{chapter_id}")
                || filenameFormat.contains("{upload_date}")
        )
            return filenameFormat
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
        return "Chapter" + chapter.getChapterNumber() + "_" + chapter.getTitle();
    }
}
