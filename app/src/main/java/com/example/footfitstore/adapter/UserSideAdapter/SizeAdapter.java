package com.example.footfitstore.adapter.UserSideAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;

import java.util.List;

public class SizeAdapter extends RecyclerView.Adapter<SizeAdapter.SizeViewHolder> {

    private final List<String> sizeList;
    private final Context context;
    private int selectedPosition = -1;
    private OnSizeSelectedListener sizeSelectedListener;

    public SizeAdapter(Context context, List<String> sizeList) {
        this.context = context;
        this.sizeList = sizeList;
    }

    @NonNull
    @Override
    public SizeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_size, parent, false);
        return new SizeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SizeViewHolder holder, int position) {
        String size = sizeList.get(position);
        holder.tvSize.setText(size);

        if (selectedPosition == position) {
            holder.itemView.setBackgroundResource(R.drawable.size_seleted_background);
            holder.tvSize.setTextColor(context.getResources().getColor(android.R.color.white));
        } else {
            holder.itemView.setBackgroundResource(R.drawable.size_background);
            holder.tvSize.setTextColor(context.getResources().getColor(android.R.color.black));
        }

        holder.itemView.setOnClickListener(v -> {
            int adapterPosition = holder.getAdapterPosition();

            if (adapterPosition != RecyclerView.NO_POSITION) {
                selectedPosition = adapterPosition;
                notifyDataSetChanged();

                if (sizeSelectedListener != null) {
                    sizeSelectedListener.onSizeSelected(sizeList.get(adapterPosition));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return sizeList.size();
    }

    public static class SizeViewHolder extends RecyclerView.ViewHolder {
        TextView tvSize;

        public SizeViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSize = itemView.findViewById(R.id.tvSize);
        }
    }

    public interface OnSizeSelectedListener {
        void onSizeSelected(String size);
    }

    public void setOnSizeSelectedListener(OnSizeSelectedListener listener) {
        this.sizeSelectedListener = listener;
    }
}

