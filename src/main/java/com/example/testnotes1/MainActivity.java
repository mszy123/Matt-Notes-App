package com.example.testnotes1;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import android.widget.ImageView;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.core.view.WindowCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testnotes1.databinding.ActivityMainBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.widget.Toolbar;


public class MainActivity extends AppCompatActivity implements NoteAdapter.OnNoteClickListener {

    private RecyclerView recyclerView;
    private NoteAdapter noteAdapter;
    private ArrayList<Note> noteList = new ArrayList<>();

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;

    private NoteDatabase noteDb;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), false);
        super.onCreate(savedInstanceState);


        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set up the database
        noteDb = new NoteDatabase(this);

        //toolbar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(""); // set the title to an empty string
        }



        // Set up RecyclerView
        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        noteAdapter = new NoteAdapter(noteList, this);
        recyclerView.setAdapter(noteAdapter);

        //add divider
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, R.color.mainGreen, 1);
        recyclerView.addItemDecoration(dividerItemDecoration);


        // Set up FloatingActionButton
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
            startActivity(intent);
        });

        loadNotes();



    }


    private void loadNotes() {
        SQLiteDatabase db = noteDb.getReadableDatabase();
        String[] projection = {
                NoteContract.NoteEntry._ID,
                NoteContract.NoteEntry.COLUMN_TITLE,
                NoteContract.NoteEntry.COLUMN_CONTENT,
                NoteContract.NoteEntry.COLUMN_TIMESTAMP,
                NoteContract.NoteEntry.COLUMN_IS_PINNED
        };
        Cursor cursor = db.query(
                NoteContract.NoteEntry.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                NoteContract.NoteEntry.COLUMN_TIMESTAMP + " DESC"
        );
        noteList.clear();
        List<Note> pinnedNotes = new ArrayList<>();
        List<Note> unpinnedNotes = new ArrayList<>();
        while (cursor.moveToNext()) {
            long id = cursor.getLong(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry._ID));
            String title = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_TITLE));
            String content = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_CONTENT));
            String timestamp = cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_TIMESTAMP));
            boolean isPinned = Boolean.parseBoolean(cursor.getString(cursor.getColumnIndexOrThrow(NoteContract.NoteEntry.COLUMN_IS_PINNED)));
            Note note = new Note(title, content, timestamp, isPinned);
            note.setId(id);
            if (isPinned) {
                pinnedNotes.add(note);
            } else {
                unpinnedNotes.add(note);
            }
        }
        cursor.close();
        // Sort the pinned notes by timestamp in descending order
        pinnedNotes.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
        // Sort the unpinned notes by timestamp in descending order
        unpinnedNotes.sort((n1, n2) -> n2.getTimestamp().compareTo(n1.getTimestamp()));
        // Combine the pinned and unpinned notes, with pinned notes at the top
        noteList.clear();
        noteList.addAll(pinnedNotes);
        noteList.addAll(unpinnedNotes);
        noteAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotes();
        noteAdapter.notifyDataSetChanged();
        Log.d("MainActivity", "RecyclerView updated with " + noteList.size() + " notes.");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            loadNotes();
        }
    }

    private void saveNote(Note note) {
        SQLiteDatabase db = noteDb.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteEntry.COLUMN_TITLE, note.getTitle());
        values.put(NoteContract.NoteEntry.COLUMN_CONTENT, note.getContent());
        db.insert(NoteContract.NoteEntry.TABLE_NAME, null, values);
    }

    public void addNoteToList(Note note) {
        noteList.add(note);
        noteAdapter.notifyDataSetChanged();
        saveNote(note);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        //getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onNoteLongClick(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this note?");
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                // Remove the note from the list
                Note noteToDelete = noteList.get(position);
                noteList.remove(position);

                // Update the RecyclerView with the new data
                noteAdapter.notifyDataSetChanged();

                noteDb.deleteNote(noteToDelete.getId());
                // Save the updated note list to SharedPreferences
                //saveNote(po);

                // Notify the user that the note has been deleted
                Toast.makeText(MainActivity.this, "Note deleted", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("No", null);
        builder.show();
    }


    @Override
    public void onNoteClick(int position) {
        // Implement your code here when a note is clicked
        Intent intent = new Intent(MainActivity.this, AddNoteActivity.class);
        Log.d("position", " " + position);
        long id = noteList.get(position).getId();
        intent.putExtra("note_id", id);
        startActivity(intent);

    }


    public void updateDatabase(Note note) {
        SQLiteDatabase db = noteDb.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(NoteContract.NoteEntry.COLUMN_TITLE, note.getTitle());
        values.put(NoteContract.NoteEntry.COLUMN_CONTENT, note.getContent());
        values.put(NoteContract.NoteEntry.COLUMN_TIMESTAMP, note.getTimestamp());
        String selection = NoteContract.NoteEntry._ID + " = ?";
        String[] selectionArgs = { String.valueOf(note.getId()) };
        int count = db.update(
                NoteContract.NoteEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs
        );
        Log.d("NoteDatabase", "Rows updated: " + count);
    }


}
