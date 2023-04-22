package com.qc.casserver.utils;


import com.qc.casserver.common.CustomException;

/**
 * 参数校验工具类
 */
public class ParamsCalibration {

    public static void checkSensitiveWords(String word){
        String sensitiveWords = "admin,妈,爸,爹,爷,妈妈,爷爷,爸爸,admins,Admin,ADmin,ADMin,ADMIn,ADMIN,Root,root,ROOT,ROOt,name,<,>";
        if (sensitiveWords.contains(word)){
            throw new CustomException("err: 敏感词");

        }
    }

    /**
     * @param username
     * @return 0:账户密码登录 1:邮箱登录
     */
    public static int booleanLoginType(String username){

        String em = "^\\w+([-+.]\\w+)*@\\w+([-.]\\w+)*\\.\\w+([-.]\\w+)*$";
        if (username.matches(em)){
            return 1;
        }else {
            return 0;
        }
    }

    public static boolean checkIsWord(String name){
        if (name.equals("doc")){
            return true;
        } else if (name.equals("docx")) {
            return true;
        }else {
            return false;
        }
    }
    public static boolean checkIsPdf(String name){
        if (name.equals("pdf")){
            return true;
        } else {
            return false;
        }
    }
    public static boolean checkIsCanPrint(String name){
       if (checkIsPdf(name)){
           return true;
       } else if (checkIsWord(name)) {
           return true;
       }else {
           return false;
       }
    }

    /**
     * 双面打印的singleDocumentPaperUsage在此处变更
     * @param oldName
     * @param url
     * @param printingDirection
     * @param copies
     * @param printBigValue
     * @param needPrintPagesEndIndex
     * @param isDuplex
     * @param originFilePages
     * @param singleDocumentPaperUsage
     * @param userId
     * @return
     */

}
