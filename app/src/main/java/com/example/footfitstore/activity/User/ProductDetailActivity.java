package com.example.footfitstore.activity.User;

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.Utils.CustomDialog;
import com.example.footfitstore.adapter.UserSideAdapter.SizeAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductDetailActivity extends AppCompatActivity {

    private ImageView ivProductImage;
    private TextView tvProductName, tvCategory, tvPrice, tvDescription;
    private ImageButton btnFavorite;
    private ImageButton btnCart;
    private ImageButton btnBack;
    private Button btnAddToCart;
    private RecyclerView recyclerViewSizes;
    private SizeAdapter sizeAdapter;
    private RatingBar ratingBar;
    private DatabaseReference productReference;
    private FirebaseUser currentUser;
    private DatabaseReference userFavoritesRef;
    private DatabaseReference userCartRef;

    private String productId;
    private String selectedSize;
    private String productName;
    private boolean isFavorite = false;
    private double price = 0;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);
        ivProductImage = findViewById(R.id.ivProductImage);
        tvProductName = findViewById(R.id.tvProductName);
        tvCategory = findViewById(R.id.tvCategory);
        tvPrice = findViewById(R.id.tvPrice);
        tvDescription = findViewById(R.id.tvDescription);
        btnFavorite = findViewById(R.id.btnFavorite);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnCart =findViewById(R.id.btnCart);
        btnBack = findViewById(R.id.btnBack);
        recyclerViewSizes = findViewById(R.id.recyclerViewSizes);
        ratingBar = findViewById(R.id.ratingProduct);

        Intent intent = getIntent();
        if (intent != null) {
            productId = intent.getStringExtra("productId");
            getRating();
            loadProductData(productId);
        }

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            userFavoritesRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("favourite");
            userCartRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("cart");
        }

        checkIfProductIsFavorite();

        btnBack.setOnClickListener(v -> {
            finish();
        });

        btnCart.setOnClickListener(v -> {
            Intent intentCart = new Intent(ProductDetailActivity.this, MainActivity.class);
            intentCart.putExtra("openFragment", "cart");
            startActivity(intentCart);
            finish();
        });

        btnAddToCart.setOnClickListener(v -> {
            if (currentUser == null) {
                new CustomDialog(ProductDetailActivity.this)
                        .setTitle("Failed")
                        .setMessage("Something's Wrong.")
                        .setIcon(R.drawable.error)
                        .setPositiveButton("OK", null)
                        .hideNegativeButton()
                        .show();
            } else if (selectedSize == null) {
                new CustomDialog(ProductDetailActivity.this)
                        .setTitle("Failed")
                        .setMessage("Please Select A Size.")
                        .setIcon(R.drawable.error)
                        .setPositiveButton("OK", null)
                        .hideNegativeButton()
                        .show();
            } else {
                addToCart();
            }
        });

        btnFavorite.setOnClickListener(v -> {
            if (currentUser == null) {
                new CustomDialog(ProductDetailActivity.this)
                        .setTitle("Failed")
                        .setMessage("Please Log In To Get Cart Data.")
                        .setIcon(R.drawable.error)
                        .setPositiveButton("OK", null)
                        .hideNegativeButton()
                        .show();            } else {
                toggleFavorite();
            }
        });
    }

    private void loadProductData(String productId) {
        productReference = FirebaseDatabase.getInstance().getReference("Shoes").child(productId);

        productReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    productName = dataSnapshot.child("title").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    price = dataSnapshot.child("price").getValue(Double.class);
                    String imageUrl = dataSnapshot.child("picUrl").child("0").getValue(String.class);
                    String category =dataSnapshot.child("category").getValue(String.class);
                    double productRating = dataSnapshot.child("rating").getValue(Double.class);

                    tvProductName.setText(productName);
                    tvDescription.setText(description);
                    tvCategory.setText(category+"'s Shoes");
                    ratingBar.setRating((float)productRating);
                    Picasso.get().load(imageUrl).into(ivProductImage);
                    DataSnapshot promotionSnapshot = dataSnapshot.child("promotion");
                    if (promotionSnapshot.exists()) {
                        int discount = promotionSnapshot.child("discount").getValue(Integer.class);
                        String startDate = promotionSnapshot.child("startDate").getValue(String.class);
                        String endDate = promotionSnapshot.child("endDate").getValue(String.class);

                        if (isPromotionActive(startDate, endDate)) {
                            double finalPrice = price * (1 - discount / 100.0);
                            tvPrice.setText("$" + String.format("%.2f", finalPrice));
                        } else {
                            tvPrice.setText("$" + String.format("%.2f", price));
                        }
                    } else {
                        tvPrice.setText("$" + String.format("%.2f", price));
                    }
                    List<String> sizes = new ArrayList<>();
                    for (DataSnapshot sizeSnapshot : dataSnapshot.child("size").getChildren()) {
                        sizes.add(sizeSnapshot.getValue(String.class));
                    }
                    setupSizeRecyclerView(sizes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                new CustomDialog(ProductDetailActivity.this)
                        .setTitle("Failed")
                        .setMessage("Failed To Load Cart Data.")
                        .setIcon(R.drawable.error)
                        .setPositiveButton("OK", null)
                        .hideNegativeButton()
                        .show();            }
        });
    }

    private void setupSizeRecyclerView(List<String> sizes) {
        recyclerViewSizes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        sizeAdapter = new SizeAdapter(this, sizes);
        recyclerViewSizes.setAdapter(sizeAdapter);

        sizeAdapter.setOnSizeSelectedListener(size -> selectedSize = size);
    }

    private void checkIfProductIsFavorite() {
        userFavoritesRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isFavorite = dataSnapshot.exists();
                updateFavoriteIcon();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                new CustomDialog(ProductDetailActivity.this)
                        .setTitle("Failed")
                        .setMessage("Failed To Get Favourite Shoe.")
                        .setIcon(R.drawable.error)
                        .setPositiveButton("OK", null)
                        .hideNegativeButton()
                        .show();            }
        });
    }

    private void updateFavoriteIcon() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_heart_filled);
        } else {
            btnFavorite.setImageResource(R.drawable.ic_heart);
        }
    }

    private void toggleFavorite() {
        if (isFavorite) {
            userFavoritesRef.child(productId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = false;
                        updateFavoriteIcon();
                    });
        } else {
            Map<String, Object> favouriteItem = new HashMap<>();
            favouriteItem.put("productId", productId);

            userFavoritesRef.child(productId).setValue(favouriteItem)
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = true;
                        updateFavoriteIcon();
                    });
        }
    }

    private void addToCart() {
        String cartKey = productId + "_" + selectedSize;

        userCartRef.child(cartKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int currentQuantity = dataSnapshot.child("quantity").getValue(Integer.class);
                    userCartRef.child(cartKey).child("quantity").setValue(currentQuantity + 1)
                            .addOnSuccessListener(aVoid -> new CustomDialog(ProductDetailActivity.this)
                                    .setTitle("Success")
                                    .setMessage("Successfully Add Product To Cart.")
                                    .setIcon(R.drawable.congrat)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show())
                            .addOnFailureListener(e -> new CustomDialog(ProductDetailActivity.this)
                                    .setTitle("Failed")
                                    .setMessage("Failed From Add To Cart.")
                                    .setIcon(R.drawable.error)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show());
                } else {
                    Map<String, Object> cartItem = new HashMap<>();
                    cartItem.put("productId", productId);
                    cartItem.put("productName", productName);
                    cartItem.put("price", price);
                    cartItem.put("quantity", 1);
                    cartItem.put("size", selectedSize);

                    userCartRef.child(cartKey).setValue(cartItem)
                            .addOnSuccessListener(aVoid -> new CustomDialog(ProductDetailActivity.this)
                                    .setTitle("Success")
                                    .setMessage("Successfully Add Product To Cart.")
                                    .setIcon(R.drawable.congrat)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show())
                            .addOnFailureListener(e -> new CustomDialog(ProductDetailActivity.this)
                                    .setTitle("Failed")
                                    .setMessage("Failed From Add To Cart.")
                                    .setIcon(R.drawable.error)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                new CustomDialog(ProductDetailActivity.this)
                        .setTitle("Failed")
                        .setMessage("Failed To Check Cart Status.")
                        .setIcon(R.drawable.error)
                        .setPositiveButton("OK", null)
                        .hideNegativeButton()
                        .show();
            }
        });
    }
    public void getRating()
    {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("Users");
        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Double> allRatings = new ArrayList<>();
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    DataSnapshot ordersSnapshot = userSnapshot.child("order");
                    for (DataSnapshot orderSnapshot : ordersSnapshot.getChildren()) {
                        DataSnapshot reviewsSnapshot = orderSnapshot.child("review");
                        for (DataSnapshot reviewSnapshot : reviewsSnapshot.getChildren()) {
                            String input = reviewSnapshot.getKey();
                            String result = input.split("_")[0];
                            if (result.equals(productId))
                            {
                                Double rating = reviewSnapshot.child("rating").getValue(Double.class);
                                if (rating != null) {
                                    allRatings.add(rating);
                                }
                            }
                        }
                    }
                }
                Double finalValue = 0.0;
                if (!allRatings.isEmpty())
                {
                    for (Double rating : allRatings) {
                        finalValue += rating;
                    }
                    finalValue /= allRatings.size();
                }
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Shoes").child(productId).child("rating");
                userRef.setValue(finalValue);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private boolean isPromotionActive(String startDate, String endDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);
            Date today = new Date();
            today = resetTime(today);
            return (today.after(start) || today.equals(start)) && (today.before(end) || today.equals(end));
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    private static Date resetTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
}

