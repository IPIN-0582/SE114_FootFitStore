package com.example.footfitstore.adapter.AdminSideAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.adapter.UserSideAdapter.CartRatingAdapter;
import com.example.footfitstore.model.OrderHistory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class OrderMinimizeAdapter extends RecyclerView.Adapter<OrderMinimizeAdapter.OrderMinimizeViewHolder> {
    Context context;
    List<OrderHistory> orderHistoryList;
    CartRatingAdapter cartRatingAdapter;
    private String userId;
    public OrderMinimizeAdapter(Context context, List<OrderHistory> orderHistoryList, String userId) {
        this.context = context;
        this.orderHistoryList = orderHistoryList;
        this.userId = userId;
    }

    @NonNull
    @Override
    public OrderMinimizeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_minimize,parent,false);
        return new OrderMinimizeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderMinimizeViewHolder holder, int position) {
        OrderHistory orderHistory = orderHistoryList.get(position);
        holder.txtDate.setText(orderHistory.getOrderTime());
        holder.txtStatus.setText(orderHistory.getOrderStatus());
        holder.txtMethod.setText(orderHistory.getPaymentMethod());
        holder.txtTransaction.setText(orderHistory.getTransaction()+" $");
        if (!orderHistory.getOrderStatus().equals("SUCCESS"))
        {
            holder.btnSubmit.setVisibility(View.GONE);
        }
        holder.btnSubmit.setOnClickListener(v->{
            updateStatus(orderHistory.getOrderTime());
            holder.btnSubmit.setVisibility(View.GONE);
        });
        if (!orderHistory.getReview().isEmpty())
        {
            cartRatingAdapter = new CartRatingAdapter(context, orderHistory.getCartList(), true, orderHistory.getOrderTime(),false);
            holder.recycler.setAdapter(cartRatingAdapter);
            holder.recycler.setLayoutManager(new LinearLayoutManager(context));
            holder.txtReview.setText(orderHistory.getReview());
        }
        else
        {
            holder.itemLabel.setVisibility(View.GONE);
            holder.txtReview.setVisibility(View.GONE);
            holder.reviewLabel.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orderHistoryList.size();
    }

    static class OrderMinimizeViewHolder extends RecyclerView.ViewHolder{
        TextView txtDate, txtMethod, txtStatus, txtTransaction, txtReview, reviewLabel, itemLabel;
        RecyclerView recycler;
        Button btnSubmit;
        public OrderMinimizeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txt_Date);
            txtMethod = itemView.findViewById(R.id.txt_Method);
            txtTransaction = itemView.findViewById(R.id.txt_transaction);
            txtStatus = itemView.findViewById(R.id.txt_status);
            btnSubmit = itemView.findViewById(R.id.btnSubmit);
            txtReview = itemView.findViewById(R.id.reviewTxt);
            reviewLabel = itemView.findViewById(R.id.reviewLabel);
            recycler = itemView.findViewById(R.id.rvOrderItems);
            itemLabel = itemView.findViewById(R.id.itemLabel);
        }
    }
    private void updateStatus(String orderTime)
    {
        DatabaseReference orderRef = FirebaseDatabase.getInstance().getReference("Users").child(userId).child("order");
        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    String orderId = dataSnapshot.getKey();
                    if (dataSnapshot.child("orderTime").getValue(String.class).equals(orderTime))
                    {
                        assert orderId != null;
                        orderRef.child(orderId).child("orderStatus").setValue("ARRIVED");
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
