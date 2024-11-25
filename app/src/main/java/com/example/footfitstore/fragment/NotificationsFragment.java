package com.example.footfitstore.fragment;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.example.footfitstore.R;
import com.example.footfitstore.activity.MainActivity;
import com.example.footfitstore.activity.ProductDetailActivity;
import com.example.footfitstore.adapter.NotificationAdapter;
import com.example.footfitstore.model.Notification;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class NotificationsFragment extends Fragment {
    private RecyclerView recyclerView;
    private NotificationAdapter adapter;
    private List<Notification> notificationList;
    ImageButton buttonBack;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);
        recyclerView = view.findViewById(R.id.recycler_notification);
        buttonBack = view.findViewById(R.id.btn_Back);
        buttonBack.setOnClickListener(v->{
            ExploreFragment exploreFragment = new  ExploreFragment();
            getParentFragmentManager().beginTransaction()
                    .replace(R.id.main_frame, exploreFragment)
                    .addToBackStack(null)
                    .commit();
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).setSelectedNavItem(R.id.nav_explore);
            }
        });
        notificationList = new ArrayList<>();
        adapter = new NotificationAdapter(getContext(), notificationList, new NotificationAdapter.OnNotificationClickListener() {
            @Override
            public void onNotificationClick(int position) {
                DatabaseReference userReference = FirebaseDatabase.getInstance().getReference()
                        .child("Users")
                        .child(FirebaseAuth.getInstance().getCurrentUser().getUid()).child("promotionRead")
                        .child(notificationList.get(position).getProductId())
                        .child(notificationList.get(position).getEndDate());
                userReference.setValue("READ");
                notificationList.get(position).setRead(true);
                adapter.notifyDataSetChanged();
                Intent intent = new Intent(getContext(), ProductDetailActivity.class);
                intent.putExtra("productId", notificationList.get(position).getProductId()); // Truyền productId vào Intent
                getContext().startActivity(intent);
            }
        });
        getNotifications(notificationList, adapter); // Fetch your notifications
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        return view;
    }

    private void getNotifications(List<Notification> notificationList, NotificationAdapter adapter)
    {
        DatabaseReference shoeReference = FirebaseDatabase.getInstance().getReference().child("Shoes");
        shoeReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot: snapshot.getChildren())
                {
                    if (dataSnapshot.child("promotion").exists())
                    {
                        String productId = dataSnapshot.getKey();
                        String description = dataSnapshot.child("promotion").child("description").getValue(String.class);
                        String imgUrl = dataSnapshot.child("picUrl").child("0").getValue(String.class);
                        String endDate = dataSnapshot.child("promotion").child("endDate").getValue(String.class);
                        checkReadStatus(productId, endDate, isRead -> {
                            Notification newNoti = new Notification();
                            newNoti.setDescription(description);
                            newNoti.setProductId(productId);
                            newNoti.setImgUrl(imgUrl);
                            newNoti.setEndDate(endDate);
                            newNoti.setRead(isRead);

                            notificationList.add(newNoti);
                            adapter.notifyDataSetChanged();
                        });
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void checkReadStatus(String productId, String endDate, Callback<Boolean> callback) {
        DatabaseReference userReference = FirebaseDatabase.getInstance().getReference()
                .child("Users")
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .child("promotionRead").child(productId).child(endDate);

        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String check = snapshot.getValue(String.class);
                if (check != null) {
                    if (check.equals("READ"))
                    {
                        callback.onResult(true);
                    }
                    else
                    {
                        callback.onResult(false);
                    }

                }
                else {
                    callback.onResult(false);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(false);
            }
        });
    }
    public interface Callback<T> {
        void onResult(T result);
    }
}