package com.example.xtremednotes;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.xtremednotes.model.Note;

import java.util.ArrayList;
import androidx.recyclerview.widget.RecyclerView;

public class NotesAdapter extends RecyclerView.Adapter<NotesAdapter.ViewHolder> {

    private ArrayList<Note> localDataSet;
    private OnItemClickListener listener;
    private OnLongClickListener listenerLong;

    public NotesAdapter(ArrayList<Note> notesList, OnItemClickListener listener,
                        OnLongClickListener listenerLong) {
        this.localDataSet = notesList;
        this.listener = listener;
        this.listenerLong = listenerLong;
    }

    public interface OnItemClickListener {
        void onItemClick(Note note);
    }

    public interface OnLongClickListener{
        void onLongItemClick(NotesAdapter ad, Note note);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView imageView;

        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.noteText);
            imageView = (ImageView) view.findViewById(R.id.noteImage);
        }

        public void bind(final Note note, final OnItemClickListener listener,
                         final OnLongClickListener listenerLong) {
            String visibleTitle = note.getTitle().substring(0, note.getTitle().lastIndexOf("."));
            if(visibleTitle.length() > 7){
                visibleTitle = visibleTitle.substring(0,7)+"...";
            }

            textView.setText(visibleTitle);
            imageView.setImageResource(note.getImg());
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    listener.onItemClick(note);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener(){
                @Override
                public boolean onLongClick(View view) {
                    listenerLong.onLongItemClick(NotesAdapter.this, note);
                    return true;
                }
            });
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.list_item, viewGroup, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.bind(localDataSet.get(position), listener, listenerLong);
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }
}
