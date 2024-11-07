package com.example.footfitstore.fragment;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import com.example.footfitstore.adapter.BannerAdapter;
import com.example.footfitstore.adapter.CategoryAdapter;
import com.example.footfitstore.adapter.SearchShoeAdapter;
import com.example.footfitstore.adapter.ShoeAdapter;
import com.example.footfitstore.adapter.SizeAdapter;
import com.example.footfitstore.model.Shoe;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.*;

public class ExploreFragment extends Fragment implements ShoeAdapter.BottomSheetListener {

    private RecyclerView popularShoesRecyclerView, bannerRecyclerView, allShoesRecyclerView, searchResultsRecyclerView, categoryRecyclerView;
    private ImageButton btnCart;
    private EditText searchEditText;
    private ShoeAdapter popularShoeAdapter, allShoeAdapter;
    private BannerAdapter bannerAdapter;
    private SearchShoeAdapter searchResultsAdapter;
    private CategoryAdapter categoryAdapter;
    private List<String> categoryList = new ArrayList<>(), bannerList = new ArrayList<>();
    private List<Shoe> popularshoeList = new ArrayList<>(), allshoesList = new ArrayList<>(), searchResultsList = new ArrayList<>();
    private DatabaseReference userCartRef, bannerReference, allshoesReference;
    private String selectedSize, productId, productName;
    private double price;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Khởi tạo giao diện của fragment
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        // Khởi tạo các thành phần UI và adapter
        initializeViews(view);
        initializeAdapters();
        loadDataFromFirebase();
        loadCategoriesFromFirebase();

        // Xử lý khi nhấn vào nút giỏ hàng
        btnCart.setOnClickListener(v -> openCartFragment());

        // Lắng nghe sự thay đổi của văn bản trong ô tìm kiếm
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {filterShoes(s.toString().toLowerCase()); }
            @Override public void afterTextChanged(Editable s) { }
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataFromFirebase(); // Tải lại dữ liệu khi fragment được tiếp tục hoạt động
    }

    // Hàm khởi tạo các thành phần UI
    private void initializeViews(View view) {
        btnCart = view.findViewById(R.id.btnCart);
        popularShoesRecyclerView = view.findViewById(R.id.popularShoesRecyclerView);
        allShoesRecyclerView = view.findViewById(R.id.allShoesRecyclerView);
        bannerRecyclerView = view.findViewById(R.id.bannerRecyclerView);
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);

        // Thiết lập layout cho các RecyclerView
        popularShoesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        allShoesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        bannerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    // Hàm khởi tạo các adapter cho RecyclerView
    private void initializeAdapters() {
        popularShoeAdapter = new ShoeAdapter(getContext(), popularshoeList, allshoesList, "popular", this, this);
        allShoeAdapter = new ShoeAdapter(getContext(), popularshoeList, allshoesList, "all", this, this);
        bannerAdapter = new BannerAdapter(bannerList);
        searchResultsAdapter = new SearchShoeAdapter(searchResultsList, getContext());
        categoryAdapter = new CategoryAdapter(getContext(), categoryList);

        // Thiết lập adapter cho các RecyclerView
        popularShoesRecyclerView.setAdapter(popularShoeAdapter);
        allShoesRecyclerView.setAdapter(allShoeAdapter);
        bannerRecyclerView.setAdapter(bannerAdapter);
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);
        categoryRecyclerView.setAdapter(categoryAdapter);
    }

    // Hàm tải dữ liệu từ Firebase
    private void loadDataFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : null;

        // Lấy dữ liệu giỏ hàng và yêu thích của người dùng nếu đã đăng nhập
        if (uid != null) {
            userCartRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("cart");
            DatabaseReference userFavouriteRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("favourite");
            userFavouriteRef.addListenerForSingleValueEvent(getFavouriteEventListener());
        } else {
            loadShoesData(new HashSet<>());
        }

        // Lấy dữ liệu banner từ Firebase
        bannerReference = FirebaseDatabase.getInstance().getReference("BannerEvent");
        allshoesReference = FirebaseDatabase.getInstance().getReference("Shoes");
        bannerReference.addValueEventListener(getBannerEventListener());
    }

    // Lắng nghe sự kiện tải dữ liệu yêu thích của người dùng từ Firebase
    private ValueEventListener getFavouriteEventListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot favouriteSnapshot) {
                Set<String> favouriteProductIds = new HashSet<>();
                for (DataSnapshot favSnapshot : favouriteSnapshot.getChildren()) {
                    favouriteProductIds.add(favSnapshot.getKey());
                }
                loadShoesData(favouriteProductIds);
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e("Firebase", "Không thể tải danh sách yêu thích", error.toException()); }
        };
    }

    // Lắng nghe sự kiện tải dữ liệu banner từ Firebase
    private ValueEventListener getBannerEventListener() {
        return new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bannerList.clear();
                for (DataSnapshot bannerSnapshot : snapshot.getChildren()) {
                    String imageUrl = bannerSnapshot.child("url").getValue(String.class);
                    if (imageUrl != null) bannerList.add(imageUrl);
                }
                bannerAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e("Firebase", "Không thể tải danh sách banner", error.toException()); }
        };
    }

    // Hàm tải dữ liệu giày từ Firebase và kiểm tra giày nào được yêu thích
    private void loadShoesData(Set<String> favouriteProductIds) {
        allshoesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allshoesList.clear();
                popularshoeList.clear();
                for (DataSnapshot shoeSnapshot : snapshot.getChildren()) {
                    Shoe shoe = shoeSnapshot.getValue(Shoe.class);
                    if (shoe != null) {
                        shoe.setProductId(shoeSnapshot.getKey());
                        shoe.setFavourite(favouriteProductIds.contains(shoe.getProductId()));
                        allshoesList.add(shoe);
                        if (Boolean.TRUE.equals(shoe.getIsPopular())) popularshoeList.add(shoe);
                    }
                }
                popularShoeAdapter.notifyDataSetChanged();
                allShoeAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { Log.e("Firebase", "Không thể tải danh sách giày", error.toException()); }
        });
    }

    // Mở fragment giỏ hàng khi nhấn vào nút giỏ hàng
    private void openCartFragment() {
        getParentFragmentManager().beginTransaction()
                .replace(R.id.main_frame, new CartFragment())
                .addToBackStack(null)
                .commit();
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).setSelectedNavItem(R.id.nav_cart);
        }
    }

    // Hàm lọc giày dựa trên từ khóa tìm kiếm
    private void filterShoes(String query) {
        searchResultsList.clear();
        // Kiểm tra nếu chuỗi query trống thì ẩn RecyclerView
        if (query.isEmpty()) {
            searchResultsRecyclerView.setVisibility(View.GONE);
        } else {
            // Lọc giày dựa trên từ khóa tìm kiếm
            for (Shoe shoe : allshoesList) {
                if (shoe.getTitle().toLowerCase().contains(query)) {
                    searchResultsList.add(shoe);
                }
            }
            searchResultsRecyclerView.setVisibility(searchResultsList.isEmpty() ? View.GONE : View.VISIBLE);
        }
        searchResultsAdapter.notifyDataSetChanged();
    }

    // Hàm tải dữ liệu danh mục từ Firebase
    private void loadCategoriesFromFirebase() {
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("Category");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                categoryList.clear();
                for (DataSnapshot categorySnapshot : snapshot.getChildren()) {
                    String categoryTitle = categorySnapshot.child("title").getValue(String.class);
                    if (categoryTitle != null) categoryList.add(categoryTitle);
                }
                categoryAdapter.notifyDataSetChanged();
            }
            @Override public void onCancelled(@NonNull DatabaseError error) { Toast.makeText(getContext(), "Không thể tải danh mục.", Toast.LENGTH_SHORT).show(); }
        });
    }

    // Hiển thị hộp thoại chọn kích cỡ giày
    @Override
    public void showBottomSheetDialog(Shoe shoe) {
        productId = shoe.getProductId();
        price = shoe.getPrice();
        productName = shoe.getTitle();

        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_size_pick, null);
        setupSizeRecyclerView(dialogView, shoe.getProductId(), shoe.getSize());
        dialogView.findViewById(R.id.btn_submit).setOnClickListener(v -> {
            if (selectedSize != null) {
                addToCart();
                bottomSheetDialog.dismiss();
            } else {
                showToast("Please select shoe size");
            }
        });
        bottomSheetDialog.setContentView(dialogView);
        bottomSheetDialog.show();
    }

    // Thiết lập RecyclerView để chọn kích cỡ giày
    private void setupSizeRecyclerView(View dialogView, String productId, List<String> productSize) {
        RecyclerView recyclerView = dialogView.findViewById(R.id.size);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        SizeAdapter sizeAdapter = new SizeAdapter(getContext(), productSize);
        recyclerView.setAdapter(sizeAdapter);
        sizeAdapter.setOnSizeSelectedListener(size -> selectedSize = size);
    }

    // Hàm thêm sản phẩm vào giỏ hàng
    private void addToCart() {
        String cartKey = productId + "_" + selectedSize;
        userCartRef.child(cartKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    Integer currentQuantity = dataSnapshot.child("quantity").getValue(Integer.class);
                    if (currentQuantity != null) {
                        userCartRef.child(cartKey).child("quantity").setValue(currentQuantity + 1)
                                .addOnSuccessListener(aVoid -> showToast("Increased quantity in cart"))
                                .addOnFailureListener(e -> showToast("Failed to update cart"));
                    }
                } else {
                    Map<String, Object> cartItem = new HashMap<>();
                    cartItem.put("productId", productId);
                    cartItem.put("productName", productName);
                    cartItem.put("price", price);
                    cartItem.put("quantity", 1);
                    cartItem.put("size", selectedSize);
                    userCartRef.child(cartKey).setValue(cartItem)
                            .addOnSuccessListener(aVoid -> showToast("Added to cart"))
                            .addOnFailureListener(e -> showToast("Failed to add to cart"));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showToast("Không thể kiểm tra trạng thái giỏ hàng");
            }
        });
    }

    // Hàm tiện ích để hiển thị Toast nếu Context không phải là null
    private void showToast(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    // Hàm cập nhật dữ liệu của các adapter
    public void updateAdapters() {
        popularShoeAdapter.notifyDataSetChanged();
        allShoeAdapter.notifyDataSetChanged();
    }
}
