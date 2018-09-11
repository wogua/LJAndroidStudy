package com.lijun.androidstudy.chinesematch;

import android.util.Log;

import net.sourceforge.pinyin4j.PinyinHelper;

import java.util.ArrayList;

/**
 * 处理中文模糊匹配的工具类
 */
public class ChineseMatchUtil {
    /**
     * 获取拼音
     * @param displayName
     * @return
     */
    public static ArrayList<String> getFullPinYinList(String displayName) {
        if (displayName == null) return null;
        ArrayList<HanziToPinyin.Token> tokens = HanziToPinyin.getInstance().get(displayName);
        if (tokens != null && tokens.size() > 0) {
            ArrayList<String> pinyinList = new ArrayList<String>();
            for (HanziToPinyin.Token token : tokens) {
                if (HanziToPinyin.Token.PINYIN == token.type) {
                    pinyinList.add(token.target);
                }
            }
            return pinyinList;
        }
        return null;
    }

    /**
     * 获取拼音首字母，全拼的组合
     * @param spChinese
     * @return
     */
    public static String getRegExpBySpChinese(CharSequence spChinese) {
        ArrayList<String> pinyinList = getFullPinYinList(spChinese.toString());
        if (pinyinList != null && pinyinList.size() > 0) {
            int pinyinCount = pinyinList.size();
            ArrayList<String> regExpList = new ArrayList<String>();
            StringBuilder finalRegExpBuilder = new StringBuilder();
            finalRegExpBuilder.append("(");
            for (int current = 0; current < pinyinCount; current++) {
                String pinyin = pinyinList.get(current);
                int size = pinyin.length();
                if (size > 0) {
                    StringBuilder regExpBuilder = new StringBuilder();
                    pinyin = pinyin.toLowerCase();
                    regExpBuilder.append("(");
                    for (int i = 1; i <= size; i++) {
                        regExpBuilder.append(pinyin.substring(0, i));
                        if (i < size) {
                            regExpBuilder.append("|");
                        }
                    }
                    regExpBuilder.append(")");
                    regExpList.add(regExpBuilder.toString());

                    finalRegExpBuilder.append(regExpBuilder.toString());
                    finalRegExpBuilder.append("|");
                    int regExpSize = regExpList.size();
                    int end = current + 1;
                    if (regExpSize > 1) {
                        for (int index = 0; index < end - 1; index++) {
                            finalRegExpBuilder.append("(");
                            finalRegExpBuilder.append(getStringByList(regExpList, index, end));
                            finalRegExpBuilder.append(")");
                            finalRegExpBuilder.append("|");
                        }
                    }
                }
            }
            finalRegExpBuilder.deleteCharAt(finalRegExpBuilder.length() - 1);
            finalRegExpBuilder.append(")");

            return finalRegExpBuilder.toString();
        }

        return null;
    }

    public static String getStringByList(ArrayList<String> regExpList, int start, int end) {

        StringBuilder subBuilder = new StringBuilder();
        for (int i = start; i < end; i++) {
            subBuilder.append(regExpList.get(i));
        }
        return subBuilder.toString();
    }

    public static String[] toHanyuPinyinStringArray(char spChinese){
        String[] cStrHY = PinyinHelper.toHanyuPinyinStringArray(spChinese);
        for (int i = 0; i < cStrHY.length; i++) {
            Log.i("pinyin", cStrHY[i]);
        }
        return cStrHY;
    }

}