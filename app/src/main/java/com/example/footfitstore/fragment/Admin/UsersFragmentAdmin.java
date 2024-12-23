package com.example.footfitstore.fragment.Admin;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.footfitstore.R;
import com.example.footfitstore.Utils.OnBackPressedListener;
import com.example.footfitstore.activity.LoginActivity;
import com.example.footfitstore.activity.User.MainActivity;
import com.example.footfitstore.activity.Admin.MainActivity_Admin;
import com.example.footfitstore.activity.Admin.UserProfileManagement;
import com.example.footfitstore.adapter.AdminSideAdapter.UserAdapter;
import com.example.footfitstore.model.User;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersFragmentAdmin extends Fragment implements OnBackPressedListener {
    ImageButton btnMenu;
    RecyclerView recyclerView;
    UserAdapter userAdapter;
    List<User> userList = new ArrayList<>();
    DrawerLayout drawerLayout;
    NavigationView navigationView;
    TextView headerTextView;
    CircleImageView imgHeader;
    TextView totalUserText;
    TextView activeUserText;
    TextView bannedUserText;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_users_admin, container, false);
        btnMenu=view.findViewById(R.id.btnMenu);
        recyclerView = view.findViewById(R.id.recycler_users);
        drawerLayout=view.findViewById(R.id.explore_nav);
        navigationView=view.findViewById(R.id.nav_view);
        totalUserText = view.findViewById(R.id.total_user);
        activeUserText = view.findViewById(R.id.active_user);
        bannedUserText = view.findViewById(R.id.banned_user);
        btnMenu.setOnClickListener(v -> drawerLayout.openDrawer(navigationView));
        View headerView = navigationView.getHeaderView(0);
        headerTextView = headerView.findViewById(R.id.txt_displayName);
        imgHeader = headerView.findViewById(R.id.avatar);
        navigationView.bringToFront();
        navigationView.getMenu().getItem(0).setChecked(true);
        ActionBarDrawerToggle toggle=new ActionBarDrawerToggle(getActivity(),drawerLayout,R.string.navigation_drawer_open,R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                if (item.getItemId() == R.id.nav_discount)
                {
                    if (getActivity() instanceof MainActivity_Admin) {
                        ((MainActivity_Admin) getActivity()).setSelectedNavItem(R.id.nav_discount);
                    }
                }
                else if (item.getItemId() == R.id.nav_product)
                {
                    if (getActivity() instanceof MainActivity_Admin) {
                        ((MainActivity_Admin) getActivity()).setSelectedNavItem(R.id.nav_product);
                    }
                }
                else if (item.getItemId() == R.id.nav_category)
                {
                    if (getActivity() instanceof MainActivity_Admin) {
                        ((MainActivity_Admin) getActivity()).setSelectedNavItem(R.id.nav_category);
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
                else if (item.getItemId() == R.id.nav_order)
                {
                    if (getActivity() instanceof MainActivity_Admin) {
                        ((MainActivity_Admin) getActivity()).setSelectedNavItem(R.id.nav_order);
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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        userAdapter = new UserAdapter(getContext(), userList, new UserAdapter.NavigateProfileManagement() {
            @Override
            public void NavigateToAdmin(User user) {
                Intent intent = new Intent(getActivity(), UserProfileManagement.class);
                intent.putExtra("userId",user.getUserId());
                startActivity(intent);
            }
        });
        loadUser();
        recyclerView.setAdapter(userAdapter);
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
        loadDataFromFirebase();
        return view;
    }

    private void loadUser() {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                userList.clear();
                int totalUsers = 0;
                int activeUsers = 0;
                int bannedUsers = 0;

                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    User user = new User();
                    String userId = userSnapshot.getKey();
                    String firstName = userSnapshot.child("firstName").getValue(String.class);
                    String status = userSnapshot.child("status").getValue(String.class);
                    String lastName = userSnapshot.child("lastName").getValue(String.class);
                    String email = userSnapshot.child("email").getValue(String.class);
                    String phone = userSnapshot.child("mobileNumber").getValue(String.class);
                    String avatarUrl = userSnapshot.child("avatarUrl").getValue(String.class);
                    Integer gender = userSnapshot.child("gender").getValue(Integer.class);
                    String role = userSnapshot.child("role").getValue(String.class);

                    user.setUserId(userId);
                    user.setFirstname(firstName);
                    user.setLastname(lastName);
                    user.setEmail(email);
                    user.setPhone(phone);
                    user.setAvatarUrl(avatarUrl);
                    user.setGender(gender);
                    user.setRole(role);
                    user.setStatus(status);

                    userList.add(user);

                    // Increment counters based on user status
                    totalUsers++;
                    if ("active".equalsIgnoreCase(status)) {
                        activeUsers++;
                    } else if ("banned".equalsIgnoreCase(status)) {
                        bannedUsers++;
                    }
                }

                userAdapter.notifyDataSetChanged();

                // Update UI elements with the counts


                totalUserText.setText(String.valueOf(totalUsers));
                activeUserText.setText(String.valueOf(activeUsers));
                bannedUserText.setText(String.valueOf(bannedUsers));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Handle database error
            }
        });
    }

    private void loadDataFromFirebase() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = user != null ? user.getUid() : null;
        if (uid != null) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users").child(uid);
            databaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    String firstName = snapshot.child("firstName").getValue(String.class) != null ? snapshot.child("firstName").getValue(String.class) : "";
                    String lastName = snapshot.child("lastName").getValue(String.class) != null ? snapshot.child("lastName").getValue(String.class) : "";
                    headerTextView.setText(firstName+" "+lastName);
                    String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);
                    if (avatarUrl != null) {
                        Picasso.get().load(avatarUrl).into(imgHeader);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
    }

    @Override
    public boolean onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
            return true;
        }
        return false;
    }
}