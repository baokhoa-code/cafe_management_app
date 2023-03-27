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
import com.example.cafemanagementapp.Adapters.ImportAdapter;
import com.example.cafemanagementapp.Adapters.SpinnerDrinkAdapter;
import com.example.cafemanagementapp.Models.Drink;
import com.example.cafemanagementapp.Models.Import;
import com.example.cafemanagementapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class ImportActivity extends AppCompatActivity {

    private EditText importDate;
    private TextView importDeleteAll;
    private RecyclerView rclvImportList;
    private ImportAdapter importAdapter;
    private FloatingActionButton btnImportAdd;
    private Button btnImportClearDate;
    private List<Drink> drinkList;
    private List<Import> importList;
    private ProgressDialog progressDialog;
    private AlertDialog dialogAdd;
    private AlertDialog dialogEdit;

    //Ad UI
    private Spinner newImportDrink;
    private EditText newImportQuantity, newImportUnitPrice;
    private TextView newImportUnit;
    private Button btnnewImportCancel, btnnewImportOk;

    //Edit UI

    private Spinner editImportDrink;
    private EditText editImportQuantity, editImportUnitPrice;
    private TextView editImportUnit;
    private Button btneditImportCancel, btneditImportOk;

    private int yearCurrent, monthCurrent, dayOfMonthCurrent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_import);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Import management");

        initUI();

        importDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatePickerDialog datePickerDialog = new DatePickerDialog(ImportActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int yearChosen, int monthChosen, int dayOfMonth) {

                        String dateFormatted = String.format("%02d/%02d/%d", dayOfMonth, monthChosen, yearChosen);
                        importDate.setText(dateFormatted);
                        searchData(dateFormatted);

                    }
                }, yearCurrent, monthCurrent, dayOfMonthCurrent);
                datePickerDialog.show();
            }
        });

        btnImportClearDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getData();
                importDate.setText("");
            }
        });
        btnImportAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 = LayoutInflater.from(ImportActivity.this).inflate(R.layout.add_import_dialog, null);

                newImportDrink = view2.findViewById(R.id.newImportDrink);
                newImportQuantity = view2.findViewById(R.id.newImportQuantity);
                newImportUnitPrice = view2.findViewById(R.id.newImportUnitPrice);
                newImportUnit = view2.findViewById(R.id.newImportUnit);
                btnnewImportOk = view2.findViewById(R.id.btnnewImportOk);
                btnnewImportCancel = view2.findViewById(R.id.btnnewImportCancel);

                SpinnerDrinkAdapter adapter = new SpinnerDrinkAdapter(ImportActivity.this, android.R.layout.simple_spinner_dropdown_item, drinkList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                newImportDrink.setAdapter(adapter);

                newImportDrink.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int position, long id) {
                        // Here you get the current item (a User object) that is selected by its position
                        Drink drink = adapter.getItem(position);
                        // Here you can do the action you want to...
                        newImportUnit.setText(drink.getUnit());
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapter) {
                        newImportUnit.setText(drinkList.get(0).getUnit());
                    }
                });

                final AlertDialog.Builder builder= new AlertDialog.Builder(ImportActivity.this);
                builder.setView(view2);
                dialogAdd = builder.create();
                dialogAdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogAdd.show();

                btnnewImportCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogAdd.dismiss();
                    }
                });

                btnnewImportOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isNewImportValid()){
                            DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference().child("imports");

                            Import import_temp = new Import();
                            Drink drink_temp =  (Drink) newImportDrink.getSelectedItem();

                            String dateFormatted = String.format("%02d/%02d/%d", dayOfMonthCurrent, monthCurrent, yearCurrent);

                            import_temp.setImportDate(dateFormatted);
                            import_temp.setIdDrink(drink_temp.getId());
                            import_temp.setUnitPrice(Integer.parseInt(newImportUnitPrice.getText().toString()));
                            import_temp.setQuantity(Integer.parseInt(newImportQuantity.getText().toString()));
                            import_temp.setTotalPrice(import_temp.getUnitPrice()*import_temp.getQuantity());

                            String key = importsRef.push().getKey();
                            import_temp.setId(key);

                            importsRef.child(key).setValue(import_temp)
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

        importDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(importList.size() > 0){
                    View view2 = LayoutInflater.from(ImportActivity.this).inflate(R.layout.yes_no_dialog, null);

                    TextView tile = view2.findViewById(R.id.yesNoDialogConfirmTile);
                    TextView content = view2.findViewById(R.id.yesNoDialogContent);
                    Button btnNo = view2.findViewById(R.id.btnYesNoDialogNo);
                    Button btnYes = view2.findViewById(R.id.btnYesNoDialogYes);

                    tile.setText("Confirm Box");
                    content.setText("Do you really want to delete all record of Imports?");

                    final AlertDialog.Builder builder= new AlertDialog.Builder(ImportActivity.this);
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
                            DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference().child("imports");
                            importsRef.removeValue();
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
    private boolean isNewImportValid() {
        if (newImportQuantity.getText().toString().isEmpty()) {
            showToast("Import quantity is empty!");
            return false;
        } else if (newImportUnitPrice.getText().toString().isEmpty()) {
            showToast("Import unit price is empty!");
            return false;
        } else if (Integer.parseInt(newImportQuantity.getText().toString()) <= 0) {
            showToast("Import quantity must greater than 0!");
            return false;
        } else if (Integer.parseInt(newImportUnitPrice.getText().toString()) <= 0) {
            showToast("Import unit price must greater than 0!");
            return false;
        } else {
            return true;
        }
    }

    private boolean isEditImportValid() {
        if (editImportQuantity.getText().toString().isEmpty()) {
            showToast("Import quantity is empty!");
            return false;
        } else if (editImportUnitPrice.getText().toString().isEmpty()) {
            showToast("Import unit price is empty!");
            return false;
        } else if (Integer.parseInt(editImportQuantity.getText().toString()) <= 0) {
            showToast("Import quantity must greater than 0!");
            return false;
        } else if (Integer.parseInt(editImportUnitPrice.getText().toString()) <= 0) {
            showToast("Import unit price must greater than 0!");
            return false;
        } else {
            return true;
        }
    }

    private void initUI(){
        importDate = findViewById(R.id.importDate);
        importDeleteAll = findViewById(R.id.importDeleteAll);
        rclvImportList = findViewById(R.id.rclvImportList);
        btnImportAdd = findViewById(R.id.btnImportAdd);
        btnImportClearDate = findViewById(R.id.btnImportClearDate);
        drinkList = new ArrayList<Drink>();
        importList = new ArrayList<Import>();
        progressDialog = new ProgressDialog(this);

        yearCurrent = Calendar.getInstance().get(Calendar.YEAR);
        monthCurrent = Calendar.getInstance().get(Calendar.MONTH) + 1 ;
        dayOfMonthCurrent = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        getDrinks();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rclvImportList.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rclvImportList.addItemDecoration(dividerItemDecoration);

        importAdapter = new ImportAdapter(importList, new ImportAdapter.IClickListener() {
            @Override
            public void onClickEditItem(Import import_t) {
                View view2 = LayoutInflater.from(ImportActivity.this).inflate(R.layout.edit_import_dialog, null);

                editImportDrink = view2.findViewById(R.id.editImportDrink);
                editImportQuantity = view2.findViewById(R.id.editImportQuantity);
                editImportUnitPrice = view2.findViewById(R.id.editImportUnitPrice);
                editImportUnit = view2.findViewById(R.id.editImportUnit);
                btneditImportOk = view2.findViewById(R.id.btneditImportOk);
                btneditImportCancel = view2.findViewById(R.id.btneditImportCancel);

                SpinnerDrinkAdapter adapter = new SpinnerDrinkAdapter(ImportActivity.this, android.R.layout.simple_spinner_dropdown_item, drinkList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                editImportDrink.setAdapter(adapter);

                editImportDrink.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int position, long id) {
                        // Here you get the current item (a User object) that is selected by its position
                        Drink drink = adapter.getItem(position);
                        // Here you can do the action you want to...
                        editImportUnit.setText(drink.getUnit());
                    }
                    @Override
                    public void onNothingSelected(AdapterView<?> adapter) {
                        editImportUnit.setText(drinkList.get(0).getUnit());
                    }
                });

                int position = -1;
                for (int i = 0; i < drinkList.size(); i++) {
                    Drink drink = drinkList.get(i);
                    if (drink.getId().equals(import_t.getIdDrink())) {
                        position = i;
                        break;
                    }
                }

                editImportDrink.setSelection(position);
                editImportQuantity.setText(import_t.getQuantity() + "");
                editImportUnitPrice.setText(import_t.getUnitPrice() + "");

                final AlertDialog.Builder builder= new AlertDialog.Builder(ImportActivity.this);
                builder.setView(view2);
                dialogEdit = builder.create();
                dialogEdit.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogEdit.show();

                btneditImportCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogEdit.dismiss();
                    }
                });

                btneditImportOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isEditImportValid()){
                            DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference().child("imports");

                            Import import_temp = new Import();
                            Drink drink_temp =  (Drink) editImportDrink.getSelectedItem();

                            import_temp.setImportDate(import_t.getImportDate());
                            import_temp.setIdDrink(drink_temp.getId());
                            import_temp.setUnitPrice(Integer.parseInt(editImportUnitPrice.getText().toString()));
                            import_temp.setQuantity(Integer.parseInt(editImportQuantity.getText().toString()));
                            import_temp.setTotalPrice(import_temp.getUnitPrice()*import_temp.getQuantity());

                            import_temp.setId(import_t.getId());

                            importsRef.child(import_temp.getId()).setValue(import_temp)
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
            public void onClickDeleteItem(Import import_t) {
                View view2 = LayoutInflater.from(ImportActivity.this).inflate(R.layout.yes_no_dialog, null);

                TextView tile = view2.findViewById(R.id.yesNoDialogConfirmTile);
                TextView content = view2.findViewById(R.id.yesNoDialogContent);
                Button btnNo = view2.findViewById(R.id.btnYesNoDialogNo);
                Button btnYes = view2.findViewById(R.id.btnYesNoDialogYes);

                tile.setText("Confirm Box");
                content.setText("Do you really want to delete this import?");

                final AlertDialog.Builder builder= new AlertDialog.Builder(ImportActivity.this);
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
                        DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference().child("imports");
                        importsRef.child(import_t.getId()).removeValue();
                        importAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
            }
        });

        rclvImportList.setAdapter(importAdapter);

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

        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("imports");

        unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(importList != null ) importList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Import import_temp = snapshot.getValue(Import.class);
                    importList.add(import_temp);
                }
                progressDialog.dismiss();
                importAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDangerMessage("Failed to read value.");
                progressDialog.dismiss();
            }
        });
    }

    private void searchData(String keyword){
        DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference("imports");

        importsRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(importList != null ) importList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Import tempp = snapshot.getValue(Import.class);
                    if(tempp.getImportDate().equals(keyword)){
                        importList.add(tempp);
                    }
                }
                importAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDangerMessage("Failed to read value.");
                progressDialog.dismiss();
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