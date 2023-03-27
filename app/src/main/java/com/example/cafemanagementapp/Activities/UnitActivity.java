package com.example.cafemanagementapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.cafemanagementapp.Adapters.UnitAdapter;
import com.example.cafemanagementapp.Models.Drink;
import com.example.cafemanagementapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class UnitActivity extends AppCompatActivity {

    private SearchView unitSearch;
    private TextView unitDeleteAll;
    private RecyclerView rclvUnitList;
    private FloatingActionButton btnUnitAdd;
    private UnitAdapter unitAdapter;
    private List<String> unitList;
    private ProgressDialog progressDialog;
    private AlertDialog dialogAdd;
    private AlertDialog dialogEdit;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unit);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Unit management");

        initUI();

        btnUnitAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 = LayoutInflater.from(UnitActivity.this).inflate(R.layout.add_unit_dialog, null);

                EditText newUnit = view2.findViewById(R.id.newUnitName);
                Button btnnewUnitNameCancel = view2.findViewById(R.id.btnnewUnitNameCancel);
                Button btnnewUnitNameOk = view2.findViewById(R.id.btnnewUnitNameOk);

                final AlertDialog.Builder builder= new AlertDialog.Builder(UnitActivity.this);
                builder.setView(view2);
                dialogAdd = builder.create();
                dialogAdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogAdd.show();

                btnnewUnitNameCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogAdd.dismiss();
                    }
                });

                btnnewUnitNameOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!isNullOrEmpty(String.valueOf(newUnit.getText()))){
                            addNewUnit(String.valueOf(newUnit.getText()));
                        }else{
                            showDangerMessage("You must enter new unit name!");
                        }

                    }
                });
            }
        });

        unitDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(unitList.size() >0){
                    View view2 = LayoutInflater.from(UnitActivity.this).inflate(R.layout.yes_no_dialog, null);

                    TextView tile = view2.findViewById(R.id.yesNoDialogConfirmTile);
                    TextView content = view2.findViewById(R.id.yesNoDialogContent);
                    Button btnNo = view2.findViewById(R.id.btnYesNoDialogNo);
                    Button btnYes = view2.findViewById(R.id.btnYesNoDialogYes);

                    tile.setText("Confirm Box");
                    content.setText("Do you really want to delete all record of Unit?, this also delete all record of other collection.");

                    final AlertDialog.Builder builder= new AlertDialog.Builder(UnitActivity.this);
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
                            DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference().child("units");
                            unitsRef.removeValue();
                            DatabaseReference drinksRef = FirebaseDatabase.getInstance().getReference().child("drinks");
                            drinksRef.removeValue();
                            DatabaseReference importsRef = FirebaseDatabase.getInstance().getReference().child("imports");
                            importsRef.removeValue();
                            DatabaseReference sellsRef = FirebaseDatabase.getInstance().getReference().child("sells");
                            sellsRef.removeValue();
                            unitAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        unitSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Called when the user submits the query
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Called when the user changes the query text
                searchData(newText);
                return true;
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
        unitSearch = findViewById(R.id.unitSearch);
        unitDeleteAll = findViewById(R.id.unitDeleteAll);
        rclvUnitList = findViewById(R.id.rclvUnitList);
        btnUnitAdd = findViewById(R.id.btnUnitAdd);
        unitList = new ArrayList<String>();
        progressDialog = new ProgressDialog(this);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rclvUnitList.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rclvUnitList.addItemDecoration(dividerItemDecoration);

        unitAdapter = new UnitAdapter(unitList, new UnitAdapter.IClickListener() {
            @Override
            public void onClickDeleteItem(String s) {
                View view2 = LayoutInflater.from(UnitActivity.this).inflate(R.layout.yes_no_dialog, null);

                TextView tile = view2.findViewById(R.id.yesNoDialogConfirmTile);
                TextView content = view2.findViewById(R.id.yesNoDialogContent);
                Button btnNo = view2.findViewById(R.id.btnYesNoDialogNo);
                Button btnYes = view2.findViewById(R.id.btnYesNoDialogYes);

                tile.setText("Confirm Box");
                content.setText("Do you really want to delete " + s + " unit?");

                final AlertDialog.Builder builder= new AlertDialog.Builder(UnitActivity.this);
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
                        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference().child("units");
                        unitsRef.child(s).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                deleteDrinkByUnitName(s);
                            }
                        });
                        unitAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
            }

            @Override
            public void onClickEditItem(String s) {
                View view2 = LayoutInflater.from(UnitActivity.this).inflate(R.layout.edit_unit_dialog, null);

                EditText editUnit = view2.findViewById(R.id.editUnitName);
                Button btneditUnitNameCancel = view2.findViewById(R.id.btneditUnitNameCancel);
                Button btneditUnitNameOk = view2.findViewById(R.id.btneditUnitNameOk);

                editUnit.setText(s);

                final AlertDialog.Builder builder= new AlertDialog.Builder(UnitActivity.this);
                builder.setView(view2);
                dialogEdit = builder.create();
                dialogEdit.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogEdit.show();

                btneditUnitNameCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogEdit.dismiss();
                    }
                });

                btneditUnitNameOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(!isNullOrEmpty(String.valueOf(editUnit.getText()))){
                            editUnit(s,String.valueOf(editUnit.getText()));
                        }else{
                            showDangerMessage("Unit name cannot be empty!");
                        }

                    }
                });
            }
        });

        rclvUnitList.setAdapter(unitAdapter);

        getData();

    }
    private void getData(){
        progressDialog.setMessage("Getting data...");
        progressDialog.show();

        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("units");

        unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(unitList != null ) unitList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String unit = snapshot.getValue(String.class);
                    unitList.add(unit);
                }
                progressDialog.dismiss();
                unitAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDangerMessage("Failed to read value.");
                progressDialog.dismiss();
            }
        });
    }
    private void searchData(String keyword){

        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("units");

        unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(unitList != null ) unitList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String unit = snapshot.getValue(String.class);
                    if(unit.contains(keyword)){
                        unitList.add(unit);
                    }
                }
                unitAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDangerMessage("Failed to read value.");
                progressDialog.dismiss();
            }
        });

    }

    private void deleteDrinkByUnitName(String uname){
        DatabaseReference drinksRef = FirebaseDatabase.getInstance().getReference("drinks");

        Query cayDrinksQuery = drinksRef.orderByChild("unit").equalTo(uname);

        cayDrinksQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot cayDrinkSnapshot : dataSnapshot.getChildren()) {
                    Drink cayDrink = cayDrinkSnapshot.getValue(Drink.class);
                    cayDrinkSnapshot.getRef().removeValue();
                    deleteImportByIdDrink(cayDrink.getId());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }

    private void deleteImportByIdDrink(String id){
        DatabaseReference drinksRef = FirebaseDatabase.getInstance().getReference("imports");

        Query cayDrinksQuery = drinksRef.orderByChild("idDrink").equalTo(id);

        cayDrinksQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot cayDrinkSnapshot : dataSnapshot.getChildren()) {
                    cayDrinkSnapshot.getRef().removeValue();
                }
                deleteSellByIdDrink(id);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }

    private void deleteSellByIdDrink(String id){
        DatabaseReference drinksRef = FirebaseDatabase.getInstance().getReference("sells");

        Query cayDrinksQuery = drinksRef.orderByChild("idDrink").equalTo(id);

        cayDrinksQuery.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot cayDrinkSnapshot : dataSnapshot.getChildren()) {
                    cayDrinkSnapshot.getRef().removeValue();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý lỗi nếu có
            }
        });
    }

    private void addNewUnit(String s){
        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference().child("units");

        unitsRef.orderByValue().equalTo(s).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    showDangerMessage("Unit name already exists!");
                } else {
                    unitsRef.child(s).setValue(s)
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

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDangerMessage("Database error: " + databaseError.getMessage());
            }
        });



    }
    private void editUnit(String s, String newS){
        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference().child("units");

        unitsRef.child(s).removeValue();

        unitsRef.orderByValue().equalTo(s).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    showDangerMessage("Unit name already exists!");
                } else {
                    unitsRef.child(newS).setValue(newS)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    showSuccessMessage("Update success!");
                                    dialogEdit.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    showDangerMessage("Update fail!");
                                }
                            });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDangerMessage("Database error: " + databaseError.getMessage());
            }
        });

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
}