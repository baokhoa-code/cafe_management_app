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

import com.example.cafemanagementapp.Adapters.SummaryAdapter;
import com.example.cafemanagementapp.Adapters.SellAdapter;
import com.example.cafemanagementapp.Helpers.SummaryDateComparator;
import com.example.cafemanagementapp.Models.Drink;
import com.example.cafemanagementapp.Models.Import;
import com.example.cafemanagementapp.Models.Summary;
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
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SummaryActivity extends AppCompatActivity {
    private EditText summaryStartDate, summaryEndDate;
    private Button btnSummaryClearDate;
    private RecyclerView rclvSummaryList;
    private SummaryAdapter summaryAdapter;
    private TextView summaryTotalPriceOfALl;
    private List<Summary> summaryList;
    private ProgressDialog progressDialog;
    private int yearCurrent, monthCurrent, dayOfMonthCurrent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("View Summary");

        initUI();

        summaryStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(SummaryActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int yearChosen, int monthChosen, int dayOfMonth) {

                        String dateFormatted = String.format("%02d/%02d/%d", dayOfMonth, monthChosen, yearChosen);
                        summaryStartDate.setText(dateFormatted);
                        if(!isNullOrEmpty(summaryEndDate.getText().toString())){
                            if(isValidRange(dateFormatted,summaryEndDate.getText().toString())){
                                getData(dateFormatted,summaryEndDate.getText().toString() );
                            }else{
                                String currentDate = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);
                                summaryStartDate.setText(currentDate);
                                summaryEndDate.setText(currentDate);
                                getData(currentDate, currentDate);
                            }
                        }
                    }
                }, yearCurrent, monthCurrent, dayOfMonthCurrent);
                datePickerDialog.show();
            }
        });

        summaryEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(SummaryActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int yearChosen, int monthChosen, int dayOfMonth) {

                        String dateFormatted = String.format("%02d/%02d/%d", dayOfMonth, monthChosen, yearChosen);
                        summaryEndDate.setText(dateFormatted);
                        if(!isNullOrEmpty(summaryStartDate.getText().toString())){
                            if(isValidRange(summaryStartDate.getText().toString(), dateFormatted)){
                                getData(summaryStartDate.getText().toString(), dateFormatted);
                            }else {
                                String currentDate = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);
                                summaryStartDate.setText(currentDate);
                                summaryEndDate.setText(currentDate);
                                getData(currentDate, currentDate);
                            }

                        }
                    }
                }, yearCurrent, monthCurrent, dayOfMonthCurrent);
                datePickerDialog.show();
            }
        });

        btnSummaryClearDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentDate = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);
                summaryStartDate.setText(currentDate);
                summaryEndDate.setText(currentDate);
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
        summaryStartDate = findViewById(R.id.summaryStartDate);
        summaryEndDate = findViewById(R.id.summaryEndDate);
        btnSummaryClearDate = findViewById(R.id.btnSummaryClearDate);
        rclvSummaryList = findViewById(R.id.rclvSummaryList);
        summaryTotalPriceOfALl = findViewById(R.id.summaryTotalPriceOfALl);
        summaryList = new ArrayList<Summary>();
        progressDialog = new ProgressDialog(this);

        yearCurrent = Calendar.getInstance().get(Calendar.YEAR);
        monthCurrent = Calendar.getInstance().get(Calendar.MONTH) + 1 ;
        dayOfMonthCurrent = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rclvSummaryList.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rclvSummaryList.addItemDecoration(dividerItemDecoration);

        summaryAdapter = new SummaryAdapter( summaryList);

        rclvSummaryList.setAdapter(summaryAdapter);

        String currentDate = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);
        summaryStartDate.setText(currentDate);
        summaryEndDate.setText(currentDate);
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
                    if(summaryList!=null) summaryList.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Summary summary_temp = new Summary();
                        Sell sell_temp = snapshot.getValue(Sell.class);

                        if(sell_temp.getSellDate().equals(startDate)){
                            summary_temp.setIdDrink(sell_temp.getIdDrink());
                            summary_temp.setSellDate(sell_temp.getSellDate());
                            summary_temp.setQuantity(sell_temp.getQuantity());
                            summary_temp.setTotalPrice(sell_temp.getTotalPrice());
                            summaryList.add(summary_temp);
                        }
                    }
                    DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference("imports");

                    importsRef.orderByKey().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                                Summary summary_temp = new Summary();
                                Import import_temp = snapshot.getValue(Import.class);

                                if(import_temp.getImportDate().equals(startDate)){
                                    summary_temp.setIdDrink(import_temp.getIdDrink());
                                    summary_temp.setSellDate(import_temp.getImportDate());
                                    summary_temp.setQuantity(import_temp.getQuantity());
                                    summary_temp.setTotalPrice(-import_temp.getTotalPrice());
                                    summaryList.add(summary_temp);
                                }
                            }
                            progressDialog.dismiss();
                            Collections.sort(summaryList, new SummaryDateComparator());
                            summaryAdapter.notifyDataSetChanged();
                            calTotal();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            showDangerMessage("Failed to read value.");
                            progressDialog.dismiss();
                        }
                    });
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
                    if(summaryList!=null) summaryList.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Summary summary_temp = new Summary();
                        Sell sell_temp = snapshot.getValue(Sell.class);

                        if(isBetween(startDate, endDate, sell_temp.getSellDate())){
                            summary_temp.setIdDrink(sell_temp.getIdDrink());
                            summary_temp.setSellDate(sell_temp.getSellDate());
                            summary_temp.setQuantity(sell_temp.getQuantity());
                            summary_temp.setTotalPrice(sell_temp.getTotalPrice());
                            summaryList.add(summary_temp);
                        }
                    }
                    DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference("imports");

                    importsRef.orderByKey().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                                Summary summary_temp = new Summary();
                                Import import_temp = snapshot.getValue(Import.class);

                                if(isBetween(startDate, endDate, import_temp.getImportDate())){
                                    summary_temp.setIdDrink(import_temp.getIdDrink());
                                    summary_temp.setSellDate(import_temp.getImportDate());
                                    summary_temp.setQuantity(import_temp.getQuantity());
                                    summary_temp.setTotalPrice(0-import_temp.getTotalPrice());
                                    summaryList.add(summary_temp);
                                }
                            }
                            progressDialog.dismiss();
                            Collections.sort(summaryList, new SummaryDateComparator());
                            summaryAdapter.notifyDataSetChanged();
                            calTotal();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            showDangerMessage("Failed to read value.");
                            progressDialog.dismiss();
                        }
                    });
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
                    if(summaryList!=null) summaryList.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Summary summary_temp = new Summary();
                        Sell sell_temp = snapshot.getValue(Sell.class);

                        if(sell_temp.getSellDate().equals(startDate)){
                            summary_temp.setIdDrink(sell_temp.getIdDrink());
                            summary_temp.setSellDate(sell_temp.getSellDate());
                            summary_temp.setQuantity(sell_temp.getQuantity());
                            summary_temp.setTotalPrice(sell_temp.getTotalPrice());
                            summaryList.add(summary_temp);
                        }
                    }
                    DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference("imports");

                    importsRef.orderByKey().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                                Summary summary_temp = new Summary();
                                Import import_temp = snapshot.getValue(Import.class);

                                if(import_temp.getImportDate().equals(startDate)){
                                    summary_temp.setIdDrink(import_temp.getIdDrink());
                                    summary_temp.setSellDate(import_temp.getImportDate());
                                    summary_temp.setQuantity(import_temp.getQuantity());
                                    summary_temp.setTotalPrice(-import_temp.getTotalPrice());
                                    summaryList.add(summary_temp);
                                }
                            }
                            Collections.sort(summaryList, new SummaryDateComparator());
                            summaryAdapter.notifyDataSetChanged();
                            calTotal();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            showDangerMessage("Failed to read value.");
                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showDangerMessage("Failed to read value.");
                }
            });
        }else{

            DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("sells");

            unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(summaryList!=null) summaryList.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Summary summary_temp = new Summary();
                        Sell sell_temp = snapshot.getValue(Sell.class);

                        if(isBetween(startDate, endDate, sell_temp.getSellDate())){
                            summary_temp.setIdDrink(sell_temp.getIdDrink());
                            summary_temp.setSellDate(sell_temp.getSellDate());
                            summary_temp.setQuantity(sell_temp.getQuantity());
                            summary_temp.setTotalPrice(sell_temp.getTotalPrice());
                            summaryList.add(summary_temp);
                        }
                    }
                    DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference("imports");

                    importsRef.orderByKey().addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                                Summary summary_temp = new Summary();
                                Import import_temp = snapshot.getValue(Import.class);

                                if(isBetween(startDate, endDate, import_temp.getImportDate())){
                                    summary_temp.setIdDrink(import_temp.getIdDrink());
                                    summary_temp.setSellDate(import_temp.getImportDate());
                                    summary_temp.setQuantity(import_temp.getQuantity());
                                    summary_temp.setTotalPrice(0-import_temp.getTotalPrice());
                                    summaryList.add(summary_temp);
                                }
                            }
                            Collections.sort(summaryList, new SummaryDateComparator());
                            summaryAdapter.notifyDataSetChanged();
                            calTotal();
                        }
                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {
                            showDangerMessage("Failed to read value.");
                        }
                    });
                }
                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showDangerMessage("Failed to read value.");
                }
            });
        }
    }

    private int getPosition(List<Summary> list, String drinkId){
        for (int i = 0; i < list.size(); i++) {
            Summary summary = list.get(i);
            if(summary.getIdDrink().equals(drinkId)){
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
        if (summaryList == null || summaryList.isEmpty()) {
            return;
        }

        int totalAllItem = 0;
        for (int i = 0; i < summaryList.size(); i++) {
            Summary summary = summaryList.get(i);
            totalAllItem += summary.getTotalPrice();
        }
        if(totalAllItem<0){
            summaryTotalPriceOfALl.setTextColor(getResources().getColor(R.color.total_price_lo));
            summaryTotalPriceOfALl.setText("- " + -totalAllItem + " 000 VNĐ");
        }else{
            summaryTotalPriceOfALl.setTextColor(getResources().getColor(R.color.total_price_loi));
            summaryTotalPriceOfALl.setText("+ " + totalAllItem + " 000 VNĐ");
        }
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