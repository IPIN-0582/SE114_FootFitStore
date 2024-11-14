package com.example.footfitstore.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.model.Cart;
import com.example.footfitstore.model.OrderHistory;

import java.util.List;

public class OrderHistoryAdapter extends RecyclerView.Adapter<OrderHistoryAdapter.PaymentViewHolder> {
    Context context;
    List<OrderHistory> orderHistoryList;

    public OrderHistoryAdapter(Context context, List<OrderHistory> orderHistoryList) {
        this.context = context;
        this.orderHistoryList=orderHistoryList;
    }

    @NonNull
    @Override
    public PaymentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.payment_history,parent,false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        List<Cart> singleCartList = orderHistoryList.get(position).getCartList();
        holder.txtDate.setText("Order Time:"+orderHistoryList.get(position).getOrderTime());
        holder.txtStatus.setText("Status:" + orderHistoryList.get(position).getOrderStatus());
        CartAdapter cartAdapter=new CartAdapter(singleCartList,context,true);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerView.setAdapter(cartAdapter);
    }

    @Override
    public int getItemCount() {
        return orderHistoryList.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate,txtStatus;
        RecyclerView recyclerView;
        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate=itemView.findViewById(R.id.txt_Date);
            txtStatus=itemView.findViewById(R.id.txt_OrderStatus);
            recyclerView=itemView.findViewById(R.id.recycler);
        }
    }
}
