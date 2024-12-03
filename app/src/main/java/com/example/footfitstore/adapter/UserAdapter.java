package com.example.footfitstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.model.User;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UserAdapter  extends RecyclerView.Adapter<UserAdapter.UserViewHolder>{
    Context context;
    List<User> userList;

    public UserAdapter(Context context, List<User> userList) {
        this.context = context;
        this.userList = userList;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = userList.get(position);
        String firstName = "";
        String lastName = "";
        String mail = "";
        String phoneNumber = "";
        if (user.getFirstname() != null)
        {
            firstName = user.getFirstname();
        }
        if (user.getLastname() != null)
        {
            lastName = user.getLastname();
        }
        if (user.getEmail() != null)
        {
            mail = user.getEmail();
        }
        if (user.getPhone() != null)
        {
            phoneNumber = user.getPhone();
        }
        holder.txtName.setText(firstName+" "+lastName);
        holder.txtMail.setText(mail);
        holder.txtPhone.setText(phoneNumber);
        if (user.getAvatarUrl() != null)
        {
            Picasso.get().load(user.getAvatarUrl()).placeholder(R.drawable.onboard1).into(holder.imgAvatar);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        TextView txtName, txtMail, txtPhone;
        Button btnSubmit;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.avatar);
            txtName = itemView.findViewById(R.id.name);
            txtMail = itemView.findViewById(R.id.email);
            txtPhone = itemView.findViewById(R.id.phone);
            btnSubmit = itemView.findViewById(R.id.btnSubmit);
        }
    }
}
