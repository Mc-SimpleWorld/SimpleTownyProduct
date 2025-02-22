package org.nott.utils;

public class CommonUtils {

    public static String[] removeFirstElement(String[] args){
        String[] result = new String[args.length - 1];
        System.arraycopy(args, 1, result, 0, args.length - 1);
        return result;
    }
}
