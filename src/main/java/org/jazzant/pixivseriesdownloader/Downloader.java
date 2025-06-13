package org.jazzant.pixivseriesdownloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Downloader {
    private static final int SAVE_AS_CBZ = 0;
    private static final int SAVE_AS_ZIP = 1;
    private static final int SAVE_AS_FOLDER = 2;

    private int saveAs;
    private String libraryDirectory;
    private String currentSeriesDirectory;


    public void downloadChapter(ArrayList<String> downloadLinks, String directory, String chapterName){
        new File(directory).mkdirs();
        String chapterDirectory = directory + "\\" + chapterName;
        try {
            FileOutputStream fos = new FileOutputStream(chapterDirectory);
            ZipOutputStream zipOut = new ZipOutputStream(fos);

            for(String link : downloadLinks){
                URLConnection connection = new URI(link).toURL().openConnection();
                connection.addRequestProperty("Referer", "https://www.pixiv.net");
                String[] pathArray = connection.getURL().getPath().split("/");
                String filename = pathArray[pathArray.length - 1];

                ZipEntry zipEntry = new ZipEntry(filename);
                zipOut.putNextEntry(zipEntry);

                InputStream is = connection.getInputStream();

                byte[] buffer = new byte[1024];
                int length;
                while((length = is.read(buffer)) != -1){
                    zipOut.write(buffer, 0, length);
                }
                is.close();
            }
            zipOut.close();
            fos.close();
        } catch (IOException | URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }
}
