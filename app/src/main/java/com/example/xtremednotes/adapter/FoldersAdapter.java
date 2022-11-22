package com.example.xtremednotes.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.xtremednotes.R;
import com.example.xtremednotes.model.Folder;
import com.example.xtremednotes.util.ConfigUtil;

import java.util.ArrayList;

public class FoldersAdapter extends RecyclerView.Adapter<FoldersAdapter.ViewHolder> {
    private ArrayList<Folder> localDataSet;
    private FoldersAdapter.OnItemClickListener listener;
    private FoldersAdapter.OnLongClickListener listenerLong;

    public FoldersAdapter(ArrayList<Folder> foldersList, FoldersAdapter.OnItemClickListener listener,
                          FoldersAdapter.OnLongClickListener listenerLong) {
        this.localDataSet = foldersList;
        this.listener = listener;
        this.listenerLong = listenerLong;
    }

    public interface OnItemClickListener {
        void onItemClick(Folder folder);
    }

    public interface OnLongClickListener {
        void onLongItemClick(FoldersAdapter ad, Folder folder);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textView;
        private final ImageView folderIcon;

        public ViewHolder(View view) {
            super(view);
            textView = (TextView) view.findViewById(R.id.folderName);
            folderIcon = (ImageView) view.findViewById(R.id.folderIcon);
        }

        public void bind(final Folder folder, final FoldersAdapter.OnItemClickListener listener,
                         final FoldersAdapter.OnLongClickListener listenerLong) {
            textView.setText(ConfigUtil.decodeBase64(folder.getName()));
            folderIcon.setImageResource(android.R.drawable.sym_contact_card);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(folder);
                }
            });
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    listenerLong.onLongItemClick(FoldersAdapter.this, folder);
                    return true;
                }
            });
        }
    }

    @Override
    public FoldersAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.folder_item, viewGroup, false);

        return new FoldersAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(FoldersAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.bind(localDataSet.get(position), listener, listenerLong);
    }

    @Override
    public int getItemCount() {
        return localDataSet.size();
    }

    public void setData(ArrayList<Folder> set) {
        this.localDataSet = set;
        notifyDataSetChanged();
    }
}
