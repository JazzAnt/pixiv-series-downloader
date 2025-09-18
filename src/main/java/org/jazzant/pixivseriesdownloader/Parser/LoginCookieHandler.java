package org.jazzant.pixivseriesdownloader.Parser;

import org.openqa.selenium.Cookie;

import java.io.*;

public class LoginCookieHandler {
    //static for easier testing purposes, will change to oop later
        public static void saveCookie(Cookie cookie){
            try {
                FileOutputStream fos = new FileOutputStream("loginCookie.ser");
                ObjectOutputStream oos = new ObjectOutputStream(fos);
                oos.writeObject(cookie);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        public static Cookie getCookie(){
            try {
                FileInputStream fis = new FileInputStream("loginCookie.ser");
                ObjectInputStream ois = new ObjectInputStream(fis);
                Cookie cookie = (Cookie) ois.readObject();
                return cookie;
            } catch (FileNotFoundException e) {
               return null;
            } catch (IOException | ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }

        public static boolean isCookieNotExpired(Cookie cookie){
            long expiryDate = cookie.getExpiry().getTime();
            long currentTime = System.currentTimeMillis();
            return currentTime < expiryDate;
        }
}
