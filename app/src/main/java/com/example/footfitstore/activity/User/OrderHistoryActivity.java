package com.example.footfitstore.activity.User;

import android.os.Bundle;
import android.widget.ImageButton;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.adapter.UserSideAdapter.OrderHistoryAdapter;
import com.example.footfitstore.model.Cart;
import com.example.footfitstore.model.CartRating;
import com.example.footfitstore.model.OrderHistory;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrderHistoryActivity extends AppCompatActivity {
    RecyclerView recyclerView;
    OrderHistoryAdapter orderHistoryAdapter;
    ImageButton imgBack;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_payment_history);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        imgBack=findViewById(R.id.btnBack);
        recyclerView=findViewById(R.id.recycler_Order);
        imgBack.setOnClickListener(v->finish());
        loadDataFromDatabase();
    }
    public void loadDataFromDatabase()
    {
        FirebaseUser currentUser= FirebaseAuth.getInstance().getCurrentUser();
        String userUid = currentUser.getUid();
        DatabaseReference orderRef= FirebaseDatabase.getInstance().getReference().child("Users").child(userUid).child("order");
        orderRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<OrderHistory> paymentHistoryList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    OrderHistory orderHistory = new OrderHistory();
                    orderHistory.setOrderTime(dataSnapshot.child("orderTime").getValue(String.class));
                    orderHistory.setOrderStatus(dataSnapshot.child("orderStatus").getValue(String.class));
                    List<CartRating> cartList = new ArrayList<>();
                    for (DataSnapshot cartSnapshot : dataSnapshot.child("carList").getChildren()) {
                        Cart cart = cartSnapshot.getValue(Cart.class);
                        CartRating temporary = new CartRating();
                        temporary.setPrice(cart.getPrice());
                        temporary.setProductId(cart.getProductId());
                        temporary.setSize(cart.getSize());
                        temporary.setProductName(cart.getProductName());
                        temporary.setQuantity(cart.getQuantity());
                        cartList.add(temporary);
                    }
                    orderHistory.setCartList(cartList);
                    paymentHistoryList.add(orderHistory);
                    if (dataSnapshot.child("review").exists()) {
                        List<CartRating> cartRatingList = new ArrayList<>();
                        for (DataSnapshot cartSnapshot : dataSnapshot.child("review").getChildren()) {
                            String key = cartSnapshot.getKey();
                            assert key != null;
                            if (key.equals( "order_review")) {
                                String review = cartSnapshot.getValue(String.class);
                                orderHistory.setReview(review);
                            }
                            else
                            {
                                CartRating cartRating = cartSnapshot.getValue(CartRating.class);
                                cartRatingList.add(cartRating);
                            }
                            orderHistory.setCartList(cartRatingList);
                        }
                    }
                }
                setupSizeRecyclerView(paymentHistoryList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void setupSizeRecyclerView(List<OrderHistory> orderHistoryList)
    {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        orderHistoryAdapter = new OrderHistoryAdapter(this,orderHistoryList);
        recyclerView.setAdapter(orderHistoryAdapter);
    }
}