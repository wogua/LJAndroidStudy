package com.lijun.androidstudy.floatmeltitask;

/**
 * filename:LrcContent.java
 * Copyright MALATA ,ALL rights reserved.
 * 15-7-16
 * author: laiyang
 * <p>
 * The class is java bean,to save lrc's content and time
 * <p>
 * Modification History
 * -----------------------------------
 * <p>
 * -----------------------------------
 */
public class LrcContent {
    /**
     * lrc content
     */
    private String content;

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    /**
     * mills of lrc time
     */
    private int time;

}
