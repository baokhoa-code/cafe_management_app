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
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputFilter;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cafemanagementapp.Adapters.DrinkAdapter;
import com.example.cafemanagementapp.Adapters.SellAdapter;
import com.example.cafemanagementapp.Adapters.SpinnerDrinkAdapter;
import com.example.cafemanagementapp.Helpers.InputFilterMinMax;
import com.example.cafemanagementapp.Models.Drink;
import com.example.cafemanagementapp.Models.Import;
import com.example.cafemanagementapp.Models.Sell;
import com.example.cafemanagementapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class SellActivity extends AppCompatActivity {

    private EditText sellDate;
    private TextView selllDeleteAll;
    private RecyclerView rclvSellList;
    private SellAdapter sellAdapter;
    private FloatingActionButton btnSellAdd;
    private Button btnSellClearDate;
    private List<Drink> drinkList;
    private List<Sell> sellList;
    private ProgressDialog progressDialog;
    private AlertDialog dialogAdd;
    private AlertDialog dialogEdit;

    //Ad UI
    private Spinner newSellDrink;
    private EditText newSellQuantity, newSellUnitPrice;
    private TextView newSellUnit;
    private Button btnnewSellCancel, btnnewSellOk;

    //Edit UI

    private Spinner editSellDrink;
    private EditText editSellQuantity, editSellUnitPrice;
    private TextView editSellUnit;
    private Button btneditSellCancel, btneditSellOk;
    private int yearCurrent, monthCurrent, dayOfMonthCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sell);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Sell management");

        initUI();

        sellDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(SellActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int yearChosen, int monthChosen, int dayOfMonth) {

                        String dateFormatted = String.format("%02d/%02d/%d", dayOfMonth, monthChosen, yearChosen);
                        sellDate.setText(dateFormatted);
                        searchData(dateFormatted);

                    }
                }, yearCurrent, monthCurrent, dayOfMonthCurrent);
                datePickerDialog.show();
            }
        });

        btnSellClearDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
                sellDate.setText("");
            }
        });

        btnSellAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 = LayoutInflater.from(SellActivity.this).inflate(R.layout.add_sell_dialog, null);

                newSellDrink = view2.findViewById(R.id.newSellDrink);
                newSellQuantity = view2.findViewById(R.id.newSellQuantity);
                newSellUnitPrice = view2.findViewById(R.id.newSellUnitPrice);
                newSellUnit = view2.findViewById(R.id.newSellUnit);
                btnnewSellOk = view2.findViewById(R.id.btnnewSellOk);
                btnnewSellCancel = view2.findViewById(R.id.btnnewSellCancel);

                SpinnerDrinkAdapter adapter = new SpinnerDrinkAdapter(SellActivity.this, android.R.layout.simple_spinner_dropdown_item, drinkList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                newSellDrink.setAdapter(adapter);

                newSellDrink.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int position, long id) {
                        Drink drink = adapter.getItem(position);
                        newSellUnit.setText(drink.getUnit());

                        InStockCallback callback = new InStockCallback() {
                            @Override
                            public void onInStockReceived(int inStock) {
                                if(inStock == 0){
                                    showDangerMessage("There are no items in the stock!");
                                    dialogAdd.dismiss();
                                }else{
                                    newSellQuantity.setText("");
                                    newSellUnitPrice.setText("");
                                    newSellQuantity.setFilters(new InputFilter[]{ new InputFilterMinMax("1", inStock + "")});
                                    String temps = newSellUnit.getText().toString();
                                    newSellUnit.setText("(Max: " + inStock +") " + temps );
                                }
                            }
                        };

                        inStock(drink.getId(), callback);
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapter) {
                        newSellUnit.setText(drinkList.get(0).getUnit());
                    }
                });




                final AlertDialog.Builder builder= new AlertDialog.Builder(SellActivity.this);
                builder.setView(view2);
                dialogAdd = builder.create();
                dialogAdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogAdd.show();

                btnnewSellCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogAdd.dismiss();
                    }
                });

                btnnewSellOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isNewSellValid()){
                            DatabaseReference sellsRef = FirebaseDatabase.getInstance().getReference().child("sells");

                            Sell sell_temp = new Sell();
                            Drink drink_temp =  (Drink) newSellDrink.getSelectedItem();

                            String dateFormatted = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);

                            sell_temp.setSellDate(dateFormatted);
                            sell_temp.setIdDrink(drink_temp.getId());
                            sell_temp.setUnitPrice(Integer.parseInt(newSellUnitPrice.getText().toString()));
                            sell_temp.setQuantity(Integer.parseInt(newSellQuantity.getText().toString()));
                            sell_temp.setTotalPrice(sell_temp.getUnitPrice()*sell_temp.getQuantity());

                            String key = sellsRef.push().getKey();
                            sell_temp.setId(key);

                            sellsRef.child(key).setValue(sell_temp)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            showSuccessMessage("Add success!");
                                            dialogAdd.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            showDangerMessage("Add fail!");
                                        }
                                    });

                        }
                    }
                });
            }
        });

        selllDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sellList.size() > 0){
                    View view2 = LayoutInflater.from(SellActivity.this).inflate(R.layout.yes_no_dialog, null);

                    TextView tile = view2.findViewById(R.id.yesNoDialogConfirmTile);
                    TextView content = view2.findViewById(R.id.yesNoDialogContent);
                    Button btnNo = view2.findViewById(R.id.btnYesNoDialogNo);
                    Button btnYes = view2.findViewById(R.id.btnYesNoDialogYes);

                    tile.setText("Confirm Box");
                    content.setText("Do you really want to delete all record of Sells?");

                    final AlertDialog.Builder builder= new AlertDialog.Builder(SellActivity.this);
                    builder.setView(view2);
                    AlertDialog dialog = builder.create();
                    dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                    dialog.show();

                    btnNo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            dialog.dismiss();
                        }
                    });

                    btnYes.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            DatabaseReference sellsRef = FirebaseDatabase.getInstance().getReference().child("sells");
                            sellsRef.removeValue();
                            dialog.dismiss();
                        }
                    });
                }
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

    private boolean isNewSellValid() {
        if (newSellQuantity.getText().toString().isEmpty()) {
            showToast("Sell quantity is empty!");
            return false;
        } else if (newSellUnitPrice.getText().toString().isEmpty()) {
            showToast("Sell unit price is empty!");
            return false;
        } else if (Integer.parseInt(newSellQuantity.getText().toString()) <= 0) {
            showToast("Sell quantity must greater than 0!");
            return false;
        } else if (Integer.parseInt(newSellUnitPrice.getText().toString()) <= 0) {
            showToast("Sell unit price must greater than 0!");
            return false;
        } else {
            return true;
        }
    }

    private boolean isEditSellValid() {
        if (editSellQuantity.getText().toString().isEmpty()) {
            showToast("Sell quantity is empty!");
            return false;
        } else if (editSellUnitPrice.getText().toString().isEmpty()) {
            showToast("Sell unit price is empty!");
            return false;
        } else if (Integer.parseInt(editSellQuantity.getText().toString()) <= 0) {
            showToast("Sell quantity must greater than 0!");
            return false;
        } else if (Integer.parseInt(editSellUnitPrice.getText().toString()) <= 0) {
            showToast("Sell unit price must greater than 0!");
            return false;
        } else {
            return true;
        }
    }

    private void initUI(){
        sellDate = findViewById(R.id.sellDate);
        selllDeleteAll = findViewById(R.id.sellDeleteAll);
        rclvSellList = findViewById(R.id.rclvSellList);
        btnSellAdd = findViewById(R.id.btnSellAdd);
        btnSellClearDate = findViewById(R.id.btnSellClearDate);
        drinkList = new ArrayList<Drink>();
        sellList = new ArrayList<Sell>();
        progressDialog = new ProgressDialog(this);

        yearCurrent = Calendar.getInstance().get(Calendar.YEAR);
        monthCurrent = Calendar.getInstance().get(Calendar.MONTH) + 1 ;
        dayOfMonthCurrent = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        getDrinks();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rclvSellList.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rclvSellList.addItemDecoration(dividerItemDecoration);

        sellAdapter = new SellAdapter( sellList, new SellAdapter.IClickListener() {
            @Override
            public void onClickEditItem(Sell sell_t) {
                View view2 = LayoutInflater.from(SellActivity.this).inflate(R.layout.edit_sell_dialog, null);

                editSellDrink = view2.findViewById(R.id.editSellDrink);
                editSellQuantity = view2.findViewById(R.id.editSellQuantity);
                editSellUnitPrice = view2.findViewById(R.id.editSellUnitPrice);
                editSellUnit = view2.findViewById(R.id.editSellUnit);
                btneditSellOk = view2.findViewById(R.id.btneditSellOk);
                btneditSellCancel = view2.findViewById(R.id.btneditSellCancel);

                SpinnerDrinkAdapter adapter = new SpinnerDrinkAdapter(SellActivity.this, android.R.layout.simple_spinner_dropdown_item, drinkList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                editSellDrink.setAdapter(adapter);

                editSellDrink.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int position, long id) {
                        Drink drink = adapter.getItem(position);
                        editSellUnit.setText(drink.getUnit());
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapter) {
                        editSellUnit.setText(drinkList.get(0).getUnit());
                    }
                });

                int position = -1;
                for (int i = 0; i < drinkList.size(); i++) {
                    Drink drink = drinkList.get(i);
                    if (drink.getId().equals(sell_t.getIdDrink())) {
                        position = i;
                        break;
                    }
                }

                editSellDrink.setSelection(position);
                editSellDrink.setEnabled(false);
                editSellQuantity.setText(sell_t.getQuantity() + "");
                editSellUnitPrice.setText(sell_t.getUnitPrice() + "");

                InStockCallback callback = new InStockCallback() {
                    @Override
                    public void onInStockReceived(int inStock) {
                        editSellQuantity.setFilters(new InputFilter[]{ new InputFilterMinMax("1", inStock + "")});
                        String temps = editSellUnit.getText().toString();
                        editSellUnit.setText("(Max: " + inStock +") " + temps );
                    }
                };

                inStock(sell_t.getIdDrink(), callback);

                final AlertDialog.Builder builder= new AlertDialog.Builder(SellActivity.this);
                builder.setView(view2);
                dialogEdit = builder.create();
                dialogEdit.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogEdit.show();

                btneditSellCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogEdit.dismiss();
                    }
                });

                btneditSellOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isEditSellValid()){
                            DatabaseReference sellsRef = FirebaseDatabase.getInstance().getReference().child("sells");

                            Sell sell_temp = new Sell();
                            Drink drink_temp =  (Drink) editSellDrink.getSelectedItem();

                            sell_temp.setSellDate(sell_t.getSellDate());
                            sell_temp.setIdDrink(drink_temp.getId());
                            sell_temp.setUnitPrice(Integer.parseInt(editSellUnitPrice.getText().toString()));
                            sell_temp.setQuantity(Integer.parseInt(editSellQuantity.getText().toString()));
                            sell_temp.setTotalPrice(sell_temp.getUnitPrice()*sell_temp.getQuantity());

                            sell_temp.setId(sell_t.getId());

                            sellsRef.child(sell_temp.getId()).setValue(sell_temp)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            showSuccessMessage("Edit success!");
                                            dialogEdit.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            showDangerMessage("Edit fail!");
                                        }
                                    });

                        }
                    }
                });


            }

            @Override
            public void onClickDeleteItem(Sell sell_t) {


                View view2 = LayoutInflater.from(SellActivity.this).inflate(R.layout.yes_no_dialog, null);

                TextView tile = view2.findViewById(R.id.yesNoDialogConfirmTile);
                TextView content = view2.findViewById(R.id.yesNoDialogContent);
                Button btnNo = view2.findViewById(R.id.btnYesNoDialogNo);
                Button btnYes = view2.findViewById(R.id.btnYesNoDialogYes);

                tile.setText("Confirm Box");
                content.setText("Do you really want to delete this import?");

                final AlertDialog.Builder builder= new AlertDialog.Builder(SellActivity.this);
                builder.setView(view2);
                AlertDialog dialog = builder.create();
                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialog.show();

                btnNo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialog.dismiss();
                    }
                });

                btnYes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        DatabaseReference sellsRef = FirebaseDatabase.getInstance().getReference().child("sells");
                        sellsRef.child(sell_t.getId()).removeValue();
                        sellAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
            }
        });

        rclvSellList.setAdapter(sellAdapter);

        getData();
    }

    private void getDrinks(){
        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("drinks");

        unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(drinkList != null ) drinkList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Drink drink_temp = snapshot.getValue(Drink.class);
                    drinkList.add(drink_temp);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });
    }

    private void getData(){
        progressDialog.setMessage("Getting data...");
        progressDialog.show();

        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("sells");

        unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (sellList != null ) sellList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Sell sell_temp = snapshot.getValue(Sell.class);
                    sellList.add(sell_temp);
                }
                progressDialog.dismiss();
                sellAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDangerMessage("Failed to read value.");
                progressDialog.dismiss();
            }
        });
    }

    private void searchData(String keyword){
        DatabaseReference sellsRef = FirebaseDatabase.getInstance().getReference("sells");

        sellsRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if ( sellList != null ) sellList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Sell tempp = snapshot.getValue(Sell.class);
                    if(tempp.getSellDate().equals(keyword)){
                        sellList.add(tempp);
                    }
                }
                sellAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDangerMessage("Failed to read value.");
                progressDialog.dismiss();
            }
        });
    }


    private void inStock(String drinkId, final InStockCallback callback) {
        TotalQuantityCallback importCallback = new TotalQuantityCallback() {
            @Override
            public void onTotalQuantityReceived(int importQuantity) {
                TotalQuantityCallback sellCallback = new TotalQuantityCallback() {
                    @Override
                    public void onTotalQuantityReceived(int sellQuantity) {
                        int inStock = importQuantity - sellQuantity;
                        callback.onInStockReceived(inStock);
                    }
                };
                totalSell(drinkId, sellCallback);
            }
        };
        totalImport(drinkId, importCallback);
    }
    public interface TotalQuantityCallback {
        void onTotalQuantityReceived(int totalQuantity);
    }
    public interface InStockCallback {
        void onInStockReceived(int inStock);
    }

    private void totalImport(String drinkId, TotalQuantityCallback callback){
        DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference("imports");
        Query query = importsRef.orderByChild("idDrink").equalTo(drinkId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalQuantity = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Import importData = snapshot.getValue(Import.class);
                    int quantity = importData.getQuantity();
                    totalQuantity += quantity;
                }
                callback.onTotalQuantityReceived(totalQuantity);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SellActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void totalSell(String drinkId, TotalQuantityCallback callback){
        DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference("sells");
        Query query = importsRef.orderByChild("idDrink").equalTo(drinkId);
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                int totalQuantity = 0;
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Sell sellData = snapshot.getValue(Sell.class);
                    int quantity = sellData.getQuantity();
                    totalQuantity += quantity;
                }
                callback.onTotalQuantityReceived(totalQuantity);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(SellActivity.this, "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private  String encodedImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputSream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputSream);
        byte[] bytes = byteArrayOutputSream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }

    private Bitmap getDrinkImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
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

    private void showWarningMessage(String s){
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar =  Snackbar.make(parentLayout, s, Snackbar.LENGTH_LONG);
        snackbar.getView().setZ(9999);
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.white));
        snackbar.getView().setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.snackbar_warning)));
        snackbar.show();
    }

    private void showInfoMessage(String s){
        View parentLayout = findViewById(android.R.id.content);
        Snackbar snackbar =  Snackbar.make(parentLayout, s, Snackbar.LENGTH_LONG);
        snackbar.getView().setZ(9999);
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.white));
        snackbar.getView().setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.snackbar_infor)));
        snackbar.show();
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
}