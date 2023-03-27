package com.example.cafemanagementapp.Activities;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cafemanagementapp.Adapters.DrinkAdapter;
import com.example.cafemanagementapp.Adapters.UnitAdapter;
import com.example.cafemanagementapp.Models.Drink;
import com.example.cafemanagementapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class DrinkActivity extends AppCompatActivity {

    private SearchView drinkSearch;
    private TextView drinkDeleteAll;
    private RecyclerView rclvDrinkList;
    private FloatingActionButton btnDrinkAdd;
    private DrinkAdapter drinkAdapter;
    private List<Drink> drinkList;
    private ProgressDialog progressDialog;
    private AlertDialog dialogAdd;
    private AlertDialog dialogEdit;
    private List<String> unitList;

    //element of add ui
    private FrameLayout newDrinkLayoutImage;
    private ImageView newDrinkImage;
    private TextView newDrinkTextAddImage;
    private EditText newDrinkName;
    private Spinner newDrinkUnit;
    private EditText newDrinkManufacture;
    private EditText newDrinkDescription;
    private  Button btnnewDrinkNameCancel;
    private Button btnnewDrinkNameOk;
    private String newDrinkEncodedImage;

    //element of edit ui
    private FrameLayout editDrinkLayoutImage;
    private ImageView editDrinkImage;
    private TextView editDrinkTextEditImage;
    private EditText editDrinkName;
    private Spinner editDrinkUnit;
    private EditText editDrinkManufacture;
    private EditText editDrinkDescription;
    private  Button btneditDrinkNameCancel;
    private Button btneditDrinkNameOk;
    private String editDrinkEncodedImage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Drink management");

        initUI();

        btnDrinkAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 = LayoutInflater.from(DrinkActivity.this).inflate(R.layout.add_drink_dialog, null);

                newDrinkUnit = view2.findViewById(R.id.newDrinkUnit);
                newDrinkManufacture = view2.findViewById(R.id.newDrinkManufacture);
                newDrinkDescription = view2.findViewById(R.id.newDrinkDescription);
                newDrinkImage = view2.findViewById(R.id.newDrinkImage);
                newDrinkLayoutImage = view2.findViewById(R.id.newDrinkLayoutImage);
                newDrinkName = view2.findViewById(R.id.newDrinkName);
                newDrinkTextAddImage = view2.findViewById(R.id.newDrinkTextAddImage);
                btnnewDrinkNameOk = view2.findViewById(R.id.btnnewDrinkNameOk);
                btnnewDrinkNameCancel = view2.findViewById(R.id.btnnewDrinkNameCancel);

                ArrayAdapter<String> adapter = new ArrayAdapter<>(DrinkActivity.this, android.R.layout.simple_spinner_item, unitList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                newDrinkUnit.setAdapter(adapter);

                final AlertDialog.Builder builder= new AlertDialog.Builder(DrinkActivity.this);
                builder.setView(view2);
                dialogAdd = builder.create();
                dialogAdd.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogAdd.show();

                btnnewDrinkNameCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogAdd.dismiss();
                    }
                });

                btnnewDrinkNameOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isNewDrinkValid()){
                            DatabaseReference drinksRef = FirebaseDatabase.getInstance().getReference().child("drinks");

                            Drink drink_temp = new Drink();

                            drink_temp.setName(newDrinkName.getText().toString());
                            drink_temp.setUnit(newDrinkUnit.getSelectedItem().toString());
                            drink_temp.setDescription(newDrinkDescription.getText().toString());
                            drink_temp.setManufacture(newDrinkManufacture.getText().toString());
                            drink_temp.setImage(newDrinkEncodedImage);

                            String key = drinksRef.push().getKey();
                            drink_temp.setId(key);
                            drinksRef.orderByChild("name").equalTo(drink_temp.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        showDangerMessage("Drink existed!");
                                    } else {
                                        drinksRef.child(key).setValue(drink_temp)
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
                                                });;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Xử lý lỗi ở đây
                                }
                            });

                        }
                    }
                });

                newDrinkLayoutImage.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickImage.launch(intent);
                });
            }
        });

        drinkDeleteAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(drinkList.size() >0){
                    View view2 = LayoutInflater.from(DrinkActivity.this).inflate(R.layout.yes_no_dialog, null);

                    TextView tile = view2.findViewById(R.id.yesNoDialogConfirmTile);
                    TextView content = view2.findViewById(R.id.yesNoDialogContent);
                    Button btnNo = view2.findViewById(R.id.btnYesNoDialogNo);
                    Button btnYes = view2.findViewById(R.id.btnYesNoDialogYes);

                    tile.setText("Confirm Box");
                    content.setText("Do you really want to delete all record of Drinks?");

                    final AlertDialog.Builder builder= new AlertDialog.Builder(DrinkActivity.this);
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
                            DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference().child("drinks");
                            unitsRef.removeValue();
                            DatabaseReference unitsRef2 = FirebaseDatabase.getInstance().getReference().child("imports");
                            unitsRef2.removeValue();
                            DatabaseReference unitsRef3 = FirebaseDatabase.getInstance().getReference().child("sells");
                            unitsRef3.removeValue();
//                            unitAdapter.notifyDataSetChanged();
                            dialog.dismiss();
                        }
                    });
                }
            }
        });

        drinkSearch.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

    private void searchData(String keyword){

        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("drinks");

        unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(drinkList != null ) drinkList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Drink tempp = snapshot.getValue(Drink.class);
                    if(tempp.getName().contains(keyword)){
                        drinkList.add(tempp);
                    }
                }
                drinkAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDangerMessage("Failed to read value.");
                progressDialog.dismiss();
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
        drinkSearch = findViewById(R.id.drinkSearch);
        drinkDeleteAll = findViewById(R.id.drinkDeleteAll);
        rclvDrinkList = findViewById(R.id.rclvDrinkList);
        btnDrinkAdd = findViewById(R.id.btnDrinkAdd);
        drinkList = new ArrayList<Drink>();
        unitList = new ArrayList<String>();
        progressDialog = new ProgressDialog(this);
        getUnits();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        rclvDrinkList.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        rclvDrinkList.addItemDecoration(dividerItemDecoration);

        drinkAdapter = new DrinkAdapter(drinkList, new DrinkAdapter.IClickListener() {
            @Override
            public void onClickEditItem(Drink drink) {
                View view2 = LayoutInflater.from(DrinkActivity.this).inflate(R.layout.edit_drink_dialog, null);

                editDrinkUnit = view2.findViewById(R.id.editDrinkUnit);
                editDrinkManufacture = view2.findViewById(R.id.editDrinkManufacture);
                editDrinkDescription = view2.findViewById(R.id.editDrinkDescription);
                editDrinkImage = view2.findViewById(R.id.editDrinkImage);
                editDrinkLayoutImage = view2.findViewById(R.id.editDrinkLayoutImage);
                editDrinkName = view2.findViewById(R.id.editDrinkName);
                editDrinkTextEditImage = view2.findViewById(R.id.editDrinkTextEditImage);
                btneditDrinkNameOk = view2.findViewById(R.id.btneditDrinkNameOk);
                btneditDrinkNameCancel = view2.findViewById(R.id.btneditDrinkNameCancel);

                editDrinkName.setText(drink.getName());
                editDrinkManufacture.setText(drink.getManufacture());
                editDrinkDescription.setText(drink.getDescription());
                editDrinkImage.setImageBitmap(getDrinkImage(drink.getImage()));
                editDrinkTextEditImage.setVisibility(View.GONE);
                editDrinkEncodedImage = encodedImage(getDrinkImage(drink.getImage()));



                ArrayAdapter<String> adapter = new ArrayAdapter<>(DrinkActivity.this, android.R.layout.simple_spinner_item, unitList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                editDrinkUnit.setAdapter(adapter);

                int position = unitList.indexOf(drink.getUnit());
                editDrinkUnit.setSelection(position);

                final AlertDialog.Builder builder= new AlertDialog.Builder(DrinkActivity.this);
                builder.setView(view2);
                dialogEdit = builder.create();
                dialogEdit.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                dialogEdit.show();

                btneditDrinkNameCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dialogEdit.dismiss();
                    }
                });

                btneditDrinkNameOk.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(isNewDrinkValidEdit()){
                            DatabaseReference drinksRef = FirebaseDatabase.getInstance().getReference().child("drinks");

                            Drink drink_temp = new Drink();

                            drink_temp.setName(editDrinkName.getText().toString());
                            drink_temp.setUnit(editDrinkUnit.getSelectedItem().toString());
                            drink_temp.setDescription(editDrinkDescription.getText().toString());
                            drink_temp.setManufacture(editDrinkManufacture.getText().toString());
                            drink_temp.setImage(editDrinkEncodedImage);
                            drink_temp.setId(drink.getId());

                            drinksRef.orderByChild("name").equalTo(drink_temp.getName()).addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(DataSnapshot dataSnapshot) {
                                    if (dataSnapshot.exists()) {
                                        showDangerMessage("Drink existed!");
                                    } else {
                                        drinksRef.child(drink_temp.getId()).setValue(drink_temp)
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
                                                });;
                                    }
                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError) {
                                    // Xử lý lỗi ở đây
                                }
                            });

                        }
                    }
                });

                editDrinkLayoutImage.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                    pickImageEdit.launch(intent);
                });
            }
            @Override
            public void onClickDeleteItem(Drink drink) {
                View view2 = LayoutInflater.from(DrinkActivity.this).inflate(R.layout.yes_no_dialog, null);

                TextView tile = view2.findViewById(R.id.yesNoDialogConfirmTile);
                TextView content = view2.findViewById(R.id.yesNoDialogContent);
                Button btnNo = view2.findViewById(R.id.btnYesNoDialogNo);
                Button btnYes = view2.findViewById(R.id.btnYesNoDialogYes);

                tile.setText("Confirm Box");
                content.setText("Do you really want to delete " + drink.getName() + "?");

                final AlertDialog.Builder builder= new AlertDialog.Builder(DrinkActivity.this);
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
                        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference().child("drinks");
                        unitsRef.child(drink.getId()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if(task.isSuccessful()){
                                    deleteImportByIdDrink(drink.getId());
                                    drinkAdapter.notifyDataSetChanged();
                                    dialog.dismiss();
                                }
                            }
                        });

                    }
                });
            }
        }
    );

        rclvDrinkList.setAdapter(drinkAdapter);

        getData();
    }

    private void getData(){
        progressDialog.setMessage("Getting data...");
        progressDialog.show();

        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("drinks");

        unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(drinkList != null ) drinkList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Drink drink_temp = snapshot.getValue(Drink.class);
                    drinkList.add(drink_temp);
                }
                progressDialog.dismiss();
                drinkAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDangerMessage("Failed to read value.");
                progressDialog.dismiss();
            }
        });
    }

    private final ActivityResultLauncher<Intent> pickImage = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if(result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            newDrinkImage.setImageBitmap(bitmap);
                            newDrinkTextAddImage.setVisibility(View.GONE);
                            newDrinkEncodedImage = encodedImage(bitmap);
                        } catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );

    private final ActivityResultLauncher<Intent> pickImageEdit = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK){
                    if(result.getData() != null) {
                        Uri imageUri = result.getData().getData();
                        try{
                            InputStream inputStream = getContentResolver().openInputStream(imageUri);
                            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                            editDrinkImage.setImageBitmap(bitmap);
                            editDrinkTextEditImage.setVisibility(View.GONE);
                            editDrinkEncodedImage = encodedImage(bitmap);
                        } catch (FileNotFoundException e){
                            e.printStackTrace();
                        }
                    }
                }
            }
    );
    private  String encodedImage(Bitmap bitmap){
        int previewWidth = 150;
        int previewHeight = bitmap.getHeight() * previewWidth / bitmap.getWidth();
        Bitmap previewBitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false);
        ByteArrayOutputStream byteArrayOutputSream = new ByteArrayOutputStream();
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputSream);
        byte[] bytes = byteArrayOutputSream.toByteArray();
        return Base64.encodeToString(bytes, Base64.DEFAULT);
    }
    private void getUnits(){
        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("units");

        unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(unitList != null ) unitList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String unit = snapshot.getValue(String.class);
                    unitList.add(unit);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDangerMessage("Failed to read value.");
            }
        });
    }

    private List<String> getUnitsForEdit(){
        List<String> temp = new ArrayList<String>();
        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("units");

        unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    String unit = snapshot.getValue(String.class);
                    temp.add(unit);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDangerMessage("Failed to read value.");
            }
        });
        return temp;
    }

    private Bitmap getDrinkImage(String encodedImage){
        byte[] bytes = Base64.decode(encodedImage, Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
    }

    private boolean isNewDrinkValid(){
        if (newDrinkEncodedImage == null) {
            showToast("Image is empty!");
            return false;
        } else if (newDrinkName.getText().toString().trim().isEmpty()) {
            showToast("Drink name is empty!");
            return false;
        } else if (newDrinkManufacture.getText().toString().trim().isEmpty()) {
            showToast("Drink manufacture is empty!");
            return false;
        } else if (newDrinkDescription.getText().toString().trim().isEmpty()) {
            showToast("Drink description is empty!");
            return false;
        } else {
            return true;
        }
    }

    private boolean isNewDrinkValidEdit(){
        if (editDrinkEncodedImage == null) {
            showToast("Image is empty!");
            return false;
        } else if (editDrinkName.getText().toString().trim().isEmpty()) {
            showToast("Drink name is empty!");
            return false;
        } else if (editDrinkManufacture.getText().toString().trim().isEmpty()) {
            showToast("Drink manufacture is empty!");
            return false;
        } else if (editDrinkDescription.getText().toString().trim().isEmpty()) {
            showToast("Drink description is empty!");
            return false;
        } else {
            return true;
        }
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
}