package org.nott.utils;

public class CommonUtils {

    public static String[] removeFirstElement(String[] args){
        if(args.length == 0){
            return args;
        }
        String[] result = new String[args.length - 1];
        System.arraycopy(args, 1, result, 0, args.length - 1);
        return result;
    }
}
