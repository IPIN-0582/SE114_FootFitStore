package com.example.footfitstore.fragment.User;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.Utils.CustomDialog;
import com.example.footfitstore.activity.User.MainActivity;
import com.example.footfitstore.adapter.UserSideAdapter.ShoeAdapter;
import com.example.footfitstore.adapter.UserSideAdapter.SizeAdapter;
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
                    new CustomDialog(requireContext())
                            .setTitle("Failed")
                            .setMessage("Please Select A Size.")
                            .setIcon(R.drawable.error)
                            .setPositiveButton("OK", null)
                            .hideNegativeButton()
                            .show();
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

        favouriteShoeAdapter = new ShoeAdapter(getContext(), null, favouriteshoeList, "all",this,this);
        favouriteShoesRecyclerView.setAdapter(favouriteShoeAdapter);

        btnBack.setOnClickListener(v -> {
            ExploreFragment exploreFragment = new  ExploreFragment();

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, exploreFragment)
                    .addToBackStack(null)
                    .commit();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedNavItem(R.id.nav_explore);
            }
        });

        btnCart.setOnClickListener(v -> {
            CartFragment cartFragment = new CartFragment();

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, cartFragment)
                    .addToBackStack(null)
                    .commit();

            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedNavItem(R.id.nav_cart);
            }
        });

        allshoesReference = FirebaseDatabase.getInstance().getReference("Shoes");

        loadDataFromFirebase();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDataFromFirebase();
    }


    private void loadDataFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
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

                loadShoesData(favouriteProductIds);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                loadShoesData(new HashSet<>());
            }
        });
    }

    private void loadShoesData(Set<String> favouriteProductIds) {
        if (favouriteProductIds.isEmpty()) {
            return;
        }
        allshoesReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange (@NonNull DataSnapshot snapshot){
                favouriteshoeList.clear();
                for (DataSnapshot shoeSnapshot : snapshot.getChildren()) {
                    Shoe shoe = shoeSnapshot.getValue(Shoe.class);
                    if (shoe != null && favouriteProductIds.contains(shoeSnapshot.getKey())) {
                        shoe.setProductId(shoeSnapshot.getKey());
                        shoe.setFavourite(true);
                        favouriteshoeList.add(shoe);
                    }
                }
                favouriteShoeAdapter.notifyDataSetChanged();
        }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
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
                    price = dataSnapshot.child("price").getValue(Double.class);
                    List<String> sizes = new ArrayList<>();
                    for (DataSnapshot sizeSnapshot : dataSnapshot.child("size").getChildren()) {
                        sizes.add(sizeSnapshot.getValue(String.class));
                    }
                    setupSizeRecyclerView(sizes);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                new CustomDialog(requireContext())
                        .setTitle("Failed")
                        .setMessage("Failed To Load Product Data.")
                        .setIcon(R.drawable.error)
                        .setPositiveButton("OK", null)
                        .hideNegativeButton()
                        .show();
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
        String cartKey = productId + "_" + selectedSize;

        userCartRef.child(cartKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int currentQuantity = dataSnapshot.child("quantity").getValue(Integer.class);
                    userCartRef.child(cartKey).child("quantity").setValue(currentQuantity + 1)
                            .addOnSuccessListener(aVoid -> new CustomDialog(requireContext())
                                    .setTitle("Success")
                                    .setMessage("Successfully Add To Cart")
                                    .setIcon(R.drawable.congrat)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show())
                            .addOnFailureListener(e -> new CustomDialog(requireContext())
                                    .setTitle("Failed")
                                    .setMessage("Failed From Add To Cart")
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
                            .addOnSuccessListener(aVoid -> new CustomDialog(requireContext())
                                    .setTitle("Success")
                                    .setMessage("Successfully Add To Cart")
                                    .setIcon(R.drawable.congrat)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show())
                            .addOnFailureListener(e -> new CustomDialog(requireContext())
                                    .setTitle("Failed")
                                    .setMessage("Failed From Add To Cart")
                                    .setIcon(R.drawable.error)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                new CustomDialog(requireContext())
                        .setTitle("Failed")
                        .setMessage("Failed From Fetch Data")
                        .setIcon(R.drawable.error)
                        .setPositiveButton("OK", null)
                        .hideNegativeButton()
                        .show();
            }
        });
    }
}