package com.example.speaktext.util;

import android.content.Context;
import android.widget.Toast;

public class Util {
    public static boolean isNumeric(String data){
        String regex = "-?\\d+(\\.\\d+)?";
        boolean flag = false;
        if (data.matches(regex))
            flag = true;
        return flag;
    }
    public static void alertMsg(String msg, Context context){
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }
    static String getTagContent(String psHtml, String psFind){
        String vsContent = "", vsChar;
        int vPosB, vPosE, vCountTagB = 0, vPosTagE = 0;
        vPosB = psHtml.indexOf(psFind);
        if(vPosB >= 0) {
            vPosB = psHtml.substring(0, vPosB).lastIndexOf("<");
            if(vPosB >= 0)
                psHtml = psHtml.substring(vPosB);
        }
        vPosB = psHtml.indexOf("<");
        if(vPosB >= 0)
            vCountTagB++;
        while (vPosB >= 0){
            vPosB = psHtml.indexOf("<", vPosB + 1);
            if(vPosB >= 0) {
                vsChar = psHtml.substring(vPosB + 1,vPosB + 2);
                if(vsChar.equals("/")){
                    vCountTagB--;
                    if(vCountTagB == 0) {
                        vPosTagE = vPosB - 1;
                        break;
                    }
                }else {
                    vsChar = psHtml.substring(vPosB, vPosB + 3);
                    if(!vsChar.equalsIgnoreCase("<br"))
                        vCountTagB++;
                }
            }else
                break;
        }
        if(vPosTagE > 0)
            psHtml = psHtml.substring(0, vPosTagE);
        return psHtml;
    }
    static String cutTag(String psContent, String psTagB, String psTagE){
        int vPosB, vPosE, vLen;
        vLen = psTagE.length();
        vPosB = psContent.indexOf(psTagB);
        while(vPosB >= 0){
            vPosE = psContent.indexOf(psTagE, vPosB);
            if(vPosE >= 0) {
                psContent = psContent.substring(0, vPosB) + psContent.substring(vPosE + vLen);
                vPosB = psContent.indexOf(psTagB);
            }else
                break;
        }
        return psContent;
    }

    ///xoá ký tự script khác trong nội dung
    public static String parserContent(String psContent, String psClass){
        int vPosB;
        String vsScriptB = "<script", vsScriptE = "</script>";
        if(psClass != null && !psClass.isEmpty())
            psContent = getTagContent(psContent, psClass);
        psContent = cutTag(psContent, vsScriptB, vsScriptE);
        psContent = cutTag(psContent, "<", ">");
        return psContent;
    }
}
