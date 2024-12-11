package com.example.footfitstore.adapter.AdminSideAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
    private NavigateProfileManagement listener;
    public interface NavigateProfileManagement{
        void NavigateToAdmin(User user);
    }
    public UserAdapter(Context context, List<User> userList, NavigateProfileManagement listener) {
        this.context = context;
        this.userList = userList;
        this.listener = listener;
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
        if (user.getStatus() != null)
        {
            holder.txtStatus.setText(user.getStatus());
        }
        holder.txtName.setText(firstName+" "+lastName);
        holder.txtMail.setText(mail);
        holder.txtPhone.setText(phoneNumber);
        if (user.getAvatarUrl() != null)
        {
            Picasso.get().load(user.getAvatarUrl()).into(holder.imgAvatar);
        }
        else
        {
            if (user.getGender() == 0)
            {
                holder.imgAvatar.setImageResource(R.drawable.boy);
            }
            else
            {
                holder.imgAvatar.setImageResource(R.drawable.girl);
            }
        }
        holder.txtRole.setText(user.getRole());
        if (user.getRole().equals("admin"))
        {
            holder.imgRole.setImageResource(R.drawable.admin);
        }
        else
        {
            holder.imgRole.setImageResource(R.drawable.user);
        }
        holder.btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (listener !=null)
                {
                    listener.NavigateToAdmin(user);
                }
            }
        });
        if (!user.getStatus().equals("active"))
        {
            holder.btnSubmit.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class UserViewHolder extends RecyclerView.ViewHolder {
        CircleImageView imgAvatar;
        TextView txtName, txtMail, txtPhone,txtRole,txtStatus;
        Button btnSubmit;
        ImageView imgRole;
        View line;
        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.avatar);
            txtName = itemView.findViewById(R.id.name);
            txtMail = itemView.findViewById(R.id.email);
            txtPhone = itemView.findViewById(R.id.phone);
            btnSubmit = itemView.findViewById(R.id.btnSubmit);
            txtRole = itemView.findViewById(R.id.txtRole);
            imgRole = itemView.findViewById(R.id.role);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            line = itemView.findViewById(R.id.view);
        }
    }
}
