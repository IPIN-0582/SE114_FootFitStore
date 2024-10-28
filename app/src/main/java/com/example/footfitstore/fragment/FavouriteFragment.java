package com.example.footfitstore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.activity.MainActivity;
import com.example.footfitstore.activity.ProductDetailActivity;
import com.example.footfitstore.adapter.ShoeAdapter;
import com.example.footfitstore.adapter.SizeAdapter;
import com.example.footfitstore.model.Shoe;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FavouriteFragment extends Fragment implements ShoeAdapter.BottomSheetListener {

    private RecyclerView favouriteShoesRecyclerView;
    private ShoeAdapter favouriteShoeAdapter;
    private final List<Shoe> favouriteshoeList = new ArrayList<>();

    private DatabaseReference allshoesReference;
    private ImageButton btnBack;
    private ImageButton btnCart;

    private RecyclerView recyclerView;
    private String selectedSize;
    private String productId;
    private String productName;
    private double price = 0;
    private DatabaseReference userCartRef;
    private DatabaseReference productReference;
    private SizeAdapter sizeAdapter;

    public void showBottomSheetDialog(Shoe shoe) {
        productId=shoe.getProductId();
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_size_pick, null);
        recyclerView=dialogView.findViewById(R.id.size);
        String productId=shoe.getProductId();
        loadProductData(productId);
        Button btnSubmit=dialogView.findViewById(R.id.btn_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedSize==null)
                {
                    Toast.makeText(getContext(), "Please select a size", Toast.LENGTH_SHORT).show();
                }
                else {
                    addToCart();
                    bottomSheetDialog.cancel();
                }
            }
        });
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favourite, container, false);
        btnBack =  view.findViewById(R.id.btnBack);
        btnCart = view.findViewById(R.id.btnCart);
        favouriteShoesRecyclerView = view.findViewById(R.id.favouriteShoesRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        favouriteShoesRecyclerView.setLayoutManager(gridLayoutManager);

        // Dùng cùng một adapter cho cả hai RecyclerView nhưng với cờ viewType khác nhau
        favouriteShoeAdapter = new ShoeAdapter(getContext(), null, favouriteshoeList, "all",this,this);
        favouriteShoesRecyclerView.setAdapter(favouriteShoeAdapter);

        btnBack.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), MainActivity.class));
        });

        btnCart.setOnClickListener(v -> {
            // Chuyển đổi sang CartFragment
            CartFragment cartFragment = new CartFragment();

            // Sử dụng FragmentManager để thay thế fragment hiện tại bằng CartFragment
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, cartFragment)  // R.id.main_frame là ID của FrameLayout trong MainActivity
                    .addToBackStack(null)  // Nếu muốn cho phép quay lại
                    .commit();

            // Cập nhật BottomNavigationView
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedNavItem(R.id.nav_cart);  // Cập nhật trạng thái bottom nav
            }
        });

        allshoesReference = FirebaseDatabase.getInstance().getReference("Shoes");

        // Lấy dữ liệu từ Firebase
        loadDataFromFirebase();
        return view;
    }

    private void loadDataFromFirebase() {
        // Lấy UID của người dùng hiện tại
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            // Người dùng chưa đăng nhập, không cần tải dữ liệu yêu thích
            Log.e("FavouriteFragment", "Người dùng chưa đăng nhập.");
            return;
        }

        String uid = user.getUid();
        DatabaseReference userFavouriteRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("favourite");
        userCartRef = FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .child("cart");
        userFavouriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot favouriteSnapshot) {
                Set<String> favouriteProductIds = new HashSet<>();
                for (DataSnapshot favSnapshot : favouriteSnapshot.getChildren()) {
                    String productId = favSnapshot.getKey();
                    favouriteProductIds.add(productId);
                }

                // Sau khi có danh sách yêu thích, tải danh sách giày
                loadShoesData(favouriteProductIds);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi lấy dữ liệu yêu thích", error.toException());
                // Trong trường hợp lỗi, vẫn tải danh sách giày mà không có thông tin yêu thích
                loadShoesData(new HashSet<>());
            }
        });
    }

    private void loadShoesData(Set<String> favouriteProductIds) {
        if (favouriteProductIds.isEmpty()) {
            Log.e("Firebase", "Không có sản phẩm yêu thích nào.");
            return;
        }
        allshoesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot){
                favouriteshoeList.clear();
                int favouriteCount = 0;
                for (DataSnapshot shoeSnapshot : snapshot.getChildren()) {
                    Shoe shoe = shoeSnapshot.getValue(Shoe.class);
                    // Chỉ thêm các sản phẩm thuộc danh sách yêu thích
                    if (shoe != null && favouriteProductIds.contains(shoeSnapshot.getKey())) {
                        shoe.setProductId(shoeSnapshot.getKey());
                        shoe.setFavourite(true);
                        favouriteshoeList.add(shoe);
                        favouriteCount++;
                    }
                }
                Log.d("COUNT:", "favourite shoe:"+favouriteCount);
                // Thông báo rằng dữ liệu đã thay đổi sau khi tất cả sản phẩm đã được xử lý
                favouriteShoeAdapter.notifyDataSetChanged();
        }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi lấy dữ liệu giày", error.toException());
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
                Toast.makeText(getContext(), "Failed to load product data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void setupSizeRecyclerView(List<String> sizes) {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        sizeAdapter = new SizeAdapter(getContext(), sizes);
        recyclerView.setAdapter(sizeAdapter);
        sizeAdapter.setOnSizeSelectedListener(size -> selectedSize = size);
    }
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
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Increased quantity in cart", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to update cart", Toast.LENGTH_SHORT).show());
                } else {
                    // Nếu sản phẩm chưa có trong giỏ hàng, thêm sản phẩm mới với số lượng là 1
                    Map<String, Object> cartItem = new HashMap<>();
                    cartItem.put("productId", productId);
                    cartItem.put("productName", productName);
                    cartItem.put("price", price);
                    cartItem.put("quantity", 1);
                    cartItem.put("size", selectedSize);

                    userCartRef.child(cartKey).setValue(cartItem)
                            .addOnSuccessListener(aVoid -> Toast.makeText(getContext(), "Added to cart", Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Failed to add to cart", Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(getContext(), "Failed to check cart status", Toast.LENGTH_SHORT).show();
            }
        });
    }
}