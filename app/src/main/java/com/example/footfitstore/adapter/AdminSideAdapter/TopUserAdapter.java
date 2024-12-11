package com.example.footfitstore.adapter.AdminSideAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.model.TopUser;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TopUserAdapter extends RecyclerView.Adapter<TopUserAdapter.TopUserViewHolder> {
    private final Context context;
    private List<TopUser> topUserList;

    public TopUserAdapter(Context context, List<TopUser> topUserList) {
        this.context = context;
        this.topUserList = topUserList;
    }

    @NonNull
    @Override
    public TopUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_top_user, parent, false);
        return new TopUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopUserViewHolder holder, int position) {
        TopUser topUser = topUserList.get(position);
        switch (position)
        {
            case 0:
                holder.topTier.setImageResource(R.drawable.ic_first_place);
                break;
            case 1:
                holder.topTier.setImageResource(R.drawable.ic_second_place);
                break;
            default:
                holder.topTier.setImageResource(R.drawable.ic_third_place);
        }
        holder.tvName.setText(topUser.getFirstName() + " " + topUser.getLastName());
        holder.tvEmail.setText(topUser.getEmail());
        holder.tvTotalTransaction.setText(String.valueOf(topUser.getTotalTransaction().intValue()));
        if (!topUser.getImageUrl().isEmpty())
        {
            Picasso.get().load(topUser.getImageUrl()).into(holder.topUserImage);
        }
        else
        {
            if (topUser.getGender() == 0)
            {
                holder.topUserImage.setImageResource(R.drawable.boy);
            }
            else
            {
                holder.topUserImage.setImageResource(R.drawable.girl);
            }
        }
    }

    @Override
    public int getItemCount() {
        return topUserList.size();
    }

    public static class TopUserViewHolder extends RecyclerView.ViewHolder{
        TextView tvName, tvEmail, tvTotalTransaction;
        ImageView topTier;
        ImageView topUserImage;
        public TopUserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.displayName);
            tvEmail = itemView.findViewById(R.id.email);
            tvTotalTransaction = itemView.findViewById(R.id.totalTransaction);
            topTier = itemView.findViewById(R.id.topTier);
            topUserImage = itemView.findViewById(R.id.avatar);
        }
    }
}
