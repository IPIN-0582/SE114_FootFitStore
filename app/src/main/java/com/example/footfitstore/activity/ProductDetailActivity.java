package com.example.footfitstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.adapter.SizeAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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

        // Ánh xạ các View
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

        // Nhận dữ liệu từ Intent
        Intent intent = getIntent();
        if (intent != null) {
            productId = intent.getStringExtra("productId");
            loadProductData(productId); // Gọi phương thức để tải dữ liệu sản phẩm từ Firebase
        }

        // Lấy thông tin người dùng hiện tại
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

        // Kiểm tra trạng thái yêu thích ban đầu
        checkIfProductIsFavorite();

        // Xử lý sự kiện cho nút Back
        btnBack.setOnClickListener(v -> {
            finish(); // Quay lại màn hình trước đó
        });

        // Xử lý sự kiện thêm vào giỏ hàng
        btnAddToCart.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(ProductDetailActivity.this, "Please log in to add to cart", Toast.LENGTH_SHORT).show();
            } else if (selectedSize == null) {
                Toast.makeText(ProductDetailActivity.this, "Please select a size", Toast.LENGTH_SHORT).show();
            } else {
                addToCart();
            }
        });

        // Xử lý sự kiện yêu thích
        btnFavorite.setOnClickListener(v -> {
            if (currentUser == null) {
                Toast.makeText(ProductDetailActivity.this, "Please log in to manage favourites", Toast.LENGTH_SHORT).show();
            } else {
                toggleFavorite();
            }
        });
    }

    // Phương thức để tải dữ liệu sản phẩm từ Firebase dựa vào productId
    private void loadProductData(String productId) {
        productReference = FirebaseDatabase.getInstance().getReference("Shoes").child(productId);

        productReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Truy xuất dữ liệu sản phẩm từ Firebase
                    productName = dataSnapshot.child("title").getValue(String.class);
                    String description = dataSnapshot.child("description").getValue(String.class);
                    price = dataSnapshot.child("price").getValue(Double.class);
                    String imageUrl = dataSnapshot.child("picUrl").child("0").getValue(String.class);  // Lấy URL của ảnh đầu tiên

                    // Gán dữ liệu vào các View
                    tvProductName.setText(productName);
                    tvDescription.setText(description);
                    tvPrice.setText("$" + price);
                    tvCategory.setText("Men's Shoes");  // Giả sử là danh mục

                    // Tải hình ảnh từ URL (sử dụng Picasso)
                    Picasso.get().load(imageUrl).into(ivProductImage);

                    // Tải kích cỡ giày từ Firebase
                    List<String> sizes = new ArrayList<>();
                    for (DataSnapshot sizeSnapshot : dataSnapshot.child("size").getChildren()) {
                        sizes.add(sizeSnapshot.getValue(String.class));
                    }
                    setupSizeRecyclerView(sizes);  // Hiển thị các kích cỡ vào RecyclerView
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProductDetailActivity.this, "Failed to load product data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Thiết lập RecyclerView kích cỡ
    private void setupSizeRecyclerView(List<String> sizes) {
        recyclerViewSizes.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        sizeAdapter = new SizeAdapter(this, sizes);
        recyclerViewSizes.setAdapter(sizeAdapter);

        // Xử lý sự kiện khi người dùng chọn kích cỡ
        sizeAdapter.setOnSizeSelectedListener(size -> selectedSize = size);
    }

    // Kiểm tra xem sản phẩm có trong danh sách yêu thích không
    private void checkIfProductIsFavorite() {
        userFavoritesRef.child(productId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                isFavorite = dataSnapshot.exists(); // Nếu tồn tại thì là yêu thích
                updateFavoriteIcon();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProductDetailActivity.this, "Failed to check favourite status", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Cập nhật icon yêu thích dựa trên trạng thái
    private void updateFavoriteIcon() {
        if (isFavorite) {
            btnFavorite.setImageResource(R.drawable.ic_heart_filled); // Icon yêu thích
        } else {
            btnFavorite.setImageResource(R.drawable.ic_heart); // Icon chưa yêu thích
        }
    }

    // Xử lý sự kiện chuyển trạng thái yêu thích
    private void toggleFavorite() {
        if (isFavorite) {
            // Nếu sản phẩm đã được yêu thích, xóa khỏi danh sách yêu thích
            userFavoritesRef.child(productId).removeValue()
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = false;
                        updateFavoriteIcon();
                        Toast.makeText(ProductDetailActivity.this, "Removed from favourites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProductDetailActivity.this, "Failed to remove from favourites", Toast.LENGTH_SHORT).show();
                    });
        } else {
            // Nếu sản phẩm chưa được yêu thích, thêm vào danh sách yêu thích
            Map<String, Object> favouriteItem = new HashMap<>();
            favouriteItem.put("productId", productId);

            userFavoritesRef.child(productId).setValue(favouriteItem)
                    .addOnSuccessListener(aVoid -> {
                        isFavorite = true;
                        updateFavoriteIcon();
                        Toast.makeText(ProductDetailActivity.this, "Added to favourites", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ProductDetailActivity.this, "Failed to add to favourites", Toast.LENGTH_SHORT).show();
                    });
        }
    }
    // Thêm sản phẩm vào giỏ hàng
    private void addToCart() {
        // Tạo khóa mới cho sản phẩm trong giỏ hàng dựa trên productId và size
        String cartKey = productId + "_" + selectedSize;

        // Kiểm tra xem sản phẩm với kích cỡ đã có trong giỏ hàng chưa
        userCartRef.child(cartKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    // Nếu sản phẩm đã có trong giỏ hàng, tăng số lượng
                    int currentQuantity = dataSnapshot.child("quantity").getValue(Integer.class);
                    userCartRef.child(cartKey).child("quantity").setValue(currentQuantity + 1)
                            .addOnSuccessListener(aVoid -> Toast.makeText(ProductDetailActivity.this, "Increased quantity in cart", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(ProductDetailActivity.this, "Failed to update cart", Toast.LENGTH_SHORT).show());
                } else {
                    // Nếu sản phẩm chưa có trong giỏ hàng, thêm sản phẩm mới với số lượng là 1
                    Map<String, Object> cartItem = new HashMap<>();
                    cartItem.put("productId", productId);
                    cartItem.put("productName", productName);
                    cartItem.put("price", price);
                    cartItem.put("quantity", 1);
                    cartItem.put("size", selectedSize);

                    userCartRef.child(cartKey).setValue(cartItem)
                            .addOnSuccessListener(aVoid -> Toast.makeText(ProductDetailActivity.this, "Added to cart", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(ProductDetailActivity.this, "Failed to add to cart", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(ProductDetailActivity.this, "Failed to check cart status", Toast.LENGTH_SHORT).show();
            }
        });
    }
}

