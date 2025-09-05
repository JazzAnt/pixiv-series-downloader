package org.jazzant.pixivseriesdownloader;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;

public class ImageURLUtils {
    private ImageURLUtils(){};

    /**
     * Connects to an image URL and retrieves the byte array of it's input stream.
     * @param imageURL the URL to obtain the image
     * @return a byte array containing the image bytes.
     * @throws URISyntaxException if the given URL is invalid.
     * @throws IOException if the attempt to open a connection fails.
     */
    public static byte[] getByteArrayFromImageURL(String imageURL) throws URISyntaxException, IOException {
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
    public static String getImageIDFromImageURL(String imageURL){
        String[] pathArray = imageURL.split("/");
        return pathArray[pathArray.length - 1];
    }
}
