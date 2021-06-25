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
}
