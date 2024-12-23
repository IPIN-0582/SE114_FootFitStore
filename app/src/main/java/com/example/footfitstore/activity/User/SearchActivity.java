package com.example.footfitstore.activity.User;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.footfitstore.R;
import com.example.footfitstore.adapter.UserSideAdapter.SearchShoeAdapter;
import com.example.footfitstore.model.Shoe;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {

    private EditText searchEditText;
    private RecyclerView searchResultsRecyclerView;
    private SearchShoeAdapter searchResultsAdapter;
    private List<Shoe> searchResultsList = new ArrayList<>();
    private DatabaseReference shoesReference;
    private ImageView backButton;
    private TextView noResultsTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);

        searchEditText = findViewById(R.id.searchEditText);
        searchResultsRecyclerView = findViewById(R.id.searchResultsRecyclerView);
        noResultsTextView = findViewById(R.id.noResultsTextView);

        searchResultsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        searchResultsAdapter = new SearchShoeAdapter(searchResultsList, this);
        searchResultsRecyclerView.setAdapter(searchResultsAdapter);

        shoesReference = FirebaseDatabase.getInstance().getReference("Shoes");

        backButton = findViewById(R.id.btnBack);
        backButton.setOnClickListener(v -> finish());

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterShoes(s.toString().toLowerCase());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterShoes(String query) {
        searchResultsList.clear();
        if (query.isEmpty()) {
            searchResultsAdapter.notifyDataSetChanged();
            noResultsTextView.setVisibility(View.GONE);
            return;
        }

        shoesReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot shoeSnapshot : snapshot.getChildren()) {
                    Shoe shoe = shoeSnapshot.getValue(Shoe.class);
                    if (shoe != null && shoe.getTitle().toLowerCase().contains(query)) {
                        searchResultsList.add(shoe);
                    }
                }
                searchResultsAdapter.notifyDataSetChanged();

                if (searchResultsList.isEmpty()) {
                    noResultsTextView.setVisibility(View.VISIBLE);
                } else {
                    noResultsTextView.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }

}