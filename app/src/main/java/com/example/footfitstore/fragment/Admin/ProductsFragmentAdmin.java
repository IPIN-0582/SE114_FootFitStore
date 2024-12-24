package com.example.footfitstore.fragment.Admin;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.footfitstore.R;
import com.example.footfitstore.Utils.CustomDialog;
import com.example.footfitstore.activity.Admin.MainActivity_Admin;
import com.example.footfitstore.adapter.AdminSideAdapter.MinimizeCategoryAdapter;
import com.example.footfitstore.adapter.AdminSideAdapter.ShoeProductAdapter;
import com.example.footfitstore.Utils.SimpleSpaceItemDecoration;
import com.example.footfitstore.model.Shoe;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class ProductsFragmentAdmin extends Fragment {
    ImageButton btnBack;
    EditText edt_Title, edt_Price, edt_Description;
    Spinner spinner_Price;
    ImageView imgView;
    Button submit_AddImg, submit_AddShoes;
    RecyclerView recyclerView_Shoes;
    List<String> categoryList = new ArrayList<>();
    MinimizeCategoryAdapter minimizeCategoryAdapter;
    String selectedCategory;
    List<Shoe> shoeList = new ArrayList<>();
    ShoeProductAdapter shoeProductAdapter;
    Uri avatarUri;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_products_admin, container, false);
        initializeView(view);
        getDataFromFirebase();
        shoeProductAdapter = new ShoeProductAdapter(requireContext(), shoeList);
        minimizeCategoryAdapter = new MinimizeCategoryAdapter(requireContext(), R.layout.item_category_picked, categoryList);
        spinner_Price.setAdapter(minimizeCategoryAdapter);
        spinner_Price.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categoryList.get(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnBack.setOnClickListener(v -> {
            UsersFragmentAdmin usersFragmentAdmin = new UsersFragmentAdmin();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.frame, usersFragmentAdmin)
                    .addToBackStack(null)
                    .commit();
            if (getActivity() instanceof MainActivity_Admin) {
                ((MainActivity_Admin) getActivity()).setSelectedNavItem(R.id.nav_users);
            }
        });
        submit_AddImg.setOnClickListener(v->{
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Avatar"), 1);
        });
        submit_AddShoes.setOnClickListener(v->uploadShoe());
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        recyclerView_Shoes.setLayoutManager(gridLayoutManager);
        recyclerView_Shoes.addItemDecoration(new SimpleSpaceItemDecoration(16));
        recyclerView_Shoes.setAdapter(shoeProductAdapter);
        return view;
    }

    private void uploadShoe() {
        if (edt_Title.getText().toString().isEmpty() || edt_Description.getText().toString().isEmpty() || edt_Price.getText().toString().isEmpty() || !"hasImage".equals(imgView.getTag()))
        {
            new CustomDialog(requireContext())
                    .setTitle("Failed")
                    .setMessage("Please Fill All Information.")
                    .setIcon(R.drawable.error)
                    .setPositiveButton("OK", null)
                    .hideNegativeButton()
                    .show();
            return;
        }
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setMessage("Uploading product...");
        progressDialog.show();
        Shoe shoe = new Shoe();
        shoe.setTitle(edt_Title.getText().toString());
        shoe.setCategory(selectedCategory);
        shoe.setDescription(edt_Description.getText().toString());
        shoe.setPrice(Double.parseDouble(edt_Price.getText().toString()));
        List<String> sizeList = new ArrayList<>();
        for (int i=38; i<=44;i++)
        {
            sizeList.add(""+i);
        }
        shoe.setFavourite(false);
        shoe.setSize(sizeList);
        shoe.setRating(0.0);
        DatabaseReference shoeRef = FirebaseDatabase.getInstance().getReference("Shoes");
        shoeRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long childrenCount = snapshot.getChildrenCount();
                String productId = "shoe" + childrenCount;
                List<String> url = new ArrayList<>();
                StorageReference storageRef = FirebaseStorage.getInstance().getReference(productId+".png");
                storageRef.putFile(avatarUri)
                        .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            progressDialog.dismiss();
                            String avatarUrl = uri.toString();
                            url.add(avatarUrl);
                            shoe.setProductId(productId);
                            shoe.setPicUrl(url);
                            shoeRef.child(productId).setValue(shoe);
                            new CustomDialog(requireContext())
                                    .setTitle("Success")
                                    .setMessage("Successfully uploaded")
                                    .setIcon(R.drawable.congrat)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show();
                        }))
                        .addOnFailureListener(e -> {
                            progressDialog.dismiss();
                            new CustomDialog(requireContext())
                                    .setTitle("Failed")
                                    .setMessage("Upload failed")
                                    .setIcon(R.drawable.error)
                                    .setPositiveButton("OK", null)
                                    .hideNegativeButton()
                                    .show();
                        });

            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        edt_Description.setText("");
        edt_Price.setText("");
        edt_Title.setText("");
        imgView.setImageDrawable(null);
    }

    private void getDataFromFirebase() {
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("Category");
        categoryRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    categoryList.add(dataSnapshot.child("title").getValue(String.class));
                }
                minimizeCategoryAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        DatabaseReference shoeRef = FirebaseDatabase.getInstance().getReference("Shoes");
        shoeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                shoeList.clear();
                for (DataSnapshot shoeSnapshot : snapshot.getChildren()) {
                    Shoe shoe = shoeSnapshot.getValue(Shoe.class);
                    if (shoe != null) {
                        shoe.setProductId(shoeSnapshot.getKey());
                        shoeList.add(shoe);
                    }
                    shoeProductAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void initializeView(View view) {
        btnBack = view.findViewById(R.id.btnBack);
        edt_Title = view.findViewById(R.id.edt_title);
        edt_Price = view.findViewById(R.id.edt_price);
        edt_Description = view.findViewById(R.id.edt_description);
        spinner_Price = view.findViewById(R.id.categorySpinner);
        imgView = view.findViewById(R.id.img_product);
        submit_AddImg = view.findViewById(R.id.submit_add_image);
        submit_AddShoes = view.findViewById(R.id.submit_add_shoes);
        recyclerView_Shoes = view.findViewById(R.id.recycler_view_products);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            avatarUri = data.getData();
            imgView.setImageURI(avatarUri);
            imgView.setTag("hasImage");
        }
    }
}