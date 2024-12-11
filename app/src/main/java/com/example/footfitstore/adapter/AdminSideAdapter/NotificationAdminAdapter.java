package com.example.footfitstore.adapter.AdminSideAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.model.NotificationAdmin;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class NotificationAdminAdapter extends RecyclerView.Adapter<NotificationAdminAdapter.NotificationAdminViewHolder>{
    private Context context;
    private List<NotificationAdmin> list;
    private List<Boolean> selectedList;

    public List<Boolean> getSelectedList() {
        return selectedList;
    }

    public void setSelectedList(List<Boolean> selectedList) {
        this.selectedList = selectedList;
    }
    public NotificationAdminAdapter(Context context, List<NotificationAdmin> list) {
        this.context = context;
        this.list = list;
        selectedList = new ArrayList<>();
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
        int positionNew = position;
        holder.selected.setChecked(selectedList.get(position));
        holder.selected.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                selectedList.set(positionNew, isChecked);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class NotificationAdminViewHolder extends RecyclerView.ViewHolder {
        CheckBox selected;
        TextView txt_description;
        ImageView ivProductImage;
        TextView txtStartDate, txtEndDate;
        public NotificationAdminViewHolder(@NonNull View itemView) {
            super(itemView);
            txt_description = itemView.findViewById(R.id.edtDescription);
            ivProductImage = itemView.findViewById(R.id.imgProduct);
            txtStartDate = itemView.findViewById(R.id.startDate);
            txtEndDate = itemView.findViewById(R.id.endDate);
            selected = itemView.findViewById(R.id.selected_notification);
        }
    }
}
