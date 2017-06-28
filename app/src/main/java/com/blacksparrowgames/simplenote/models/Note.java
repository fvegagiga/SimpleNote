package com.blacksparrowgames.simplenote.models;

import android.database.Cursor;

public class Note {

    private long id;
    private String title;
    private long lastTime;
    private String comment;
    private String imagePath;

    public Note(long id, long lastTime, String title, String comment, String imagePath) {
        this.id = id;
        this.lastTime = lastTime;
        this.title = title;
        this.comment = comment;
        this.imagePath = imagePath;
    }

    public long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getComment() {
        return comment;
    }

    public String getImagePath() {
        return imagePath;
    }

    public static Note cursorToNote(Cursor cursor) {
        Note note = new Note(cursor.getLong(0),
                cursor.getLong(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4));
        return note;
    }
}
