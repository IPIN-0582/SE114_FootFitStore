package com.example.footfitstore.fragment;

import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.example.footfitstore.R;
import com.example.footfitstore.Utils.CreateAlertDialog;
import com.example.footfitstore.activity.MainActivity_Admin;
import com.example.footfitstore.adapter.MinimizeShoeAdapter;
import com.example.footfitstore.adapter.NotificationAdminAdapter;
import com.example.footfitstore.model.MinimizeShoe;
import com.example.footfitstore.model.NotificationAdmin;
import com.google.android.material.slider.Slider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;

public class PromotionFragmentAdmin extends Fragment {
    Button btnSubmit;
    ImageView btnBack;
    int promotionValue;
    ImageButton datePicker1, datePicker2;
    TextView startDate, endDate;
    Slider promotionPick;
    TextView promotionDisplay;
    String startPromoDate, endPromoDate;
    List<MinimizeShoe> minimizeShoeList = new ArrayList<>();
    MinimizeShoeAdapter minimizeShoeAdapter;
    Spinner spinner;
    String selectedProductId = "";
    RecyclerView recyclerView;
    NotificationAdminAdapter notificationAdminAdapter;
    List<NotificationAdmin> notificationAdminList = new ArrayList<>();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_promotion_admin, container, false);
        datePicker1 = view.findViewById(R.id.date_picker_1);
        datePicker2 = view.findViewById(R.id.date_picker_2);
        startDate = view.findViewById(R.id.start_date);
        endDate = view.findViewById(R.id.end_date);
        promotionPick = view.findViewById(R.id.promo_pick);
        promotionDisplay = view.findViewById(R.id.promotion);
        spinner = view.findViewById(R.id.shoeslist);
        btnBack = view.findViewById(R.id.btnBack);
        recyclerView = view.findViewById(R.id.promotionList);
        btnSubmit = view.findViewById(R.id.submit_button);
        CreateAlertDialog alertDialog = new CreateAlertDialog(getContext());
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
        notificationAdminAdapter = new NotificationAdminAdapter(getContext(), notificationAdminList);
        minimizeShoeAdapter = new MinimizeShoeAdapter(getContext(), R.layout.item_shoe_minimize_selected, minimizeShoeList);
        promotionPick.addOnChangeListener(new Slider.OnChangeListener() {
            @Override
            public void onValueChange(@NonNull Slider slider, float value, boolean fromUser) {
                promotionValue = (int)value;
                promotionDisplay.setText(promotionValue + "%");
            }
        });
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        startPromoDate = day + "-" + (month +1) + "-" + year;
        endPromoDate = day + "-" + (month+1) + "-" + year;
        startDate.setText(startPromoDate);
        endDate.setText(endPromoDate);
        datePicker1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getActivity(), new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                startPromoDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                                startDate.setText(startPromoDate);
                            }
                        }, year, month, day);
                datePickerDialog.show();
            }
        });
        datePicker2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(
                        getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year,
                                          int monthOfYear, int dayOfMonth) {
                        endPromoDate = dayOfMonth + "-" + (monthOfYear + 1) + "-" + year;
                        endDate.setText(endPromoDate);
                    }
                }, year, month, day);
                datePickerDialog.show();
            }
        });
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (promotionValue == 0)
                {
                    alertDialog.createDialog("Please Select a promotion value");
                    return;
                }
                SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
                Date start,end;
                try {
                    start = sdf.parse(startPromoDate);
                    end = sdf.parse(endPromoDate);
                } catch (ParseException e) {
                    throw new RuntimeException(e);
                }
                Date today = new Date();
                today = resetTime(today);
                if (start !=null && end !=null)
                {
                    if (today.after(start) || today.after(end) || start.after(end))
                    {
                        alertDialog.createDialog("Invalid Date");
                        return;
                    }
                }
                checkExistPromotion(alertDialog, startPromoDate, exists -> {
                    if (!exists) {
                        String outputStartDate = "", outputEndDate = "";
                        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
                        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
                        Date dateStart, dateEnd;
                        try {
                            dateStart = inputFormat.parse(startPromoDate);
                            dateEnd = inputFormat.parse(endPromoDate);
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                        outputStartDate = outputFormat.format(dateStart);
                        outputEndDate = outputFormat.format(dateEnd);
                        String finalOutputStartDate = outputStartDate;
                        String finalOutputEndDate = outputEndDate;
                        addPromotion(finalOutputStartDate, finalOutputEndDate);
                    }
                });
            }
        });
        getMinizeShoeList();
        getNotification();
        recyclerView.setAdapter(notificationAdminAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        spinner.setAdapter(minimizeShoeAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedProductId = minimizeShoeList.get(position).getProductId();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }

    private void getMinizeShoeList()
    {
        DatabaseReference shoeRef = FirebaseDatabase.getInstance().getReference("Shoes");
        shoeRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                minimizeShoeList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    MinimizeShoe minimizeShoe = new MinimizeShoe();
                    if (dataSnapshot.child("picUrl").child("0").getValue(String.class) !=null)
                    {
                        minimizeShoe.setAvatarUrl(dataSnapshot.child("picUrl").child("0").getValue(String.class));
                    }
                    if (dataSnapshot.child("title").getValue(String.class) != null)
                    {
                        minimizeShoe.setShoeName(dataSnapshot.child("title").getValue(String.class));
                    }
                    if (dataSnapshot.getKey() != null)
                    {
                        minimizeShoe.setProductId(dataSnapshot.getKey());
                    }
                    minimizeShoeList.add(minimizeShoe);
                }
                minimizeShoeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getNotification()
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Shoes");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                notificationAdminList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    if (dataSnapshot.child("promotion").exists()) {
                        NotificationAdmin notificationAdmin = new NotificationAdmin();
                        String title = dataSnapshot.child("title").getValue(String.class);
                        String description = dataSnapshot.child("promotion").child("description").getValue(String.class);
                        String imgUrl = dataSnapshot.child("picUrl").child("0").getValue(String.class);
                        String startDate = dataSnapshot.child("promotion").child("startDate").getValue(String.class);
                        String endDate = dataSnapshot.child("promotion").child("endDate").getValue(String.class);
                        notificationAdmin.setProductId(title);
                        notificationAdmin.setDescription(description);
                        notificationAdmin.setImgUrl(imgUrl);
                        notificationAdmin.setStartDate(startDate);
                        notificationAdmin.setEndDate(endDate);
                        notificationAdminList.add(notificationAdmin);
                    }
                }
                notificationAdminAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private static Date resetTime(Date date) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }
    private void checkExistPromotion(CreateAlertDialog alertDialog, String startPromoDate, PromotionCheckCallback callback)
    {
        String outputStartDate = "";
        SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date dateStart;
        try {
            dateStart = inputFormat.parse(startPromoDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        outputStartDate = outputFormat.format(dateStart);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Shoes").child(selectedProductId);
        String finalOutputStartDate = outputStartDate;
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Date startFinal, end = null;
                boolean exists = false;
                if (snapshot.child("promotion").exists())
                {
                    String endDate = snapshot.child("promotion").child("endDate").getValue(String.class);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                    try {
                        startFinal = sdf.parse(finalOutputStartDate);
                        if (endDate != null)
                        {
                            end = sdf.parse(endDate);
                        }
                    } catch (ParseException e) {
                        throw new RuntimeException(e);
                    }
                    if (end != null && (Objects.requireNonNull(startFinal).before(end) || Objects.requireNonNull(startFinal).equals(end)))
                    {
                        alertDialog.createDialog("Promotion's already exists");
                        exists = true;
                    }
                }
                callback.onResult(exists);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onResult(false);
            }
        });
    }
    private void addPromotion(String startDate, String endDate)
    {
        DatabaseReference promotionRef = FirebaseDatabase.getInstance().getReference("Shoes").child(selectedProductId).child("promotion");
        List<String> descriptionList = new ArrayList<>();
        descriptionList.add(promotionValue+"% DISCOUNT from "+startDate+" to "+endDate+" BUY NOW!!!");
        descriptionList.add("BIG DISCOUNT from "+ startDate + " to " +endDate + " UP TO "+promotionValue+ " %!!!");
        descriptionList.add("ONLY from "+startDate + " to " + endDate + " with "+ promotionValue + " % !!!");
        Random random = new Random();
        int randomNumber = random.nextInt(3);
        String finalDescription = descriptionList.get(randomNumber);
        promotionRef.child("description").setValue(finalDescription);
        promotionRef.child("discount").setValue(promotionValue);
        promotionRef.child("startDate").setValue(startDate);
        promotionRef.child("endDate").setValue(endDate);
    }
    public interface PromotionCheckCallback {
        void onResult(boolean exists);
    }
}