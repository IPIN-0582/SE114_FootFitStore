package com.example.footfitstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.footfitstore.R;
import com.example.footfitstore.adapter.OrderMinimizeAdapter;
import com.example.footfitstore.model.OrderHistory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserProfileManagement extends AppCompatActivity {
    Button submit;
    TextView txtName;
    ImageView imgAvatar, btnBack;
    EditText edtAddress, edtGender, edtPhoneNumber, edtMail, edtRole;
    RecyclerView recyclerView;
    List<OrderHistory> orderHistoryList = new ArrayList<>();
    OrderMinimizeAdapter orderMinimizeAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_profile_management);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent = getIntent();
        String userId = intent.getStringExtra("userId");
        initializeView();
        orderMinimizeAdapter = new OrderMinimizeAdapter(this, orderHistoryList);
        getDataFromDb(userId);
        recyclerView.setAdapter(orderMinimizeAdapter);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
                userRef.child("status").setValue("banned");
                finish();
            }
        });
        btnBack.setOnClickListener(v->finish());
    }

    private void getDataFromDb(String userId)
    {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users").child(userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String address = snapshot.child("address").getValue(String.class);
                String phone = snapshot.child("mobileNumber").getValue(String.class);
                String mail = snapshot.child("email").getValue(String.class);
                Integer gender = snapshot.child("gender").getValue(Integer.class);
                String role = snapshot.child("role").getValue(String.class);
                String firstName = snapshot.child("firstName").getValue(String.class);
                String lastName = snapshot.child("lastName").getValue(String.class);
                String name = (firstName != null) ? firstName.concat(" "+lastName) : "";
                String avatarUrl = snapshot.child("avatarUrl").getValue(String.class);
                txtName.setText(name);
                if (address != null) edtAddress.setText(address);
                if (phone != null) edtPhoneNumber.setText(phone);
                edtGender.setText((gender==0)?"Male": "Female");
                if (mail !=null) edtMail.setText(mail);
                edtRole.setText(role);
                if (avatarUrl!=null)
                {
                    Picasso.get().load(avatarUrl).into(imgAvatar);
                }
                else
                {
                    if (gender == 0)
                    {
                        imgAvatar.setImageResource(R.drawable.boy);
                    }
                    else
                    {
                        imgAvatar.setImageResource(R.drawable.girl);
                    }
                }
                for (DataSnapshot dataSnapshot : snapshot.child("order").getChildren())
                {
                    OrderHistory orderHistory = dataSnapshot.getValue(OrderHistory.class);
                    orderHistoryList.add(orderHistory);
                }
                orderMinimizeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void initializeView()
    {
        submit =findViewById(R.id.btnSubmit);
        txtName = findViewById(R.id.tv_full_name);
        btnBack = findViewById(R.id.btn_back_profile);
        edtAddress = findViewById(R.id.address);
        edtPhoneNumber = findViewById(R.id.mobile_number);
        edtGender = findViewById(R.id.gender);
        edtMail = findViewById(R.id.email);
        edtRole = findViewById(R.id.role);
        recyclerView = findViewById(R.id.recycler_order_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        imgAvatar = findViewById(R.id.img_profile_picture);
        edtAddress.setFocusable(false);
        edtAddress.setClickable(true);
        edtPhoneNumber.setFocusable(false);
        edtPhoneNumber.setClickable(true);
        edtGender.setFocusable(false);
        edtGender.setClickable(true);
        edtMail.setFocusable(false);
        edtMail.setClickable(true);
        edtRole.setFocusable(false);
        edtRole.setClickable(true);
    }
}