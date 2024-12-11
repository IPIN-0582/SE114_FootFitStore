package com.example.footfitstore.fragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.footfitstore.R;
import com.example.footfitstore.adapter.MinimizeCategoryAdapter;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.DefaultAxisValueFormatter;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Formatter;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class StatisticsFragment extends Fragment {
    PieChart pieChart;
    Spinner monthSpinner, yearSpinner, yearSpinner2;
    List<String> monthList = new ArrayList<>(), yearList = new ArrayList<>();
    MinimizeCategoryAdapter monthAdapter, yearAdapter;
    ArrayList<PieEntry> entries= new ArrayList<>();
    String selectedMonth, selectedYear1, selectedYear2;
    List<String> titleList = new ArrayList<>();
    List<Double> priceList = new ArrayList<>();
    BarChart barChart;
    TextView textView;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_statistics, container, false);
        initializeView(view);
        monthAdapter = new MinimizeCategoryAdapter(requireContext(), R.layout.item_category_picked, monthList);
        yearAdapter = new MinimizeCategoryAdapter(requireContext(), R.layout.item_category_picked, yearList);
        getTitleList(task -> {
            if (!titleList.isEmpty()) {
                getDateTimeList(false, task1 -> {
                    if (selectedMonth != null && selectedYear1 != null) {
                        getDataByMonthAndYear(selectedMonth, selectedYear1);
                    }
                });
            } else {
                Toast.makeText(requireContext(),".",Toast.LENGTH_SHORT).show();
            }
        });
        monthSpinner.setAdapter(monthAdapter);
        yearSpinner.setAdapter(yearAdapter);
        yearSpinner2.setAdapter(yearAdapter);
        monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedMonth = monthList.get(position);
                if (selectedMonth!=null && selectedYear1 !=null)
                {
                    getDataByMonthAndYear(selectedMonth, selectedYear1);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        yearSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String newSelectedYear = yearList.get(position);
                if (!newSelectedYear.equals(selectedYear1)) {
                    selectedYear1 = newSelectedYear;
                    getDateTimeList(true, task1 -> {
                        if (selectedMonth != null && selectedYear1 != null) {
                            getDataByMonthAndYear(selectedMonth, selectedYear1);
                        }
                    });
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        yearSpinner2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedYear2 = yearList.get(position);
                getDataForBarChart(selectedYear2);
                textView.setText("Revenue in " + selectedYear2);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        return view;
    }
    private void initializeView(View view)
    {
           pieChart = view.findViewById(R.id.productChart);
           monthSpinner = view.findViewById(R.id.monthSpinner);
           yearSpinner = view.findViewById(R.id.yearSpinner);
           yearSpinner2 = view.findViewById(R.id.yearSpinner2);
           barChart = view.findViewById(R.id.barChart);
           textView = view.findViewById(R.id.txtRevenue);
           barChart.getAxisRight().setDrawLabels(false);
            pieChart.setNoDataText("");
            pieChart.setNoDataTextColor(Color.TRANSPARENT);
            barChart.setNoDataText("");
            barChart.setNoDataTextColor(Color.TRANSPARENT);
    }
    private void getDateTimeList(boolean isChange, OnCompleteListener<Void> onCompleteListener)
    {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                yearList.clear();
                Set<String> monthsSet = new HashSet<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    DataSnapshot ordersSnapshot = dataSnapshot.child("order");
                    for (DataSnapshot orderSnapshot : ordersSnapshot.getChildren())
                    {
                        String status = orderSnapshot.child("orderStatus").getValue(String.class);
                        assert status != null;
                        if (status.equals("ARRIVED") || status.equals("REVIEWED"))
                        {
                            String orderDate = orderSnapshot.child("orderTime").getValue(String.class);
                            String orderYear = convertYear(orderDate);
                            String orderMonth = convertMonth(orderDate);
                            if (!yearList.contains(orderYear))
                            {
                                yearList.add(orderYear);
                            }
                            if (selectedYear1 ==null) selectedYear1 = orderYear;
                            if (orderYear.equals(selectedYear1)) {
                                monthsSet.add(orderMonth);
                            }
                        }
                    }
                }
                yearAdapter.notifyDataSetChanged();
                monthList.clear();
                if (!monthsSet.isEmpty())
                {
                    monthList.addAll(monthsSet);
                    Collections.sort(monthList);
                    if (selectedMonth == null || isChange)
                    {
                        selectedMonth = monthList.get(0);
                    }
                }
                monthAdapter.notifyDataSetChanged();
                if (onCompleteListener != null) {
                    onCompleteListener.onComplete(null); // Báo hiệu hoàn tất
                }
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (onCompleteListener != null) {
                    onCompleteListener.onComplete(null); // Gọi callback để tiếp tục luồng xử lý
                }

            }
        });
    }
    private String convertYear(String inputDate)
    {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            Date start = sdf.parse(inputDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            int year = calendar.get(Calendar.YEAR);
            return String.valueOf(year);
        }
        catch (Exception e)
        {
            return "";
        }
    }
    private String convertMonth (String inputDate)
    {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault());
            Date start = sdf.parse(inputDate);
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            int month = calendar.get(Calendar.MONTH);
            month++;
            return String.valueOf(month);
        }
        catch (Exception e)
        {
            return "";
        }
    }
    private void getDataByMonthAndYear(String month, String year)
    {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                entries.clear();
                priceList.replaceAll(ignored -> 0d);
                for (DataSnapshot dataSnapshot : snapshot.getChildren())
                {
                    DataSnapshot ordersSnapshot = dataSnapshot.child("order");
                    for (DataSnapshot orderSnapshot : ordersSnapshot.getChildren())
                    {
                        String status = orderSnapshot.child("orderStatus").getValue(String.class);
                        assert status != null;
                        if (status.equals("ARRIVED") || status.equals("REVIEWED"))
                        {
                            String orderDate = orderSnapshot.child("orderTime").getValue(String.class);
                            if (month.equals(convertMonth(orderDate)) && year.equals(convertYear(orderDate)))
                            {
                                for (DataSnapshot snapshot1 : orderSnapshot.child("carList").getChildren())
                                {
                                    if (!priceList.isEmpty() && !titleList.isEmpty())
                                    {
                                        String productName = snapshot1.child("productName").getValue(String.class);
                                        int index = titleList.indexOf(productName);
                                        Double price = snapshot1.child("price").getValue(Double.class);
                                        int quantity = snapshot1.child("quantity").getValue(Integer.class);
                                        Double currentPrice = priceList.get(index);
                                        priceList.set(index, currentPrice+ price*quantity);
                                    }
                                }
                            }
                        }
                    }
                }
                for (int i=0; i<titleList.size(); i++)
                {
                    if (priceList.get(i) != 0)
                    {
                        entries.add(new PieEntry(priceList.get(i).floatValue(), titleList.get(i)));
                    }
                }
                PieDataSet pieDataSet = new PieDataSet(entries, "");
                Legend legend = pieChart.getLegend();
                legend.setTextSize(20f);
                pieDataSet.setValueTextSize(18f);
                pieDataSet.setValueTextColor(Color.BLACK);
                pieChart.setEntryLabelTextSize(16f);
                pieChart.setEntryLabelColor(Color.BLACK);
                pieDataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                PieData pieData = new PieData(pieDataSet);
                pieChart.setData(pieData);
                pieChart.getDescription().setEnabled(false);
                pieChart.animateY(500);
                pieChart.invalidate();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    private void getTitleList(OnCompleteListener<Void> onCompleteListener)
    {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Shoes");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot shoeSnapShot : snapshot.getChildren())
                {
                    titleList.add(shoeSnapShot.child("title").getValue(String.class));
                    priceList.add(0d);
                }
                if (onCompleteListener != null) {
                    onCompleteListener.onComplete(null); // Gọi callback khi hoàn thành
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                if (onCompleteListener != null) {
                    onCompleteListener.onComplete(null); // Vẫn gọi callback để tránh bị treo
                }
            }
        });
    }
    private void getDataForBarChart(String selectedYear2)
    {
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("Users");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<BarEntry> barEntries = new ArrayList<>();
                List<Pair<String, Double>> monthsList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    DataSnapshot ordersSnapshot = dataSnapshot.child("order");
                    for (DataSnapshot orderSnapshot : ordersSnapshot.getChildren()) {
                        String status = orderSnapshot.child("orderStatus").getValue(String.class);
                        assert status != null;
                        if (status.equals("ARRIVED") || status.equals("REVIEWED")) {
                            String orderDate = orderSnapshot.child("orderTime").getValue(String.class);
                            if (selectedYear2.equals(convertYear(orderDate)))
                            {
                                String month = convertMonth(orderDate);
                                Double transactionValue = orderSnapshot.child("transaction").getValue(Double.class);
                                if (transactionValue == null) transactionValue = 0.0;
                                boolean found = false;
                                for (int i = 0; i < monthsList.size(); i++) {
                                    Pair<String, Double> pair = monthsList.get(i);
                                    if (pair.first.equals(month)) {
                                        double newTotal = pair.second + transactionValue;
                                        monthsList.set(i, new Pair<>(month, newTotal));
                                        found = true;
                                        break;
                                    }
                                }
                                if (!found) {
                                    monthsList.add(new Pair<>(month, transactionValue));
                                }
                            }
                        }
                    }
                }
                monthsList.sort(Comparator.comparing(pair -> pair.first));
                for (int i=0; i<monthsList.size(); i++)
                {
                    barEntries.add(new BarEntry((float) i, monthsList.get(i).second.floatValue()));
                }
                YAxis yAxis = barChart.getAxisLeft();
                yAxis.setAxisMinimum(0f);
                yAxis.setAxisMaximum(1000f);
                yAxis.setAxisLineWidth(2f);
                yAxis.setAxisLineColor(Color.BLACK);
                yAxis.setLabelCount(10);
                yAxis.setTextSize(18f); // Tăng kích thước chữ trên trục Y
                yAxis.setDrawGridLines(false); // Loại bỏ grid lines trên trục Y

                YAxis yAxisRight = barChart.getAxisRight();
                yAxisRight.setDrawGridLines(false); // Loại bỏ grid lines trên trục Y bên phải

                List<String> monthList = new ArrayList<>();
                for (Pair<String, Double> pair : monthsList) {
                    monthList.add(pair.first);
                }

                BarDataSet dataSet = new BarDataSet(barEntries, "Months");
                dataSet.setColors(ColorTemplate.MATERIAL_COLORS);
                dataSet.setValueTextSize(16f); // Tăng kích thước chữ trên các giá trị thanh

                BarData barData = new BarData(dataSet);
                barChart.setData(barData);
                barChart.setDragEnabled(false);
                barChart.setScaleEnabled(false);
                barChart.setTouchEnabled(false);
                barChart.setHighlightPerTapEnabled(false);
                barChart.setHighlightPerDragEnabled(false);
                barChart.getDescription().setEnabled(false);
                barChart.invalidate();
                XAxis xAxis = barChart.getXAxis();
                xAxis.setValueFormatter(new IndexAxisValueFormatter(monthList));
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setGranularity(1f);
                xAxis.setGranularityEnabled(true);
                xAxis.setTextSize(18f); // Tăng kích thước chữ trên trục X
                xAxis.setDrawGridLines(false); // Loại bỏ grid lines trên trục X

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}