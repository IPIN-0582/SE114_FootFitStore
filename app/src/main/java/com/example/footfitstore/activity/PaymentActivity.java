package com.example.footfitstore.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.footfitstore.Api.CreateOrder;
import com.example.footfitstore.R;
import com.example.footfitstore.adapter.PaymentAdapter;
import com.example.footfitstore.model.Cart;
import com.example.footfitstore.model.PaymentMethod;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import vn.zalopay.sdk.Environment;
import vn.zalopay.sdk.ZaloPayError;
import vn.zalopay.sdk.ZaloPaySDK;
import vn.zalopay.sdk.listeners.PayOrderListener;

public class PaymentActivity extends AppCompatActivity {
    int selecteditem=-1;
    double finalprice;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private DatabaseReference userCartRef;
    private FirebaseUser currentUser;
    private List<Cart> cartList = new ArrayList<>();
    Spinner spinner;
    List<PaymentMethod> methodList=new ArrayList<>();
    PaymentAdapter paymentAdapter;
    TextView txtEmail,txtPhone,txtAddr,txtProduct,txtDelivery,txtTotal;
    ImageButton btnEditEmail,btnEditPhone,btnBack;
    Button btnCheckout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Intent intent=getIntent();
        double productPrice =intent.getDoubleExtra("total",0);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();

        txtEmail=findViewById(R.id.txt_Email);
        txtPhone=findViewById(R.id.txt_Phone);
        txtAddr=findViewById(R.id.txt_Address);
        txtProduct=findViewById(R.id.txt_productPrice);
        txtDelivery=findViewById(R.id.txt_deliveryFee);
        txtTotal=findViewById(R.id.txt_totalPrice);
        btnEditEmail=findViewById(R.id.btn_edit_email);
        btnEditPhone=findViewById(R.id.btn_edit_phone);
        btnCheckout=findViewById(R.id.button2);
        btnBack=findViewById(R.id.btnBack);
        spinner=findViewById(R.id.spinner);

        btnEditEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtEmail.setFocusable(true);
                txtEmail.setEnabled(true);
                txtEmail.setClickable(true);
                txtEmail.setFocusableInTouchMode(true);
            }
        });
        btnEditPhone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtPhone.setFocusable(true);
                txtPhone.setEnabled(true);
                txtPhone.setClickable(true);
                txtPhone.setFocusableInTouchMode(true);
            }
        });
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearFinalList();
                finish();
            }
        });
        currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            userCartRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(currentUser.getUid())
                    .child("FinalOrder");

            loadCartData();
        }

        txtProduct.setText("Total: $" + productPrice);
        txtDelivery.setText("Total: $" + 15.0);
        finalprice=productPrice+15.0;
        txtTotal.setText("Total: $"+finalprice);
        methodList.add(new PaymentMethod("ZaloPay", R.drawable.zalopay));
        methodList.add(new PaymentMethod("Cash on Delivery",R.drawable.cod));
        paymentAdapter=new PaymentAdapter(R.layout.layout_payment_method,PaymentActivity.this, methodList);
        spinner.setAdapter(paymentAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selecteditem=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        btnCheckout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selecteditem == 1)
                {
                    paymentSuccess();
                }
                else if (selecteditem == 0)
                {
                    CreateOrder orderApi = new CreateOrder();
                    String totalString = String.format("%.0f", finalprice);
                    try {
                        JSONObject data = orderApi.createOrder(totalString);
                        String code = data.getString("returncode");
                        if (code.equals("1")) {
                            String token = data.getString("zptranstoken");
                            ZaloPaySDK.getInstance().payOrder(PaymentActivity.this, token, "demozpdk://app", new PayOrderListener() {
                                @Override
                                public void onPaymentSucceeded(String s, String s1, String s2) {
                                    paymentSuccess();
                                }
                                @Override
                                public void onPaymentCanceled(String s, String s1) {
                                    createDialog("cancelled");
                                }
                                @Override
                                public void onPaymentError(ZaloPayError zaloPayError, String s, String s1) {
                                    createDialog("error");
                                }
                            });
                        }

                    } catch (Exception e) {
                        createDialog("error");
                    }
                }
                else
                {
                    Toast.makeText(PaymentActivity.this,"Vui long chon phuong thuc thanh toan",Toast.LENGTH_SHORT).show();
                }
            }
        });
      setValueActivity();
        StrictMode.ThreadPolicy policy = new
                StrictMode.ThreadPolicy.Builder().permitAll().build();

        StrictMode.setThreadPolicy(policy);
        // ZaloPay SDK Init
        ZaloPaySDK.init(553, Environment.SANDBOX);
    }
    private void setValueActivity() {
        String userUid = mAuth.getCurrentUser().getUid();
        mDatabase.child("Users").child(userUid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                txtEmail.setText(dataSnapshot.child("email").getValue(String.class));
                txtPhone.setText(dataSnapshot.child("mobileNumber").getValue(String.class));
                txtAddr.setText(dataSnapshot.child("address").getValue(String.class));
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            // Xử lý lỗi khi không thể truy cập dữ liệu
                Toast.makeText(PaymentActivity.this, "Lỗi khi lấy dữ liệu", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadCartData() {
        userCartRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cartList.clear();

                for (DataSnapshot cartSnapshot : dataSnapshot.getChildren()) {
                    Cart cart = cartSnapshot.getValue(Cart.class);
                    cartList.add(cart);
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(PaymentActivity.this, "Failed to load cart data", Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void paymentSuccess()
    {

        String userUid = mAuth.getCurrentUser().getUid();
        DatabaseReference orderRef=mDatabase.child("Users")
                .child(userUid)
                .child("order");

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                long childrenCount = snapshot.getChildrenCount();
                String currentOrder = "order_" + childrenCount;
                String formattedDateTime="";
                LocalDateTime currentDateTime;
                DateTimeFormatter formatter;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    currentDateTime = LocalDateTime.now();
                    formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                    formattedDateTime= currentDateTime.format(formatter);
                }
                orderRef.child(currentOrder).child("orderTime").setValue(formattedDateTime);
                for (int i=0;i<cartList.size();i++)
                {
                    Cart cart = cartList.get(i);
                    String productKey = cart.getProductId() + "_" + cart.getSize();
                    Map<String, Object> cartItem = new HashMap<>();
                    cartItem.put("price",cart.getPrice());
                    cartItem.put("productId",cart.getProductId());
                    cartItem.put("productName",cart.getProductName());
                    cartItem.put("quantity",cart.getQuantity());
                    cartItem.put("size",cart.getSize());
                    orderRef.child(currentOrder).child("carList").child(productKey).setValue(cartItem);
                    DatabaseReference cartRef = FirebaseDatabase.getInstance()
                            .getReference("Users")
                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                            .child("cart")
                            .child(productKey);
                    cartRef.removeValue();
                }
                cartList.clear();
                orderRef.child(currentOrder).child("orderStatus").setValue("SUCCESS");
                clearFinalList();
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        AlertDialog.Builder builder=new AlertDialog.Builder(PaymentActivity.this,R.style.CustomAlertDialog);
        final View customLayout = getLayoutInflater().inflate(R.layout.dialog_payment_success,null);
        builder.setView(customLayout);
        Button positiveButton = customLayout.findViewById(R.id.pos_button);
        AlertDialog alertDialog=builder.create();
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent (PaymentActivity.this,MainActivity.class);
                startActivity(intent);
                PaymentActivity.this.finish();
            }
        });
        builder.show();
    }
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        ZaloPaySDK.getInstance().onResult(intent);
    }
    private void createDialog(String s) {
        AlertDialog.Builder builder = new AlertDialog.Builder(PaymentActivity.this, R.style.CustomAlertDialog);
        final View customLayout;
        if (s.equals("cancelled")) {
            customLayout = getLayoutInflater().inflate(R.layout.dialog_payment_cancelled, null);
        } else {
            customLayout = getLayoutInflater().inflate(R.layout.dialog_payment_error, null);
        }
        builder.setView(customLayout);
        Button positiveButton = customLayout.findViewById(R.id.pos_button);
        AlertDialog alertDialog = builder.create();
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
            }
        });
        alertDialog.show();
    }
    private void clearFinalList()
    {
        String userId=FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference finalOrder = FirebaseDatabase.getInstance().getReference().child("Users").child(userId).child("FinalOrder");
        finalOrder.removeValue();
    }
}