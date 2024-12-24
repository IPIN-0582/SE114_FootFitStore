package com.example.footfitstore.adapter.UserSideAdapter;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.Utils.CustomDialog;
import com.example.footfitstore.model.CartRating;
import com.example.footfitstore.model.OrderHistory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        View view = LayoutInflater.from(context).inflate(R.layout.item_order,parent,false);
        return new PaymentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PaymentViewHolder holder, int position) {
        List<CartRating> singleCartList = orderHistoryList.get(position).getCartList();
        holder.txtDate.setText(orderHistoryList.get(position).getOrderTime());
        holder.txtStatus.setText(orderHistoryList.get(position).getOrderStatus());
        CartRatingAdapter cartAdapter=new CartRatingAdapter(context, singleCartList,false,orderHistoryList.get(position).getOrderTime(),false);
        holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.recyclerView.setAdapter(cartAdapter);
        if (orderHistoryList.get(position).getOrderStatus().equals("SUCCESS"))
        {
            holder.btnReviewOrder.setBackgroundResource(R.drawable.button_shape_cancel);
            holder.btnReviewOrder.setText("Cancel Order");
            int newPos=position;
            holder.btnReviewOrder.setOnClickListener(v -> {
                DatabaseReference cartRef = FirebaseDatabase.getInstance()
                        .getReference("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                        .child("order")
                        .child("order_"+newPos).child("orderStatus");
                cartRef.setValue("CANCELLED");
                orderHistoryList.get(newPos).setOrderStatus("CANCELLED");
            });
        }
        else if (orderHistoryList.get(position).getOrderStatus().equals("ARRIVED"))
        {
            holder.btnReviewOrder.setBackgroundResource(R.drawable.button_shape);
            int newPos=position;
            holder.btnReviewOrder.setOnClickListener(v -> {
                Dialog dialog = new Dialog(v.getContext());
                dialog.setContentView(R.layout.dialog_review);

                if (dialog.getWindow() != null) {
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
                }

                Button button = dialog.findViewById(R.id.btnSubmitRating);
                EditText editText=dialog.findViewById(R.id.edtFeedback);
                RecyclerView recyclerView = dialog.findViewById(R.id.recyclerRating);
                recyclerView.setLayoutManager(new LinearLayoutManager(v.getContext()));
                dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                List<CartRating> newRatingList = new ArrayList<>();
                for (CartRating cart:singleCartList)
                {
                    CartRating cartRating=new CartRating();
                    cartRating.setProductName(cart.getProductName());
                    cartRating.setPrice(cart.getPrice());
                    cartRating.setProductId(cart.getProductId());
                    cartRating.setQuantity(cart.getQuantity());
                    cartRating.setSize(cart.getSize());
                    newRatingList.add(cartRating);
                }
                CartRatingAdapter cartRatingAdapter=new CartRatingAdapter(v.getContext(), newRatingList,true,orderHistoryList.get(newPos).getOrderTime(),true);
                recyclerView.setAdapter(cartRatingAdapter);
                button.setOnClickListener(v1 -> {
                    DatabaseReference cartRef = FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("order")
                            .child("order_"+newPos).child("review");
                    for (CartRating cart:newRatingList)
                    {
                        if (cart.getRating() == 0)
                        {
                            new CustomDialog(v1.getContext())
                                    .setTitle("Failed")
                                    .setMessage("Please Select A Size.")
                                    .setIcon(R.drawable.error)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show();
                            return;
                        }
                        String productKey = cart.getProductId() + "_" + cart.getSize();
                        Map<String, Object> cartItem = new HashMap<>();
                        cartItem.put("price",cart.getPrice());
                        cartItem.put("productId",cart.getProductId());
                        cartItem.put("productName",cart.getProductName());
                        cartItem.put("quantity",cart.getQuantity());
                        cartItem.put("size",cart.getSize());
                        cartItem.put("rating",cart.getRating());
                        cartRef.child(productKey).setValue(cartItem);
                    }
                    String comment = editText.getText().toString();
                    cartRef.child("order_review").setValue(comment);
                    changeStatus(newPos);
                    dialog.dismiss();
                });
                dialog.show();
            });
        }
        else if (orderHistoryList.get(position).getOrderStatus().equals("REVIEWED"))
        {
            holder.btnReviewOrder.setVisibility(View.GONE);
            cartAdapter=new CartRatingAdapter(context, singleCartList,true,orderHistoryList.get(position).getOrderTime(),false);
            holder.recyclerView.setAdapter(cartAdapter);
            holder.recyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        }
        else
        {
            holder.btnReviewOrder.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return orderHistoryList.size();
    }

    static class PaymentViewHolder extends RecyclerView.ViewHolder {
        TextView txtDate,txtStatus;
        RecyclerView recyclerView;
        Button btnReviewOrder;
        public PaymentViewHolder(@NonNull View itemView) {
            super(itemView);
            txtDate=itemView.findViewById(R.id.txt_Date);
            txtStatus=itemView.findViewById(R.id.txt_OrderStatus);
            recyclerView=itemView.findViewById(R.id.recycler);
            btnReviewOrder=itemView.findViewById(R.id.btn_ReviewOrder);
        }
    }
    public void changeStatus(int newPos)
    {
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("order")
                .child("order_"+newPos);
        cartRef.child("orderStatus").setValue("REVIEWED");
    }
}
