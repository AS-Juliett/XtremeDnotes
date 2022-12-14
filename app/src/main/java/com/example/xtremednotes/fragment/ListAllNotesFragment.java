package com.example.xtremednotes.fragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.example.xtremednotes.EncryptedFileManager;
import com.example.xtremednotes.util.ConfigUtil;
import com.example.xtremednotes.util.FileUtil;
import com.example.xtremednotes.adapter.NotesAdapter;
import com.example.xtremednotes.R;
import com.example.xtremednotes.activity.EditNoteActivity;
import com.example.xtremednotes.activity.ImportActivity;
import com.example.xtremednotes.model.Note;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Collectors;

public class ListAllNotesFragment extends Fragment {

    private View view;
    private ArrayList<Note> notesList;
    private NotesAdapter notesAdapter;
    private String filter;
    private String exportName = "export";

    public ListAllNotesFragment(String filter){
        this.filter = filter;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_list_all_notes, container,false);
        setHasOptionsMenu(true);

        FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.floatingActionButtonCreate);
        button.setOnClickListener(v -> openNote(null, null));

        notesList = new ArrayList<>();
        loadAllFiles();

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new GridLayoutManager(view.getContext(),3));

        notesAdapter = new NotesAdapter(notesList,
                note -> openNote(note.getTitle(), note.getFolder()),
                (adapter, note) -> {
                    AlertDialog.Builder builder = new AlertDialog.Builder(view.getContext());

                    builder.setTitle("Confirm");
                    builder.setMessage("Delete " + note.getTitle() + "?");
                    builder.setPositiveButton("Yes", (dialog, which) -> {

                        File file = new File(getActivity().getFilesDir(), note.getFullName());
                        if(file.exists()){
                            file.delete();
                            Toast.makeText(view.getContext(), note.getTitle()+" deleted",
                                    Toast.LENGTH_SHORT).show();
                            notesList.remove(note);
                            adapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("No", null);
                    AlertDialog alert = builder.create();
                    alert.show();
                });
        recyclerView.setAdapter(notesAdapter);
        return view;
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_import:
                onClickFileImport();
                return(true);
            case R.id.action_export:
                onClickFileExport();
                return(true);
        }
        return(super.onOptionsItemSelected(item));
    }

    public void onClickFileImport() {
        Intent intent = new Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT);

        startActivityForResult(Intent.createChooser(intent, "Select a file"), 123);
    }

    public void onClickFileExport() {
        EditText et = new EditText(view.getContext());
        et.setText(exportName);
        new android.app.AlertDialog.Builder(view.getContext())
                .setTitle("Save as")
                .setView(et)
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        exportName = et.getText().toString();
                        Intent intent = new Intent()
                                .setAction(Intent.ACTION_OPEN_DOCUMENT_TREE);

                        startActivityForResult(Intent.createChooser(intent, "Select a folder"), 125);
                    }
                })
                .setNegativeButton("Cancel", null)
                .create()
                .show();
    }

    private void openNote(String noteName, String folderName) {
        Intent i = new Intent(view.getContext(), EditNoteActivity.class);
        if (noteName != null) {
            i.putExtra(EditNoteActivity.EDIT_NAME_KEY, noteName);
            i.putExtra(EditNoteActivity.FOLDER_KEY, folderName);
        }
        startActivityForResult(i, 77);
    }

    private void loadAllFiles(){
        notesList.clear();
        String defaultDir = "Uncategorized";
        ArrayList<File> folders = new ArrayList<>();

        if(filter == null){
            String[] allFolders = ConfigUtil.convertObjectBase64(ConfigUtil.getFolders(getActivity()));
            for(String folder: allFolders){
                File f = new File(getActivity().getFilesDir(), folder);
                folders.add(f);
            }
            folders.add(getActivity().getFilesDir());
        } else if(filter.equals(defaultDir)){
            folders.add(getActivity().getFilesDir());
        } else {
            File f = new File(getActivity().getFilesDir(), ConfigUtil.encodeBase64(filter));
            folders.add(f);
        }

        for(File fld: folders){
            File[] fileList = fld.listFiles();
            File[] files = Arrays.stream(fileList != null ? fileList : new File[]{})
                    .filter(f -> f.getName().endsWith(".txt"))
                    .collect(Collectors.toList()).toArray(new File[]{});
            for (int i = 0; i < files.length; i++) {
                notesList.add(new Note(FileUtil.toNoteName(files[i].getName()), fld.getName()));
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 123 && resultCode == Activity.RESULT_OK) {
            String selectedFile = data.getData().getPath();
            Intent i = new Intent(view.getContext(), ImportActivity.class);
            if (selectedFile.contains(":") && selectedFile.endsWith(EncryptedFileManager.ARCHIVE_EXT)) {
                i.putExtra(ImportActivity.FILENAME_KEY, selectedFile.split(":")[1]);
                startActivity(i);
            }
        } else if(requestCode == 77 && resultCode == Activity.RESULT_OK){
            String noteTitle = data.getStringExtra("NOTE_TITLE");
            String noteFolder = data.getStringExtra("NOTE_FOLDER");
            notesList.add(new Note(noteTitle, noteFolder));
            notesAdapter.notifyDataSetChanged();
        } else if (requestCode == 125 && resultCode == Activity.RESULT_OK) {
            Uri uri = data.getData();
            String filePath = FileUtil.getRealPath(uri);
            String message = "No write permission";
            if (filePath != null && new File(filePath).canWrite()) {
                String fullPath = filePath + File.separator + exportName + ".zip";
                EncryptedFileManager.getInstance().export(view.getContext(), fullPath);
                message = "Successfully writing " + fullPath;
            }
            Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAllFiles();
        notesAdapter.notifyDataSetChanged();
    }
}