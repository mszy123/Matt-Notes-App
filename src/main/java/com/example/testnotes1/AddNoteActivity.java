package com.example.testnotes1;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toolbar;

import com.afollestad.materialdialogs.MaterialDialog;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipDrawable;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.afollestad.materialdialogs.MaterialDialog;
import android.view.MenuItem;




public class AddNoteActivity extends AppCompatActivity implements MenuItem.OnMenuItemClickListener {

    private EditText noteTitleEditText;
    private EditText noteContentEditText;
    private Button saveNoteButton;

    private TextView addTag;

    private NoteDatabase notesDbHelper;
    private SQLiteDatabase notesDb;

    private boolean isPinned = false;

    private Note noteToEdit;





    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_note);

        saveNoteButton = findViewById(R.id.save_note_button);
        addTag = findViewById(R.id.add_new_tag);

        noteTitleEditText = findViewById(R.id.note_title_edit_text);
        noteContentEditText = findViewById(R.id.note_content_edit_text);

        MaterialToolbar topAppBar = findViewById(R.id.top_app_bar);
        setSupportActionBar(topAppBar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        notesDbHelper = new NoteDatabase(this);
        notesDb = notesDbHelper.getWritableDatabase();


        // Get the note to edit from the intent
        Intent intent = getIntent();

        long noteIdToEdit = intent.getLongExtra("note_id", -1);
        Log.d("intent", ""+noteIdToEdit);
        if (noteIdToEdit != -1) {
            noteToEdit = notesDbHelper.getNoteById(noteIdToEdit);
            // Set the note title and content
            Log.d("Tag",noteToEdit.getTitle());
            noteTitleEditText.setText(noteToEdit.getTitle());
            noteContentEditText.setText(noteToEdit.getContent());
            // Set the value of isPinned based on the note
            isPinned = noteToEdit.getPinned();

        }





        // Set up save note button
        saveNoteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // TODO: Save the note
                // Get the note title and content from the EditText views
                EditText noteTitleEditText = findViewById(R.id.note_title_edit_text);
                EditText noteContentEditText = findViewById(R.id.note_content_edit_text);
                String noteTitle = noteTitleEditText.getText().toString();
                String noteContent = noteContentEditText.getText().toString();

                Calendar calendar = Calendar.getInstance();
                Date date = calendar.getTime();
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String currentDateAndTime = dateFormat.format(date);
                Log.d("Saving","Saving...");

                // Insert the note into the database
                // Save the note
                if (noteToEdit != null) {
                    // If we're editing an existing note, update it in the database
                    noteToEdit.setTitle(noteTitle);
                    noteToEdit.setContent(noteContent);
                    noteToEdit.setPinned(isPinned);
                    noteToEdit.setTimeStamp(currentDateAndTime);
                    notesDbHelper.updateNote(noteToEdit);
                } else {
                    // If we're saving a new note, create a new Note object and insert it into the database
                    Note myNote = new Note(noteTitle, noteContent, currentDateAndTime, isPinned);
                    if (myNote.getTitle().isEmpty() && myNote.getContent().isEmpty()){
                        //do something
                    }
                    else{
                        long newId = notesDbHelper.insertNote(myNote);
                        myNote.setId(newId);
                    }
                }





                // Set the result and finish the activity
                Intent returnIntent = new Intent();
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });

        final EditText input = new EditText(AddNoteActivity.this);


        LinearLayout chipLayout = findViewById(R.id.tag_linearlayout);
        ChipGroup chipGroup = findViewById(R.id.add_chip_group);


        addTag.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // do something when the TextView is clicked
                new MaterialAlertDialogBuilder(AddNoteActivity.this)
                        .setTitle("Add Tag")
                        .setView(R.layout.dialog_add_tag)
                        .setPositiveButton("Add", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // do something when positive button is clicked
                                // get the text from the input field
                                EditText editText = ((AlertDialog) dialogInterface).findViewById(R.id.editTextTag);
                                String tag = editText.getText().toString();
                                // do something with the tag
                                Chip newTag = new Chip(AddNoteActivity.this);
                                newTag.setText(tag);
                                chipLayout.addView(newTag);




                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            }
        });


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        switch (item.getItemId()) {
            case R.id.pin:
                // Toggle the value of isPinned
                isPinned = !isPinned;
                // Change the icon of the menu item based on isPinned
                if (isPinned) {
                    isPinned = true;
                    item.setIcon(R.drawable.baseline_push_pin_24);
                } else {
                    isPinned = false;
                    item.setIcon(R.drawable.outline_push_pin_24);
                }
                return true;
            case R.id.reminder:
                // Handle reminder item click
                return true;
            default:
                return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_app_bar, menu);
        MenuItem pinItem = menu.findItem(R.id.pin);
        if (isPinned) {
            pinItem.setIcon(R.drawable.baseline_push_pin_24); // set the pin icon to a different source
        }
        return true;
    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        // Handle menu item clicks here
        switch (item.getItemId()) {
            case R.id.pin:
                // Handle pin menu item click
                return true;
            case R.id.reminder:
                // Handle reminder menu item click
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

}

