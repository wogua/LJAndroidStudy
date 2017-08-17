package com.lijun.androidstudy.floatmeltitask;

/**
 * filename:NoteContent.java
 * Copyright MALATA ,ALL rights reserved.
 * 15-7-20
 * author: laiyang
 * <p>
 * java bean,to save note
 * <p>
 * Modification History
 * -----------------------------------
 * <p>
 * -----------------------------------
 */
public class NoteContent {
    /**
     * note id in db
     */
    private int id;
    /**
     * note content
     */
    private String content;
    /**
     * write note time
     */
    private String time;

    public NoteContent(int id, String content, String time) {
        this.id = id;
        this.content = content;
        this.time = time;
    }

    public NoteContent(String content, String time) {
        this.content = content;
        this.time = time;

    }

    public NoteContent() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }
}
