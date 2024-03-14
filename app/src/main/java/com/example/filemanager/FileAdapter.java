package com.example.filemanager;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.List;

public class FileAdapter extends RecyclerView.Adapter<FileViewHolder> {

    private Context context;
    private List<File> files;
    private OnFileSelectedListener listener;

    public FileAdapter(Context context, List<File> files, OnFileSelectedListener listener) {
        this.context = context;
        this.files = files;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FileViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new FileViewHolder(LayoutInflater.from(context).inflate(R.layout.file_container, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull FileViewHolder holder, int position) {
        holder.tvName.setText(files.get(position).getName());
        holder.tvName.setSelected(true);
        int items = 0;
        if (files.get(position).isDirectory()){
            File[] files2 = files.get(position).listFiles();
            try{
                for (File singleFile : files2){
                    if (!singleFile.isHidden()){
                        items += 1;
                    }
                }
                holder.tvSize.setText(String.valueOf(items) + "Files");
            }catch (Exception e){
                e.printStackTrace();
            }

        }
        else {
            holder.tvSize.setText(Formatter.formatShortFileSize(context, files.get(position).length()));
        }
        if (files.get(position).getName().endsWith(".jpeg")){
            holder.imgFile.setImageResource(R.drawable.ic_image);
        }
        else if (files.get(position).getName().endsWith(".doc")){
            holder.imgFile.setImageResource(R.drawable.ic_image);
        }
        else if (files.get(position).getName().endsWith(",mp4")){
            holder.imgFile.setImageResource(R.drawable.ic_baseline_folder_24);
        }
        holder.container.setOnClickListener(v->{
            listener.onFileClicked(files.get(position));
        });
        holder.container.setOnLongClickListener(v->{
            listener.onFileLongClicked(files.get(position), position);
            return false;
        });
    }

    @Override
    public int getItemCount() {
        return files.size();
    }
}
