package com.philkes.pin2pdf;

import java.nio.charset.StandardCharsets;

public class Util {

    // Replace trash with german Umlaute
    public static String toStr(String str){
        String s= str
                .replace("&#196;","Ä")
                .replace("&#228;","ä")
                .replace("&#214;","Ö")
                .replace("&#246;","ö")
                .replace("&#220;","Ü")
                .replace("&#252;","ü")
                .replace("&#223;","ß");
        return s;
    }

    public static String getUrlDomainName(String url) {
        String domainName = new String(url);

        int index = domainName.indexOf("://");

        if (index != -1) {
            // keep everything after the "://"
            domainName = domainName.substring(index + 3);
        }

        index = domainName.indexOf('/');

        if (index != -1) {
            // keep everything before the '/'
            domainName = domainName.substring(0, index);
        }

        // check for and remove a preceding 'www'
        // followed by any sequence of characters (non-greedy)
        // followed by a '.'
        // from the beginning of the string
        domainName = domainName.replaceFirst("^www.*?\\.", "");

        return domainName;
    }
}
