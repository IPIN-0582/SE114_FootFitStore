package com.example.footfitstore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.footfitstore.R;
import com.example.footfitstore.activity.LoginActivity;
import com.example.footfitstore.activity.MainActivity;
import com.example.footfitstore.activity.PaymentHistory;
import com.example.footfitstore.activity.SearchActivity;
import com.example.footfitstore.adapter.BannerAdapter;
import com.example.footfitstore.adapter.CategoryAdapter;
import com.example.footfitstore.adapter.SearchShoeAdapter;
import com.example.footfitstore.adapter.ShoeAdapter;
import com.example.footfitstore.adapter.SizeAdapter;
import com.example.footfitstore.model.OrderHistory;
import com.example.footfitstore.model.Shoe;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.*;
import java.util.*;

public class ExploreFragment extends Fragment implements ShoeAdapter.BottomSheetListener {

    private RecyclerView popularShoesRecyclerView, bannerRecyclerView, allShoesRecyclerView, categoryRecyclerView;
    private ImageButton btnCart,btnMenu;
    private EditText searchEditText;
    private ShoeAdapter popularShoeAdapter, allShoeAdapter;
    private BannerAdapter bannerAdapter;
    private CategoryAdapter categoryAdapter;
    private List<String> categoryList = new ArrayList<>(), bannerList = new ArrayList<>();
    private List<Shoe> popularshoeList = new ArrayList<>(), allshoesList = new ArrayList<>();
    private DatabaseReference userCartRef, bannerReference, allshoesReference;
    private String selectedSize, productId, productName;
    private double price;
    private String selectedCategory="";
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView headerTextView;
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
        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawerLayout.openDrawer(navigationView);
            }
        });
        View headerView = navigationView.getHeaderView(0);
        headerTextView = headerView.findViewById(R.id.txt_displayName);
        navigationView.bringToFront();
        navigationView.getMenu().getItem(0).setChecked(true);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(getActivity(),drawerLayout,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.navi_cart)
                {
                    openCartFragment();
                }
                else if (item.getItemId() == R.id.navi_favourite)
                {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.main_frame, new FavouriteFragment())
                            .addToBackStack(null)
                            .commit();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).setSelectedNavItem(R.id.nav_favourite);
                    }
                }
                else if (item.getItemId() == R.id.navi_order)
                {
                    Intent intent = new Intent(getActivity(), PaymentHistory.class);
                    startActivity(intent);
                }
                else if (item.getItemId() == R.id.navi_profile)
                {
                    getParentFragmentManager().beginTransaction()
                            .replace(R.id.main_frame, new ProfileFragment())
                            .addToBackStack(null)
                            .commit();
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).setSelectedNavItem(R.id.nav_profile);
                    }
                }
                else if (item.getItemId() == R.id.navi_log_out)
                {
                    FirebaseAuth auth = FirebaseAuth.getInstance();
                    auth.signOut();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    if (getActivity() instanceof MainActivity)
                    {
                        getActivity().finish();
                    }
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                drawerLayout.addDrawerListener(new DrawerLayout.DrawerListener() {
                    @Override
                    public void onDrawerSlide(@NonNull View drawerView, float slideOffset) {

                    }

                    @Override
                    public void onDrawerOpened(@NonNull View drawerView) {

                    }

                    @Override
                    public void onDrawerClosed(@NonNull View drawerView) {
                        navigationView.getMenu().getItem(0).setChecked(true);
                    }

                    @Override
                    public void onDrawerStateChanged(int newState) {

                    }
                });
                return true;
            }
        });
        // Xử lý khi nhấn vào nút giỏ hàng
        btnCart.setOnClickListener(v -> openCartFragment());
        // Xử lý khi nhấn vào thanh tìm kiếm
        searchEditText.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SearchActivity.class);
            startActivity(intent);
        });
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START);
                } else {
                    setEnabled(false);
                    requireActivity().onBackPressed();
                }
            }
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
        btnMenu=view.findViewById(R.id.btnMenu);
        btnCart = view.findViewById(R.id.btnCart);
        popularShoesRecyclerView = view.findViewById(R.id.popularShoesRecyclerView);
        allShoesRecyclerView = view.findViewById(R.id.allShoesRecyclerView);
        bannerRecyclerView = view.findViewById(R.id.bannerRecyclerView);
        searchEditText = view.findViewById(R.id.searchEditText);
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        drawerLayout=view.findViewById(R.id.explore_nav);
        navigationView=view.findViewById(R.id.nav_view);

        // Thiết lập layout cho các RecyclerView
        popularShoesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        allShoesRecyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        bannerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    }

    // Hàm khởi tạo các adapter cho RecyclerView
    private void initializeAdapters() {
        popularShoeAdapter = new ShoeAdapter(getContext(), popularshoeList, allshoesList, "popular", this, this);
        allShoeAdapter = new ShoeAdapter(getContext(), popularshoeList, allshoesList, "all", this, this);
        bannerAdapter = new BannerAdapter(bannerList);
        categoryAdapter = new CategoryAdapter(getContext(), categoryList, new CategoryAdapter.OnCategoryClickListener() {
            @Override
            public void onCategoryClick(String categoryName) {
                selectedCategory=categoryName;
                ensureFavouriteStatusOnItem();
            }
        });

        // Thiết lập adapter cho các RecyclerView
        popularShoesRecyclerView.setAdapter(popularShoeAdapter);
        allShoesRecyclerView.setAdapter(allShoeAdapter);
        bannerRecyclerView.setAdapter(bannerAdapter);
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
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String firstName = snapshot.child("firstName").getValue(String.class);
                    String lastName = snapshot.child("lastName").getValue(String.class);
                    // Hiển thị vào TextView
                    headerTextView.setText(firstName+" "+lastName);
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
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
                                .addOnSuccessListener(aVoid -> showDialog())
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
                            .addOnSuccessListener(aVoid -> showDialog())
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
    private void showDialog()
    {
        if (getActivity() != null)
        {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomAlertDialog);
            final View customLayout = getLayoutInflater().inflate(R.layout.dialog_add_to_cart, null);
            builder.setView(customLayout);
            Button positiveButton = customLayout.findViewById(R.id.back_to_shopping);
            AlertDialog alertDialog = builder.create();
            positiveButton.setOnClickListener(v -> {
                alertDialog.dismiss();
            });
            alertDialog.show();
        }
    }
    private void ensureFavouriteStatusOnItem()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : null;
        DatabaseReference userFavouriteRef = FirebaseDatabase.getInstance().getReference("Users").child(uid).child("favourite");
        userFavouriteRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Set<String> favouriteProductIds = new HashSet<>();
                for (DataSnapshot favSnapshot : snapshot.getChildren()) {
                    favouriteProductIds.add(favSnapshot.getKey());
                    loadProductByCategory(favouriteProductIds);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void loadProductByCategory(Set<String> favouriteProductIds)
    {
        allshoesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!selectedCategory.isEmpty())
                {
                    popularshoeList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren())
                    {
                        if (dataSnapshot.child("category").getValue(String.class).equals(selectedCategory))
                        {
                            Shoe shoe = dataSnapshot.getValue(Shoe.class);
                            if (shoe != null) {
                                shoe.setProductId(dataSnapshot.getKey());
                                shoe.setFavourite(favouriteProductIds.contains(shoe.getProductId()));
                                popularshoeList.add(shoe);
                            }
                        }
                    }
                    popularShoeAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
