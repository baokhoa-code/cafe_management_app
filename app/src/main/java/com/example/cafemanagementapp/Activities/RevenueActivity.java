package com.example.cafemanagementapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cafemanagementapp.Adapters.RevenueAdapter;
import com.example.cafemanagementapp.Adapters.SellAdapter;
import com.example.cafemanagementapp.Models.Drink;
import com.example.cafemanagementapp.Models.Revenue;
import com.example.cafemanagementapp.Models.Sell;
import com.example.cafemanagementapp.R;
import com.google.android.material.snackbar.Snackbar;
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

public class RevenueActivity extends AppCompatActivity {
    private EditText revenueStartDate, revenueEndDate;
    private Button btnRevenueClearDate;
    private RecyclerView rclvRevenueList;
    private RevenueAdapter revenueAdapter;
    private TextView revenueTotalPriceOfALl;
    private List<Revenue> revenueList;
    private ProgressDialog progressDialog;
    private int yearCurrent, monthCurrent, dayOfMonthCurrent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_revenue);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("View Revenue");

        initUI();

        revenueStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(RevenueActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int yearChosen, int monthChosen, int dayOfMonth) {

                        String dateFormatted = String.format("%02d/%02d/%d", dayOfMonth, monthChosen, yearChosen);
                        revenueStartDate.setText(dateFormatted);
                        if(!isNullOrEmpty(revenueEndDate.getText().toString())){
                            if(isValidRange(dateFormatted,revenueEndDate.getText().toString())){
                                getData(dateFormatted,revenueEndDate.getText().toString() );
                            }else{
                                String currentDate = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);
                                revenueStartDate.setText(currentDate);
                                revenueEndDate.setText(currentDate);
                                getData(currentDate, currentDate);
                            }
                        }
                    }
                }, yearCurrent, monthCurrent, dayOfMonthCurrent);
                datePickerDialog.show();
            }
        });

        revenueEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(RevenueActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int yearChosen, int monthChosen, int dayOfMonth) {

                        String dateFormatted = String.format("%02d/%02d/%d", dayOfMonth, monthChosen, yearChosen);
                        revenueEndDate.setText(dateFormatted);
                        if(!isNullOrEmpty(revenueStartDate.getText().toString())){
                            if(isValidRange(revenueStartDate.getText().toString(), dateFormatted)){
                                getData(revenueStartDate.getText().toString(), dateFormatted);
                            }else {
                                String currentDate = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);
                                revenueStartDate.setText(currentDate);
                                revenueEndDate.setText(currentDate);
                                getData(currentDate, currentDate);
                            }

                        }
                    }
                }, yearCurrent, monthCurrent, dayOfMonthCurrent);
                datePickerDialog.show();
            }
        });

        btnRevenueClearDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentDate = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);
                revenueStartDate.setText(currentDate);
                revenueEndDate.setText(currentDate);
                getData(currentDate, currentDate);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUI(){
        revenueStartDate = findViewById(R.id.revenueStartDate);
        revenueEndDate = findViewById(R.id.revenueEndDate);
        btnRevenueClearDate = findViewById(R.id.btnRevenueClearDate);
        rclvRevenueList = findViewById(R.id.rclvRevenueList);
        revenueTotalPriceOfALl = findViewById(R.id.revenueTotalPriceOfALl);
        revenueList = new ArrayList<Revenue>();
        progressDialog = new ProgressDialog(this);

        yearCurrent = Calendar.getInstance().get(Calendar.YEAR);
        monthCurrent = Calendar.getInstance().get(Calendar.MONTH) + 1 ;
        dayOfMonthCurrent = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rclvRevenueList.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rclvRevenueList.addItemDecoration(dividerItemDecoration);

        revenueAdapter = new RevenueAdapter( revenueList);

        rclvRevenueList.setAdapter(revenueAdapter);

        String currentDate = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);
        revenueStartDate.setText(currentDate);
        revenueEndDate.setText(currentDate);
        getDataInit(currentDate, currentDate);
    }

    private void getDataInit(String startDate, String endDate){

        if(startDate.equals(endDate)){
            progressDialog.setMessage("Getting data...");
            progressDialog.show();

            DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("sells");

            unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(revenueList!=null) revenueList.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Revenue revenue_temp = new Revenue();
                        Sell sell_temp = snapshot.getValue(Sell.class);

                        if(sell_temp.getSellDate().equals(startDate)){

                            int position = getPosition(revenueList, sell_temp.getIdDrink());

                            if( position == -1){
                                revenue_temp.setIdDrink(sell_temp.getIdDrink());
                                revenue_temp.setSellDate(sell_temp.getSellDate());
                                revenue_temp.setQuantity(sell_temp.getQuantity());
                                revenue_temp.setTotalPrice(sell_temp.getTotalPrice());
                                revenueList.add(revenue_temp);
                            }else{
                                revenue_temp.setIdDrink(sell_temp.getIdDrink());
                                revenue_temp.setSellDate(sell_temp.getSellDate());

                                int oldQuantity, oldTotalPrice;

                                oldQuantity = revenueList.get(position).getQuantity();
                                oldTotalPrice = revenueList.get(position).getTotalPrice();

                                revenue_temp.setQuantity(oldQuantity + sell_temp.getQuantity());
                                revenue_temp.setTotalPrice(oldTotalPrice + sell_temp.getTotalPrice());

                                revenueList.set(position, revenue_temp);
                            }
                        }
                    }
                    progressDialog.dismiss();
                    revenueAdapter.notifyDataSetChanged();
                    calTotal();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showDangerMessage("Failed to read value.");
                    progressDialog.dismiss();
                }
            });
        }else{
            progressDialog.setMessage("Getting data...");
            progressDialog.show();

            DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("sells");

            unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(revenueList!=null) revenueList.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Revenue revenue_temp = new Revenue();
                        Sell sell_temp = snapshot.getValue(Sell.class);

                        if(isBetween(startDate, endDate, sell_temp.getSellDate())){

                            int position = getPosition(revenueList, sell_temp.getIdDrink());

                            if( position == -1){
                                revenue_temp.setIdDrink(sell_temp.getIdDrink());
                                revenue_temp.setSellDate(sell_temp.getSellDate());
                                revenue_temp.setQuantity(sell_temp.getQuantity());
                                revenue_temp.setTotalPrice(sell_temp.getTotalPrice());
                                revenueList.add(revenue_temp);
                            }else{
                                revenue_temp.setIdDrink(sell_temp.getIdDrink());
                                revenue_temp.setSellDate(sell_temp.getSellDate());

                                int oldQuantity, oldTotalPrice;

                                oldQuantity = revenueList.get(position).getQuantity();
                                oldTotalPrice = revenueList.get(position).getTotalPrice();

                                revenue_temp.setQuantity(oldQuantity + sell_temp.getQuantity());
                                revenue_temp.setTotalPrice(oldTotalPrice + sell_temp.getTotalPrice());

                                revenueList.set(position, revenue_temp);
                            }
                        }
                    }
                    progressDialog.dismiss();
                    revenueAdapter.notifyDataSetChanged();
                    calTotal();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showDangerMessage("Failed to read value.");
                    progressDialog.dismiss();
                }
            });
        }
    }

    private void getData(String startDate, String endDate){

        if(startDate.equals(endDate)){

            DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("sells");

            unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(revenueList!=null) revenueList.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Revenue revenue_temp = new Revenue();
                        Sell sell_temp = snapshot.getValue(Sell.class);

                        if(sell_temp.getSellDate().equals(startDate)){

                            int position = getPosition(revenueList, sell_temp.getIdDrink());

                            if( position == -1){
                                revenue_temp.setIdDrink(sell_temp.getIdDrink());
                                revenue_temp.setSellDate(sell_temp.getSellDate());
                                revenue_temp.setQuantity(sell_temp.getQuantity());
                                revenue_temp.setTotalPrice(sell_temp.getTotalPrice());
                                revenueList.add(revenue_temp);
                            }else{
                                revenue_temp.setIdDrink(sell_temp.getIdDrink());
                                revenue_temp.setSellDate(sell_temp.getSellDate());

                                int oldQuantity, oldTotalPrice;

                                oldQuantity = revenueList.get(position).getQuantity();
                                oldTotalPrice = revenueList.get(position).getTotalPrice();

                                revenue_temp.setQuantity(oldQuantity + sell_temp.getQuantity());
                                revenue_temp.setTotalPrice(oldTotalPrice + sell_temp.getTotalPrice());

                                revenueList.set(position, revenue_temp);
                            }
                        }
                    }
                    revenueAdapter.notifyDataSetChanged();
                    calTotal();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showDangerMessage("Failed to read value.");
                    progressDialog.dismiss();
                }
            });
        }else{

            DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("sells");

            unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(revenueList!=null) revenueList.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Revenue revenue_temp = new Revenue();
                        Sell sell_temp = snapshot.getValue(Sell.class);

                        if(isBetween(startDate, endDate, sell_temp.getSellDate())){

                            int position = getPosition(revenueList, sell_temp.getIdDrink());

                            if( position == -1){
                                revenue_temp.setIdDrink(sell_temp.getIdDrink());
                                revenue_temp.setSellDate(sell_temp.getSellDate());
                                revenue_temp.setQuantity(sell_temp.getQuantity());
                                revenue_temp.setTotalPrice(sell_temp.getTotalPrice());
                                revenueList.add(revenue_temp);
                            }else{
                                revenue_temp.setIdDrink(sell_temp.getIdDrink());
                                revenue_temp.setSellDate(sell_temp.getSellDate());

                                int oldQuantity, oldTotalPrice;

                                oldQuantity = revenueList.get(position).getQuantity();
                                oldTotalPrice = revenueList.get(position).getTotalPrice();

                                revenue_temp.setQuantity(oldQuantity + sell_temp.getQuantity());
                                revenue_temp.setTotalPrice(oldTotalPrice + sell_temp.getTotalPrice());

                                revenueList.set(position, revenue_temp);
                            }
                        }
                    }
                    revenueAdapter.notifyDataSetChanged();
                    calTotal();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showDangerMessage("Failed to read value.");
                    progressDialog.dismiss();
                }
            });
        }
    }

    private int getPosition(List<Revenue> list, String drinkId){
        for (int i = 0; i < list.size(); i++) {
            Revenue revenue = list.get(i);
            if(revenue.getIdDrink().equals(drinkId)){
                return i;
            }
        }
        return -1;
    }

    private boolean isBetween(String StartDate, String EndDate, String CompareDate){
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            Date date1 = dateFormat.parse(StartDate);
            Date date2 = dateFormat.parse(EndDate);
            Date selectedDate = dateFormat.parse(CompareDate);

            if (selectedDate.compareTo(date1) >= 0 && selectedDate.compareTo(date2) <= 0) {
                return true;
            } else {
                return false;
            }

        } catch (ParseException e) {
            showDangerMessage("Start date or End date is not valid!");
            return false;
        }
    }

    private boolean isValidRange(String StartDate, String EndDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        try {
            Date date1 = dateFormat.parse(StartDate);
            Date date2 = dateFormat.parse(EndDate);
            if(date1.compareTo(date2) <= 0){
                return true;
            }else{
                showDangerMessage("Start date must smaller than end date, set back to current date!");
                return false;
            }
        } catch (ParseException e) {
            showDangerMessage("Start date or End date is not valid!");
            return false;
        }
    }

    private void calTotal(){
        if (revenueList == null || revenueList.isEmpty()) {
            return;
        }

        int totalAllItem = 0;
        for (int i = 0; i < revenueList.size(); i++) {
            Revenue revenue = revenueList.get(i);
            totalAllItem += revenue.getTotalPrice();
        }
        revenueTotalPriceOfALl.setText("+ " +String.valueOf(totalAllItem) + " 000 VNĐ");
    }

    private void showSuccessMessage(String s){
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar =  Snackbar.make(parentLayout, s, Snackbar.LENGTH_LONG);
        snackbar.getView().setZ(9999);
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.white));
        snackbar.getView().setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.snackbar_success)));
        snackbar.show();
    }

    private void showToast(String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private boolean isNullOrEmpty(String s){
        if(s == null){
            return true;
        }else{
            if(s.isEmpty() || s.trim().isEmpty()){
                return true;
            }
        }
        return false;
    }
    private void showDangerMessage(String s){
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar =  Snackbar.make(parentLayout, s, Snackbar.LENGTH_LONG);
        snackbar.getView().setZ(9999);
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.white));
        snackbar.getView().setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.snackbar_danger)));
        snackbar.show();
    }
}