package com.example.footfitstore.fragment;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.footfitstore.R;
import com.example.footfitstore.adapter.UserAdapter;
import com.example.footfitstore.model.User;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UsersFragmentAdmin extends Fragment {
    RecyclerView recyclerView;
    UserAdapter userAdapter;
    List<User> userList = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_admin, container, false);
        recyclerView = view.findViewById(R.id.recycler_users);
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        userAdapter = new UserAdapter(getContext(), userList);
        loadUser();
        recyclerView.setAdapter(userAdapter);
        return view;
    }
    private void loadUser()
    {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren())
                {
                    User user = new User();
                    String firstName = userSnapshot.child("firstName").getValue(String.class);
                    String lastName = userSnapshot.child("lastName").getValue(String.class);
                    String email = userSnapshot.child("email").getValue(String.class);
                    String phone = userSnapshot.child("mobileNumber").getValue(String.class);
                    String avatarUrl = userSnapshot.child("avatarUrl").getValue(String.class);
                    user.setFirstname(firstName);
                    user.setLastname(lastName);
                    user.setEmail(email);
                    user.setPhone(phone);
                    user.setAvatarUrl(avatarUrl);
                    userList.add(user);
                }
                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}