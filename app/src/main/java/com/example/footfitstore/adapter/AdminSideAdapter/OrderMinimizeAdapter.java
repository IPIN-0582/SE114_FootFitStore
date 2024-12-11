package com.example.footfitstore.adapter.AdminSideAdapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.model.OrderHistory;

import java.util.List;

public class OrderMinimizeAdapter extends RecyclerView.Adapter<OrderMinimizeAdapter.OrderMinimizeViewHolder> {
    Context context;
    List<OrderHistory> orderHistoryList;

    public OrderMinimizeAdapter(Context context, List<OrderHistory> orderHistoryList) {
        this.context = context;
        this.orderHistoryList = orderHistoryList;
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
    }

    @Override
    public int getItemCount() {
        return orderHistoryList.size();
    }

    static class OrderMinimizeViewHolder extends RecyclerView.ViewHolder{
        TextView txtDate, txtMethod, txtStatus, txtTransaction;
        Button btnSubmit;
        public OrderMinimizeViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate = itemView.findViewById(R.id.txt_Date);
            txtMethod = itemView.findViewById(R.id.txt_Method);
            txtTransaction = itemView.findViewById(R.id.txt_transaction);
            txtStatus = itemView.findViewById(R.id.txt_status);
            btnSubmit = itemView.findViewById(R.id.btnSubmit);
        }
    }
}
