package com.lijun.androidstudy.chinesematch;

import net.sourceforge.pinyin4j.PinyinHelper;
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType;
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat;
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType;
import net.sourceforge.pinyin4j.format.exception.BadHanyuPinyinOutputFormatCombination;

import java.util.ArrayList;

/**
 * 处理中文模糊匹配的工具类
 */
public class ChineseMatchUtil {
    static HanyuPinyinOutputFormat sFormat;

    private static String[] sNumberPinyin = {"ling", "yi", "er", "san", "si", "wu", "liu", "qi", "ba", "jiu"};

    static {
        sFormat = new HanyuPinyinOutputFormat();
        sFormat.setCaseType(HanyuPinyinCaseType.LOWERCASE);
        sFormat.setToneType(HanyuPinyinToneType.WITHOUT_TONE);
    }

    /**
     * 返回汉字的拼音模糊组合字符串，多音字，前后鼻音，平舌翘舌
     *
     * @param chineseStr 需要转换的汉字，如“张朝阳”
     * @return 返回处理后的结果，如"zhangchaoyang|zhangcaoyang|zhangzhaoyang|zhangzaoyang..."
     */
    public static String getPinyinSimilary(String chineseStr) {
        if (chineseStr == null || chineseStr.length() <= 0) return null;
        final int inputLength = chineseStr.length();
        ArrayList<ArrayList<String>> all = new ArrayList<ArrayList<String>>();
        int[] eachLength = new int[inputLength];
        for (int i = 0; i < inputLength; i++) {
            final char character = chineseStr.charAt(i);
            ArrayList<String> ps = getSinglePinyinList(character);
            all.add(ps);
            eachLength[i] = ps.size();
        }

        ArrayList<String> last = all.get(0);
        int tempIndex = 0;
        while (tempIndex < inputLength - 1) {
            last = exchangeXY(last, all.get(tempIndex + 1));
            tempIndex++;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < last.size(); i++) {
            sb.append(last.get(i) + "|");
        }
        return sb.toString();
    }

    /**
     * 将两个列表元素一一结合成行的列表
     *
     * @param a
     * @param b
     * @return
     */
    private static ArrayList<String> exchangeXY(ArrayList<String> a, ArrayList<String> b) {
        final int sizeA = a.size();
        final int sizeB = b.size();
        ArrayList<String> result = new ArrayList<String>();
        for (int i = 0; i < sizeA; i++) {
            final String s = a.get(i);
            for (int j = 0; j < sizeB; j++) {
                result.add(s + b.get(j));
            }
        }
        return result;
    }

    public static ArrayList<String> getSinglePinyinList(final char c) {
        String[] ss = null;//如果是多音字，则长度大于一
        ArrayList<String> pyList = new ArrayList<String>();

        String notChinese = null;
        notChinese = parseNotChinese(c);
        if (notChinese != null) {//数字，包括汉字数字，阿拉伯数字
            pyList.add(notChinese);
            return pyList;
        }

        try {
            ss = PinyinHelper.toHanyuPinyinStringArray(c, sFormat);
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }

        if (ss != null && ss.length > 0) {
            final int sLength = ss.length;
            for (int i = 0; i < sLength; i++) {
                pyList.addAll(replaceTailStringList(ss[i]));
            }
        }
        return pyList;
    }

    /**
     * 返回拼音的模糊匹配组合
     *
     * @param strPinyin
     * @return
     */
    private static ArrayList<String> replaceTailStringList(final String strPinyin) {
        ArrayList<String> result = new ArrayList<String>();
        result.add(strPinyin);
        if (strPinyin.contains("ang")) {
            result.add(strPinyin.replace("ang", "an"));
        } else if (strPinyin.contains("eng")) {
            result.add(strPinyin.replace("eng", "en"));
        } else if (strPinyin.contains("ing")) {
            result.add(strPinyin.replace("ing", "in"));
        } else if (strPinyin.contains("an")) {
            result.add(strPinyin.replace("an", "ang"));
        } else if (strPinyin.contains("en")) {
            result.add(strPinyin.replace("en", "eng"));
        } else if (strPinyin.contains("in")) {
            result.add(strPinyin.replace("in", "ing"));
        }

        if (strPinyin.startsWith("ch")) {
            result.add(strPinyin.replace("ch", "c"));
        } else if (strPinyin.startsWith("zh")) {
            result.add(strPinyin.replace("zh", "z"));
        } else if (strPinyin.startsWith("c")) {
            result.add(strPinyin.replace("c", "ch"));
        } else if (strPinyin.startsWith("z")) {
            result.add(strPinyin.replace("z", "zh"));
        }else if (strPinyin.startsWith("l")) {
            result.add(strPinyin.replace("l", "n"));
        } else if (strPinyin.startsWith("n")) {
            result.add(strPinyin.replace("n", "l"));
        }
        return result;
    }

    /**
     * 获取汉字的拼音，只返回一总结果，用于语音返回的联系人
     *
     * @return
     */
    public static String getFullPinYinList(String str) {
        String mainPinyinStrOfChar = null;
        try {
            mainPinyinStrOfChar = PinyinHelper.toHanyuPinyinString(str, sFormat, "");
        } catch (BadHanyuPinyinOutputFormatCombination badHanyuPinyinOutputFormatCombination) {
            badHanyuPinyinOutputFormatCombination.printStackTrace();
        }

        return mainPinyinStrOfChar.toString().toLowerCase();
    }

    /**
     * 1.判断是否为数字（包括汉字数字，阿拉伯数字）,如果是，统一返回阿拉伯数字；如果不是，返回空
     * 2.判断是否为英文字母，如果是，则返回其大写
     *
     * @param c
     * @return
     */
    private static String parseNotChinese(char c) {
        if (Character.isDigit(c)) {
            int intNum = c - '0';
            return sNumberPinyin[intNum];
        } else if (c >= 'A' && c <= 'Z') {
            return String.valueOf(c).toLowerCase();
        } else if (c >= 'a' && c <= 'z') {
            return String.valueOf(c);
        }
        return null;
    }
}