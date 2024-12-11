package com.example.footfitstore.adapter.UserSideAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.model.Notification;
import com.squareup.picasso.Picasso;

import java.util.List;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>{
    private Context context;
    private List<Notification> notificationList;
    private OnNotificationClickListener onNotificationClickListener;

    public NotificationAdapter(Context context, List<Notification> notificationList, OnNotificationClickListener onNotificationClickListener) {
        this.context = context;
        this.notificationList = notificationList;
        this.onNotificationClickListener = onNotificationClickListener;
    }

    public interface OnNotificationClickListener {
        void onNotificationClick(int position);
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_notification,parent,false);
        return new NotificationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification notification = notificationList.get(position);
        holder.txt_description.setText(notification.getDescription());
        Picasso.get().load(notification.getImgUrl()).into(holder.ivProductImage);
        if (notification.isRead())
        {
            holder.txt_Status.setVisibility(View.GONE);
            holder.itemView.setBackgroundResource(R.drawable.notification_item_shape_read);
        }
        else {
            holder.txt_Status.setVisibility(View.VISIBLE);
            holder.itemView.setBackgroundResource(R.drawable.notification_item_shape_unread);
        }
        holder.itemView.setOnClickListener(view -> {
            if (onNotificationClickListener != null) {
                onNotificationClickListener.onNotificationClick(position);
            }
        });
    }



    @Override
    public int getItemCount() {
        return notificationList.size();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        TextView txt_description;
        ImageView ivProductImage;
        TextView txt_Status;
        public NotificationViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_description = itemView.findViewById(R.id.edtDescription);
            ivProductImage = itemView.findViewById(R.id.imgProduct);
            txt_Status = itemView.findViewById(R.id.txt_status);
        }
    }
}
