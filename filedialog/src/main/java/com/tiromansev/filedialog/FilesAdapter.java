package com.tiromansev.filedialog;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.tiromansev.filedialog.utils.ColorUtils;

import java.util.List;

import static com.tiromansev.filedialog.BreadCrumbs.UNDEFINED_VALUE;

public class FilesAdapter extends RecyclerView.Adapter<FilesViewHolder> {

    private List<RowItem> rowItems;
    private Context context;
    private int selectedPosition = -1;
    private ItemSelectListener itemSelectListener;

    public FilesAdapter(Context context,
                        List<RowItem> rowItems,
                        ItemSelectListener itemSelectListener) {
        this.rowItems = rowItems;
        this.context = context;
        this.itemSelectListener = itemSelectListener;
    }

    public Context getContext() {
        return context;
    }

    @NonNull
    @Override
    public FilesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).
                inflate(R.layout.view_file_dialog_item, parent, false);
        return new FilesViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FilesViewHolder holder, int position) {
        RowItem rowItem = rowItems.get(position);

        holder.txtTitle.setText(rowItem.getTitle());
        holder.txtData.setText(rowItem.getData());
        holder.txtData.setVisibility(rowItem.getData() != null ? View.VISIBLE : View.GONE);
        holder.imageView.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        holder.rlDirItem.setOnClickListener(v -> {
            selectedPosition = position;
            if (itemSelectListener != null) {
                itemSelectListener.onItemSelected(rowItem);
            }
            notifyDataSetChanged();
        });

        if (rowItem.getImageId() != UNDEFINED_VALUE) {
            holder.imageView.setImageResource(rowItem.getImageId());
        } else {
            holder.imageView.setImageBitmap(null);
        }

        if (selectedPosition == position) {
            holder.rlDirItem.setBackgroundColor(ColorUtils.getAttrColor(R.attr.file_dialog_selected_item_background, getContext()));
            holder.txtTitle.setTextColor(ColorUtils.getAttrColor(R.attr.file_dialog_selected_dir_item_color, getContext()));
            holder.txtData.setTextColor(ColorUtils.getAttrColor(R.attr.file_dialog_selected_dir_item_color, getContext()));
        } else {
            holder.rlDirItem.setBackgroundColor(ColorUtils.getAttrColor(R.attr.file_dialog_item_background, getContext()));
            holder.txtTitle.setTextColor(ColorUtils.getAttrColor(R.attr.file_dialog_dir_item_color, getContext()));
            holder.txtData.setTextColor(ColorUtils.getAttrColor(R.attr.file_dialog_dir_item_color, getContext()));
        }
    }

    @Override
    public int getItemCount() {
        return rowItems.size();
    }

    public interface ItemSelectListener {
        void onItemSelected(RowItem rowItem);
    }
}
