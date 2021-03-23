package com.tiromansev.filedialog;

import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FilesViewHolder extends RecyclerView.ViewHolder {

    public ImageView imageView;
    public TextView txtTitle;
    public TextView txtData;
    public RelativeLayout rlDirItem;

    public FilesViewHolder(@NonNull View itemView) {
        super(itemView);
        imageView = itemView.findViewById(R.id.ivFileImage);
        txtTitle = itemView.findViewById(R.id.tvFileItem);
        txtData = itemView.findViewById(R.id.tvFileData);
        rlDirItem = itemView.findViewById(R.id.rlDirItem);
    }

}
