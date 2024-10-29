package com.example.footfitstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.example.footfitstore.R;
import com.example.footfitstore.fragment.CartFragment;
import com.example.footfitstore.fragment.ExploreFragment;
import com.example.footfitstore.fragment.FavouriteFragment;
import com.example.footfitstore.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_EDIT_PROFILE = 1001;
    private FrameLayout mainFrame;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mainFrame = findViewById(R.id.main_frame);
        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_explore) {
                    setFragment(new ExploreFragment());
                    return true;
                } else if (itemId == R.id.nav_favourite) {
                    setFragment(new FavouriteFragment());
                    return true;
                } else if (itemId == R.id.nav_cart) {
                    setFragment(new CartFragment());
                    return true;
//                } else if (itemId == R.id.nav_notifications) {
//                    setFragment(new NotificationsFragment());
//                    return true;
                } else if (itemId == R.id.nav_profile) {
                    setFragment(new ProfileFragment());
                    return true;
                } else {
                    return false;
                }
            }
        });

        // Kiểm tra intent để mở fragment cụ thể nếu cần
        Intent intent = getIntent();
        if (intent != null) {
            String openFragment = intent.getStringExtra("openFragment");
            if ("cart".equals(openFragment)) {
                setFragment(new CartFragment());
                bottomNavigationView.setSelectedItemId(R.id.nav_cart);
            } else if ("profile".equals(openFragment)) {
                setFragment(new ProfileFragment());
                bottomNavigationView.setSelectedItemId(R.id.nav_profile);
            } else {
                setFragment(new ExploreFragment());  // Mặc định là ExploreFragment
            }
        } else {
            setFragment(new ExploreFragment());  // Mặc định là ExploreFragment nếu không có intent
        }
    }

    private void setFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.main_frame, fragment);
        fragmentTransaction.commit();
    }

    public void setSelectedNavItem(int itemId) {
        bottomNavigationView.setSelectedItemId(itemId);
    }
}
