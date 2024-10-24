package com.example.footfitstore.fragment;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.footfitstore.R;
import com.example.footfitstore.activity.MainActivity;
import com.example.footfitstore.adapter.BannerAdapter;
import com.example.footfitstore.adapter.SearchShoeAdapter;
import com.example.footfitstore.adapter.ShoeAdapter;
import com.example.footfitstore.model.Shoe;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ExploreFragment extends Fragment {

    private RecyclerView popularShoesRecyclerView;
    private RecyclerView bannerRecyclerView;
    private RecyclerView allShoesRecyclerView;
    private RecyclerView searchResultsRecyclerView;
    private ImageButton btnCart;

    private ShoeAdapter  popularShoeAdapter;
    private ShoeAdapter  allShoeAdapter;
    private BannerAdapter bannerAdapter;
    private SearchShoeAdapter searchResultsAdapter;

    private final List<Shoe> popularshoeList = new ArrayList<>();
    private final List<String> bannerList = new ArrayList<>();
    private final List<Shoe> allshoesList = new ArrayList<>();
    private final List<Shoe> searchResultsList = new ArrayList<>();

    private DatabaseReference bannerReference;
    private DatabaseReference allshoesReference;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_explore, container, false);

        btnCart =view.findViewById(R.id.btnCart);
        // Khởi tạo RecyclerView cho giày phổ biến
        popularShoesRecyclerView = view.findViewById(R.id.popularShoesRecyclerView);
        popularShoesRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Khởi tạo RecyclerView cho tất cả giày
        allShoesRecyclerView = view.findViewById(R.id.allShoesRecyclerView);
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        allShoesRecyclerView.setLayoutManager(gridLayoutManager);

        // Dùng cùng một adapter cho cả hai RecyclerView nhưng với cờ viewType khác nhau
        popularShoeAdapter = new ShoeAdapter(getContext(), popularshoeList, allshoesList, "popular",this);
        allShoeAdapter = new ShoeAdapter(getContext(), popularshoeList, allshoesList, "all",this);

        popularShoesRecyclerView.setAdapter(popularShoeAdapter);  // Gắn adapter cho giày phổ biến
        allShoesRecyclerView.setAdapter(allShoeAdapter);  // Gắn adapter cho tất cả giày

        // Khởi tạo RecyclerView cho banner
        bannerRecyclerView = view.findViewById(R.id.bannerRecyclerView);
        bannerRecyclerView.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        // Khởi tạo Adapter cho banner
        bannerAdapter = new BannerAdapter(bannerList);
        bannerRecyclerView.setAdapter(bannerAdapter);

        // Khởi tạo RecyclerView cho search Result
        searchResultsRecyclerView = view.findViewById(R.id.searchResultsRecyclerView);
        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultsAdapter = new SearchShoeAdapter(searchResultsList, getContext());
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        bannerReference = FirebaseDatabase.getInstance().getReference("BannerEvent");
        allshoesReference = FirebaseDatabase.getInstance().getReference("Shoes");

        // Tìm kiếm TextInputEditText từ TextInputLayout
        TextInputEditText searchEditText = view.findViewById(R.id.searchBar).findViewById(R.id.searchEditText);

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

        // Lắng nghe sự thay đổi trong thanh tìm kiếm
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Không cần xử lý nội dung trong phương thức này
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String searchText = s.toString().toLowerCase();

                // Nếu người dùng xóa hết nội dung, ẩn RecyclerView
                if (searchText.isEmpty()) {
                    searchResultsRecyclerView.setVisibility(View.GONE);
                } else {
                    // Lọc giày dựa trên từ khóa tìm kiếm
                    filterShoes(searchText);
                    // Hiển thị lại RecyclerView nếu có kết quả tìm kiếm
                    if (!searchResultsList.isEmpty()) {
                        searchResultsRecyclerView.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Không cần xử lý nội dung trong phương thức này
            }
        });

        // Lấy dữ liệu từ Firebase
        loadDataFromFirebase();
        return view;
    }

    private void filterShoes(String query) {
        searchResultsList.clear();
        // Duyệt qua danh sách tất cả giày và thêm giày phù hợp với từ khóa vào searchResultsList
        for (Shoe shoe : allshoesList) {
            if (shoe.getTitle().toLowerCase().contains(query)) {
                searchResultsList.add(shoe);
            }
        }

        // Cập nhật kết quả tìm kiếm trong RecyclerView
        searchResultsAdapter.notifyDataSetChanged();

        // Hiển thị hoặc ẩn RecyclerView dựa trên có kết quả tìm kiếm hay không
        if (searchResultsList.isEmpty()) {
            searchResultsRecyclerView.setVisibility(View.GONE);
        } else {
            searchResultsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void loadDataFromFirebase() {
        // Lấy UID của người dùng hiện tại
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : null;

        // Nếu người dùng đã đăng nhập, lấy danh sách yêu thích của họ
        if (uid != null) {
            DatabaseReference userFavouriteRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(uid)
                    .child("favourite");

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
        } else {
            // Nếu người dùng chưa đăng nhập, tải danh sách giày mà không có thông tin yêu thích
            loadShoesData(new HashSet<>());
        }

        // Lấy dữ liệu cho banner
        bannerReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bannerList.clear();
                for (DataSnapshot bannerSnapshot : snapshot.getChildren()) {
                    String imageUrl = bannerSnapshot.child("url").getValue(String.class);
                    if (imageUrl != null) {
                        bannerList.add(imageUrl);
                    }
                }
                bannerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi lấy dữ liệu banner", error.toException());
            }
        });
    }

    private void loadShoesData(Set<String> favouriteProductIds) {
        allshoesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                allshoesList.clear();
                popularshoeList.clear(); // Xóa danh sách giày phổ biến trước khi thêm mới
                int allCount = 0, popularCount = 0;

                for (DataSnapshot shoeSnapshot : snapshot.getChildren()) {
                    Shoe shoe = shoeSnapshot.getValue(Shoe.class);
                    if (shoe != null) {
                        shoe.setProductId(shoeSnapshot.getKey()); // Lấy key làm productId
                        shoe.setFavourite(favouriteProductIds.contains(shoe.getProductId()));

                        // Thêm vào danh sách tất cả giày
                        allshoesList.add(shoe);
                        allCount++;

                        // Nếu là giày phổ biến, thêm vào danh sách giày phổ biến
                        if (Boolean.TRUE.equals(shoe.getIsPopular())) {
                            popularshoeList.add(shoe);
                            popularCount++;
                        }
                    }
                }

                popularShoesRecyclerView.setAdapter(popularShoeAdapter);
                allShoesRecyclerView.setAdapter(allShoeAdapter);

                // Thông báo rằng dữ liệu đã thay đổi
                popularShoeAdapter.notifyDataSetChanged();
                allShoeAdapter.notifyDataSetChanged();

                Log.d("COUNT", "all: " + allCount + " popular: " + popularCount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi khi lấy dữ liệu giày", error.toException());
            }
        });
    }
    public void updateAdapters() {
        if (popularShoeAdapter != null) {
            popularShoeAdapter.notifyDataSetChanged();
        }
        if (allShoeAdapter != null) {
            allShoeAdapter.notifyDataSetChanged();
        }
    }
}
