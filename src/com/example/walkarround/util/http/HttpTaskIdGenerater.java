package com.example.walkarround.util.http;

import java.util.Random;

public class HttpTaskIdGenerater {
    //最小值为0000 0000 00
    //最大值为ZZZZ ZZZZ ZZ
    private static String str[]={"0","1","2","3","4","5","6","7","8","9","a","b","c","d","e","f","g","h","i","j","k","l","m","n","o","p","q","r","s","t","u","v","w","x","y","z"
            ,"A","B","C","D","E","F","G","H","I","J","K","L","M","N","O","P","Q","R","S","T","U","V","W","X","Y","Z"};


    public static String getId(){
        Random e = new Random();
        int n=0;
        StringBuilder id = new StringBuilder("#_");
        for(int i = 0;i < 10;i++){
            n = e.nextInt(str.length-1);
            id.append(str[n]);
        }

        return id.toString();
    }
}
