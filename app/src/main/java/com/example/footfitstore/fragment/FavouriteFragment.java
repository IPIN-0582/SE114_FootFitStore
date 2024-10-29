package com.example.footfitstore.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.activity.MainActivity;
import com.example.footfitstore.activity.ProductDetailActivity;
import com.example.footfitstore.adapter.ShoeAdapter;
import com.example.footfitstore.model.Shoe;
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

public class FavouriteFragment extends Fragment {

    private RecyclerView favouriteShoesRecyclerView;
    private ShoeAdapter favouriteShoeAdapter;
    private final List<Shoe> favouriteshoeList = new ArrayList<>();

    private DatabaseReference allshoesReference;
    private ImageButton btnBack;
    private ImageButton btnCart;
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
        favouriteShoeAdapter = new ShoeAdapter(getContext(), null, favouriteshoeList, "all");
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

    @Override
    public void onResume() {
        super.onResume();
        loadDataFromFirebase();  // Tải lại dữ liệu để cập nhật trạng thái yêu thích mới nhất
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
}