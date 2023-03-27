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

import com.example.cafemanagementapp.Adapters.ExpenseAdapter;
import com.example.cafemanagementapp.Adapters.ImportAdapter;
import com.example.cafemanagementapp.Models.Drink;
import com.example.cafemanagementapp.Models.Expense;
import com.example.cafemanagementapp.Models.Import;
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

public class ExpenseActivity extends AppCompatActivity {
    private EditText expenseStartDate, expenseEndDate;
    private Button btnExpenseClearDate;
    private RecyclerView rclvExpenseList;
    private ExpenseAdapter expenseAdapter;
    private TextView expenseTotalPriceOfALl;
    private List<Expense> expenseList;
    private ProgressDialog progressDialog;
    private int yearCurrent, monthCurrent, dayOfMonthCurrent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("View Expense");

        initUI();

        expenseStartDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(ExpenseActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int yearChosen, int monthChosen, int dayOfMonth) {

                        String dateFormatted = String.format("%02d/%02d/%d", dayOfMonth, monthChosen, yearChosen);
                        expenseStartDate.setText(dateFormatted);
                        if(!isNullOrEmpty(expenseEndDate.getText().toString())){
                            if(isValidRange(dateFormatted,expenseEndDate.getText().toString())){
                                getData(dateFormatted,expenseEndDate.getText().toString() );
                            }else{
                                String currentDate = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);
                                expenseStartDate.setText(currentDate);
                                expenseEndDate.setText(currentDate);
                                getData(currentDate, currentDate);
                            }
                        }
                    }
                }, yearCurrent, monthCurrent, dayOfMonthCurrent);
                datePickerDialog.show();
            }
        });

        expenseEndDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(ExpenseActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int yearChosen, int monthChosen, int dayOfMonth) {

                        String dateFormatted = String.format("%02d/%02d/%d", dayOfMonth, monthChosen, yearChosen);
                        expenseEndDate.setText(dateFormatted);
                        if(!isNullOrEmpty(expenseStartDate.getText().toString())){
                            if(isValidRange(expenseStartDate.getText().toString(), dateFormatted)){
                                getData(expenseStartDate.getText().toString(), dateFormatted);
                            }else {
                                String currentDate = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);
                                expenseStartDate.setText(currentDate);
                                expenseEndDate.setText(currentDate);
                                getData(currentDate, currentDate);
                            }

                        }
                    }
                }, yearCurrent, monthCurrent, dayOfMonthCurrent);
                datePickerDialog.show();
            }
        });

        btnExpenseClearDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String currentDate = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);
                expenseStartDate.setText(currentDate);
                expenseEndDate.setText(currentDate);
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
        expenseStartDate = findViewById(R.id.expenseStartDate);
        expenseEndDate = findViewById(R.id.expenseEndDate);
        btnExpenseClearDate = findViewById(R.id.btnExpenseClearDate);
        rclvExpenseList = findViewById(R.id.rclvExpenseList);
        expenseTotalPriceOfALl = findViewById(R.id.expenseTotalPriceOfALl);
        expenseList = new ArrayList<Expense>();
        progressDialog = new ProgressDialog(this);

        yearCurrent = Calendar.getInstance().get(Calendar.YEAR);
        monthCurrent = Calendar.getInstance().get(Calendar.MONTH) + 1 ;
        dayOfMonthCurrent = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rclvExpenseList.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rclvExpenseList.addItemDecoration(dividerItemDecoration);

        expenseAdapter = new ExpenseAdapter( expenseList);

        rclvExpenseList.setAdapter(expenseAdapter);

        String currentDate = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);
        expenseStartDate.setText(currentDate);
        expenseEndDate.setText(currentDate);
        getDataInit(currentDate, currentDate);
    }

    private void getDataInit(String startDate, String endDate){

        if(startDate.equals(endDate)){
            progressDialog.setMessage("Getting data...");
            progressDialog.show();

            DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("imports");

            unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(expenseList!=null) expenseList.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Expense expense_temp = new Expense();
                        Import import_temp = snapshot.getValue(Import.class);

                        if(import_temp.getImportDate().equals(startDate)){

                            int position = getPosition(expenseList, import_temp.getIdDrink());

                            if( position == -1){
                                expense_temp.setIdDrink(import_temp.getIdDrink());
                                expense_temp.setImportDate(import_temp.getImportDate());
                                expense_temp.setQuantity(import_temp.getQuantity());
                                expense_temp.setTotalPrice(import_temp.getTotalPrice());
                                expenseList.add(expense_temp);
                            }else{
                                expense_temp.setIdDrink(import_temp.getIdDrink());
                                expense_temp.setImportDate(import_temp.getImportDate());

                                int oldQuantity, oldTotalPrice;

                                oldQuantity = expenseList.get(position).getQuantity();
                                oldTotalPrice = expenseList.get(position).getTotalPrice();

                                expense_temp.setQuantity(oldQuantity + import_temp.getQuantity());
                                expense_temp.setTotalPrice(oldTotalPrice + import_temp.getTotalPrice());

                                expenseList.set(position, expense_temp);
                            }
                        }
                    }
                    progressDialog.dismiss();
                    expenseAdapter.notifyDataSetChanged();
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

            DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("imports");

            unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(expenseList!=null) expenseList.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Expense expense_temp = new Expense();
                        Import import_temp = snapshot.getValue(Import.class);

                        if(isBetween(startDate, endDate, import_temp.getImportDate())){

                            int position = getPosition(expenseList, import_temp.getIdDrink());

                            if( position == -1){
                                expense_temp.setIdDrink(import_temp.getIdDrink());
                                expense_temp.setImportDate(import_temp.getImportDate());
                                expense_temp.setQuantity(import_temp.getQuantity());
                                expense_temp.setTotalPrice(import_temp.getTotalPrice());
                                expenseList.add(expense_temp);
                            }else{
                                expense_temp.setIdDrink(import_temp.getIdDrink());
                                expense_temp.setImportDate(import_temp.getImportDate());

                                int oldQuantity, oldTotalPrice;

                                oldQuantity = expenseList.get(position).getQuantity();
                                oldTotalPrice = expenseList.get(position).getTotalPrice();

                                expense_temp.setQuantity(oldQuantity + import_temp.getQuantity());
                                expense_temp.setTotalPrice(oldTotalPrice + import_temp.getTotalPrice());

                                expenseList.set(position, expense_temp);
                            }
                        }
                    }
                    progressDialog.dismiss();
                    expenseAdapter.notifyDataSetChanged();
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

            DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("imports");

            unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(expenseList!=null) expenseList.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Expense expense_temp = new Expense();
                        Import import_temp = snapshot.getValue(Import.class);

                        if(import_temp.getImportDate().equals(startDate)){

                            int position = getPosition(expenseList, import_temp.getIdDrink());

                            if( position == -1){
                                expense_temp.setIdDrink(import_temp.getIdDrink());
                                expense_temp.setImportDate(import_temp.getImportDate());
                                expense_temp.setQuantity(import_temp.getQuantity());
                                expense_temp.setTotalPrice(import_temp.getTotalPrice());
                                expenseList.add(expense_temp);
                            }else{
                                expense_temp.setIdDrink(import_temp.getIdDrink());
                                expense_temp.setImportDate(import_temp.getImportDate());

                                int oldQuantity, oldTotalPrice;

                                oldQuantity = expenseList.get(position).getQuantity();
                                oldTotalPrice = expenseList.get(position).getTotalPrice();

                                expense_temp.setQuantity(oldQuantity + import_temp.getQuantity());
                                expense_temp.setTotalPrice(oldTotalPrice + import_temp.getTotalPrice());

                                expenseList.set(position, expense_temp);
                            }
                        }
                    }
                    expenseAdapter.notifyDataSetChanged();
                    calTotal();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    showDangerMessage("Failed to read value.");
                    progressDialog.dismiss();
                }
            });
        }else{

            DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("imports");

            unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    if(expenseList!=null) expenseList.clear();
                    for(DataSnapshot snapshot : dataSnapshot.getChildren()){

                        Expense expense_temp = new Expense();
                        Import import_temp = snapshot.getValue(Import.class);

                        if(isBetween(startDate, endDate, import_temp.getImportDate())){

                            int position = getPosition(expenseList, import_temp.getIdDrink());

                            if( position == -1){
                                expense_temp.setIdDrink(import_temp.getIdDrink());
                                expense_temp.setImportDate(import_temp.getImportDate());
                                expense_temp.setQuantity(import_temp.getQuantity());
                                expense_temp.setTotalPrice(import_temp.getTotalPrice());
                                expenseList.add(expense_temp);
                            }else{
                                expense_temp.setIdDrink(import_temp.getIdDrink());
                                expense_temp.setImportDate(import_temp.getImportDate());

                                int oldQuantity, oldTotalPrice;

                                oldQuantity = expenseList.get(position).getQuantity();
                                oldTotalPrice = expenseList.get(position).getTotalPrice();

                                expense_temp.setQuantity(oldQuantity + import_temp.getQuantity());
                                expense_temp.setTotalPrice(oldTotalPrice + import_temp.getTotalPrice());

                                expenseList.set(position, expense_temp);
                            }
                        }
                    }
                    expenseAdapter.notifyDataSetChanged();
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

    private int getPosition(List<Expense> list, String drinkId){
        for (int i = 0; i < list.size(); i++) {
            Expense expense = list.get(i);
            if(expense.getIdDrink().equals(drinkId)){
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
        if (expenseList == null || expenseList.isEmpty()) {
            return;
        }

        int totalAllItem = 0;
        for (int i = 0; i < expenseList.size(); i++) {
            Expense expense = expenseList.get(i);
            totalAllItem += expense.getTotalPrice();
        }
        expenseTotalPriceOfALl.setText("- " + String.valueOf(totalAllItem) + " 000 VNĐ");
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