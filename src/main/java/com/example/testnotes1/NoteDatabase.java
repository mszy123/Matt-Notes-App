package com.example.testnotes1;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.Arrays;

public class NoteDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "notes.db";
    private static final int DATABASE_VERSION = 1;

    public NoteDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTable = "CREATE TABLE " + NoteContract.NoteEntry.TABLE_NAME + " ("
                + NoteContract.NoteEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + NoteContract.NoteEntry.COLUMN_TITLE + " TEXT,"
                + NoteContract.NoteEntry.COLUMN_CONTENT + " TEXT,"
                + NoteContract.NoteEntry.COLUMN_IS_PINNED + " TEXT,"
                + NoteContract.NoteEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                + ");";
        db.execSQL(createTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + NoteContract.NoteEntry.TABLE_NAME);
        onCreate(db);
    }

    public void deleteNote(long id) {
        Log.d("Hello","Id: "+ id);
        SQLiteDatabase db = this.getWritableDatabase();
        String selection = NoteContract.NoteEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        int deletedRows = db.delete(NoteContract.NoteEntry.TABLE_NAME, selection, selectionArgs);
        db.close();
        Log.d("Rows","Deleted Rows" + deletedRows);
        if (deletedRows > 0) {
            Log.d("Tag", "Note Deleted");
        }
    }

    public long insertNote(Note note) {
        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteEntry.COLUMN_TITLE, note.getTitle());
        values.put(NoteContract.NoteEntry.COLUMN_CONTENT, note.getContent());
        values.put(NoteContract.NoteEntry.COLUMN_IS_PINNED, Boolean.toString(note.getPinned()));
        values.put(NoteContract.NoteEntry.COLUMN_TIMESTAMP, note.getTimestamp());

        SQLiteDatabase db = getWritableDatabase();
        long id = db.insert(NoteContract.NoteEntry.TABLE_NAME, null, values);
        db.close();

        return id;
    }

    public Note getNoteById(long id) {
        SQLiteDatabase db = getReadableDatabase();
        String[] projection = {
                NoteContract.NoteEntry._ID,
                NoteContract.NoteEntry.COLUMN_TITLE,
                NoteContract.NoteEntry.COLUMN_CONTENT,
                NoteContract.NoteEntry.COLUMN_IS_PINNED,
                NoteContract.NoteEntry.COLUMN_TIMESTAMP
        };
        String selection = NoteContract.NoteEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(id) };
        Cursor cursor = db.query(
                NoteContract.NoteEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                null
        );
        Note note = null;
        if (cursor.moveToFirst()) {
            String title = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_TITLE));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_CONTENT));
            boolean isPinned = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_IS_PINNED)));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_TIMESTAMP));
            note = new Note(title, content, timestamp, isPinned);
            note.setId(cursor.getLong(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry._ID)));
        }
        cursor.close();
        db.close();
        return note;
    }

    public void updateNote(Note updatedNote) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteEntry.COLUMN_TITLE, updatedNote.getTitle());
        values.put(NoteContract.NoteEntry.COLUMN_CONTENT, updatedNote.getContent());
        values.put(NoteContract.NoteEntry.COLUMN_IS_PINNED, Boolean.toString(updatedNote.getPinned()));
        values.put(NoteContract.NoteEntry.COLUMN_TIMESTAMP, updatedNote.getTimestamp());

        String selection = NoteContract.NoteEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(updatedNote.getId()) };

        int updatedRows = db.update(NoteContract.NoteEntry.TABLE_NAME, values, selection, selectionArgs);
        db.close();

        if (updatedRows > 0) {
            Log.d("Tag", "Note updated");
        }
    }

}

