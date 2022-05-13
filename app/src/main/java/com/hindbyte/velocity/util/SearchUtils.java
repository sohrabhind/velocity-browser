package com.hindbyte.velocity.util;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class SearchUtils {

    public String makeGoogleImageSearch(String imageUrl) {
        try {
            String GOOGLE_IMAGE_SEARCH = "https://www.google.com/searchbyimage?image_url=";
            return GOOGLE_IMAGE_SEARCH + URLEncoder.encode(imageUrl, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
}
