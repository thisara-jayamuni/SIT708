package com.example.lostandfound;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(LostFoundItem item);
    }

    private List<LostFoundItem> items = new ArrayList<>();
    private OnItemClickListener listener;

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<LostFoundItem> newItems) {
        items = new ArrayList<>(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_lost, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LostFoundItem item = items.get(position);
        holder.bind(item);
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(item);
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView ivThumbnail;
        private final TextView tvTitle;
        private final TextView tvBadge;
        private final TextView tvCategoryLocation;
        private final TextView tvDescriptionPreview;
        private final TextView tvTimestamp;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivThumbnail           = itemView.findViewById(R.id.ivThumbnail);
            tvTitle               = itemView.findViewById(R.id.tvTitle);
            tvBadge               = itemView.findViewById(R.id.tvBadge);
            tvCategoryLocation    = itemView.findViewById(R.id.tvCategoryLocation);
            tvDescriptionPreview  = itemView.findViewById(R.id.tvDescriptionPreview);
            tvTimestamp           = itemView.findViewById(R.id.tvTimestamp);
        }

        void bind(LostFoundItem item) {
            byte[] bytes = item.getImageBytes();
            if (bytes != null) {
                ivThumbnail.setImageBitmap(ImageHelper.bytesToBitmap(bytes));
                ivThumbnail.setScaleType(ImageView.ScaleType.CENTER_CROP);
                ivThumbnail.setPadding(0, 0, 0, 0);
            } else {
                ivThumbnail.setImageResource(R.drawable.ic_cat_default);
                ivThumbnail.setScaleType(ImageView.ScaleType.CENTER);
                int p = (int) (12 * itemView.getContext().getResources().getDisplayMetrics().density);
                ivThumbnail.setPadding(p, p, p, p);
            }

            tvTitle.setText(item.getTitle());
            tvCategoryLocation.setText(item.getCategory() + " · " + item.getLocation());
            tvDescriptionPreview.setText(item.getDescription());
            tvTimestamp.setText(item.getTimestamp());

            if ("Found".equals(item.getPostType())) {
                tvBadge.setText(R.string.label_found);
                tvBadge.setTextColor(itemView.getContext().getColor(R.color.badge_found_text));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_found);
            } else {
                tvBadge.setText(R.string.label_lost);
                tvBadge.setTextColor(itemView.getContext().getColor(R.color.badge_lost_text));
                tvBadge.setBackgroundResource(R.drawable.bg_badge_lost);
            }
        }
    }
}