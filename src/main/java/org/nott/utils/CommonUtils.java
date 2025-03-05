package org.nott.utils;

import java.text.SimpleDateFormat;
import java.util.UUID;

public class CommonUtils {

    public static final SimpleDateFormat HHMMDDHMS = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static String[] removeFirstElement(String[] args){
        if(args.length == 0){
            return new String[]{};
        }
        String[] result = new String[args.length - 1];
        System.arraycopy(args, 1, result, 0, args.length - 1);
        return result;
    }

    public static String uid(){
        return UUID.randomUUID().toString();
    }
}
