package com.example.footfitstore.fragment.Admin;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import com.example.footfitstore.R;
import com.example.footfitstore.Utils.CreateAlertDialog;
import com.example.footfitstore.activity.Admin.MainActivity_Admin;
import com.example.footfitstore.adapter.BannerAdapter;
import com.example.footfitstore.adapter.CategoryAdapter;
import com.example.footfitstore.Utils.SimpleSpaceItemDecoration;
import com.example.footfitstore.model.Banner;
import com.example.footfitstore.model.Category;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class CategoriesAndBannersFragment extends Fragment {
    ImageView imgBanner;
    ImageButton imgAddBanner, btnBack;
    Button btnAddBanner, btnAddCategory;
    RecyclerView bannerRecyclerView, categoryRecyclerView;
    EditText editText;
    private BannerAdapter bannerAdapter;
    private CategoryAdapter categoryAdapter;
    private List<String> categoryList = new ArrayList<>(), bannerList = new ArrayList<>();
    Uri avatarUri;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_categories_and_banners, container, false);
        initializeView(view);
        getDataFromDatabase();
        bannerAdapter = new BannerAdapter(bannerList);
        categoryAdapter = new CategoryAdapter(categoryList, requireContext());
        bannerRecyclerView.setAdapter(bannerAdapter);
        categoryRecyclerView.setAdapter(categoryAdapter);
        bannerRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryRecyclerView.addItemDecoration(new SimpleSpaceItemDecoration(16));
        imgAddBanner.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Avatar"), 1);
        });
        btnAddBanner.setOnClickListener(v -> {
            if ("hasImage".equals(imgBanner.getTag()))
            {
                uploadImageFirebase();
                imgBanner.setImageDrawable(null);
            }
        });
        btnAddCategory.setOnClickListener(v->{
            if (editText.getText().toString().isEmpty())
            {
                CreateAlertDialog createAlertDialog= new CreateAlertDialog(requireContext());
                createAlertDialog.createDialog("Please fill all information");
            }
            else
            {
                uploadCategory(editText.getText().toString());
                editText.setText("");
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
        return view;
    }
    private void initializeView(View view)
    {
        imgBanner = view.findViewById(R.id.imgBanner);
        imgAddBanner = view.findViewById(R.id.button_image);
        btnAddBanner = view.findViewById(R.id.btnAddBanner);
        btnAddCategory = view.findViewById(R.id.btnAddCategory);
        bannerRecyclerView = view.findViewById(R.id.bannerRecyclerView);
        categoryRecyclerView = view.findViewById(R.id.categoryRecyclerView);
        editText = view.findViewById(R.id.categoryName);
        btnBack = view.findViewById(R.id.btnBack);
    }
    private void getDataFromDatabase()
    {
        DatabaseReference bannerRef = FirebaseDatabase.getInstance().getReference("BannerEvent");
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("Category");
        bannerRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                bannerList.clear();
                for (DataSnapshot dataSnapshot:snapshot.getChildren())
                {
                    bannerList.add(dataSnapshot.child("url").getValue(String.class));
                }
                bannerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null && data.getData() != null) {
            avatarUri = data.getData();
            imgBanner.setImageURI(avatarUri);
            imgBanner.setTag("hasImage");
        }
    }
    private void uploadImageFirebase()
    {
        Banner banner = new Banner();
        DatabaseReference bannerReference = FirebaseDatabase.getInstance().getReference("BannerEvent");
        bannerReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long count = snapshot.getChildrenCount();
                count++;
                String newestBanner = "banner"+count;
                count--;
                StorageReference storageRef = FirebaseStorage.getInstance().getReference(newestBanner+".png");
                long finalCount = count;
                storageRef.putFile(avatarUri)
                        .addOnSuccessListener(taskSnapshot -> storageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            String avatarUrl = uri.toString();
                            banner.setUrl(avatarUrl);
                            bannerReference.child(String.valueOf(finalCount)).setValue(banner);
                        }))
                        .addOnFailureListener(e -> Log.d("Yup, something's wrong", "Yup"));

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void uploadCategory(String title)
    {
        Category category = new Category();
        DatabaseReference categoryRef = FirebaseDatabase.getInstance().getReference("Category");
        categoryRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int count = (int)snapshot.getChildrenCount();
                category.setId(count);
                category.setTitle(title);
                categoryRef.child(String.valueOf(snapshot.getChildrenCount())).setValue(category);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}