package com.lijun.androidstudy.floatmeltitask;

import com.lijun.androidstudy.R;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * filename:LrcReader.java
 * Copyright MALATA ,ALL rights reserved.
 * 15-7-16
 * author: laiyang
 * <p>
 * read lrc files content,prase to list
 * <p>
 * Modification History
 * -----------------------------------
 * <p>
 * -----------------------------------
 */
public class LrcReader {

    public static List<LrcContent> getLrcContents(String filePath) throws IOException {
        List<LrcContent> lrcContents = new ArrayList<LrcContent>();
        LrcContent lrcLine;
        filePath = filePath.substring(0, filePath.lastIndexOf(".")) + ".lrc";// according to music name
        File lrcFile = new File(filePath);
        if (lrcFile.exists() && lrcFile.isFile()) {// if has lrc file
            String line;
            FileInputStream fis;
            BufferedInputStream bis;
            BufferedReader reader;
            fis = new FileInputStream(lrcFile);
            bis = new BufferedInputStream(fis);
            bis.mark(4);
            byte[] first3bytes = new byte[3];
            //read file encoding type
            bis.read(first3bytes);
            bis.reset();
            if (first3bytes[0] == (byte) 0xEF && first3bytes[1] == (byte) 0xBB
                    && first3bytes[2] == (byte) 0xBF) {// utf-8
                reader = new BufferedReader(new InputStreamReader(bis, "utf-8"));
            } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFE) {
                reader = new BufferedReader(new InputStreamReader(bis, "unicode"));
            } else if (first3bytes[0] == (byte) 0xFE && first3bytes[1] == (byte) 0xFF) {
                reader = new BufferedReader(new InputStreamReader(bis, "utf-16be"));
            } else if (first3bytes[0] == (byte) 0xFF && first3bytes[1] == (byte) 0xFF) {
                reader = new BufferedReader(new InputStreamReader(bis, "utf-16le"));
            } else {
                reader = new BufferedReader(new InputStreamReader(bis, "GBK"));
            }
            while (null != (line = reader.readLine())) {// read line to praser LrcContent
                lrcLine = new LrcContent();
                line = line.replace("[", "");
                String[] datas = line.split("]", 2);
                int time = getTimesInMills(datas[0]);
                if (-1 == time) {
                    continue;
                }
                lrcLine.setTime(time);
                lrcLine.setContent(datas[1]);
                lrcContents.add(lrcLine);
            }
            reader.close();
            bis.close();
            fis.close();
        }
        return lrcContents;
    }

    /**
     * read lrc time and format to mills
     *
     * @param data
     * @return
     */
    private static int getTimesInMills(String data) {
        data = data.replace(":", ".");
        data = data.replace(".", "@");
        // split �?�?false
        String timeData[] = data.split("@");
        if (timeData.length <= 2) {
            return -1;
        }
        int minute = Integer.parseInt(timeData[0]);
        int second = Integer.parseInt(timeData[1]);
        int mills = Integer.parseInt(timeData[2]);

        return (minute * 60 + second) * 1000 + mills * 10;
    }
}
