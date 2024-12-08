package com.example.footfitstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.model.NotificationAdmin;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationAdminAdapter extends RecyclerView.Adapter<NotificationAdminAdapter.NotificationAdminViewHolder>{
    private Context context;
    private List<NotificationAdmin> list;
    public NotificationAdminAdapter(Context context, List<NotificationAdmin> list) {
        this.context = context;
        this.list = list;
    }
    @NonNull
    @Override
    public NotificationAdminViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification_admin,parent,false);
        return new NotificationAdminViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationAdminViewHolder holder, int position) {
        NotificationAdmin notificationAdmin = list.get(position);
        holder.txt_description.setText(notificationAdmin.getDescription());
        Picasso.get().load(notificationAdmin.getImgUrl()).into(holder.ivProductImage);
        holder.txtStartDate.setText("Start: "+ notificationAdmin.getStartDate());
        holder.txtEndDate.setText("End: " + notificationAdmin.getEndDate());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class NotificationAdminViewHolder extends RecyclerView.ViewHolder {
        TextView txt_description;
        ImageView ivProductImage;
        TextView txtStartDate, txtEndDate;
        public NotificationAdminViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_description = itemView.findViewById(R.id.edtDescription);
            ivProductImage = itemView.findViewById(R.id.imgProduct);
            txtStartDate = itemView.findViewById(R.id.startDate);
            txtEndDate = itemView.findViewById(R.id.endDate);
        }
    }
}
