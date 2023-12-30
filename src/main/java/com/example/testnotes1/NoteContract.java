package com.example.testnotes1;

import android.provider.BaseColumns;

//Create a new class called NoteContract to define the schema of the notes table:
public final class NoteContract {

    private NoteContract() {}

    public static class NoteEntry implements BaseColumns {
        public static final String TABLE_NAME = "notes";
        public static final String COLUMN_TITLE = "title";
        public static final String COLUMN_CONTENT = "content";
        public static final String COLUMN_TIMESTAMP = "timestamp";

        public static final String COLUMN_IS_PINNED = "isPinned";
    }
}

