package com.example.cafemanagementapp.Fragments;

import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cafemanagementapp.Activities.ImportActivity;
import com.example.cafemanagementapp.Adapters.ImportAdapter;
import com.example.cafemanagementapp.Adapters.ImportFragAdapter;
import com.example.cafemanagementapp.Adapters.SpinnerDrinkAdapter;
import com.example.cafemanagementapp.Models.Drink;
import com.example.cafemanagementapp.Models.Import;
import com.example.cafemanagementapp.Models.Revenue;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ImportFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ImportFragment extends Fragment {

    private Drink target;
    private RecyclerView rclvImportFragList;
    private ImportFragAdapter importFragAdapter;
    private TextView importFragTotalQuantity;
    private TextView importFragTotalPrice;
    private FloatingActionButton btnImportFragAdd;

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

    private View view;

    private int yearCurrent, monthCurrent, dayOfMonthCurrent;
    public ImportFragment() {
    }

    public static ImportFragment newInstance(String param1, String param2) {
        ImportFragment fragment = new ImportFragment();
        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_import, container, false);

        target = (Drink) getArguments().getSerializable("test");

        initUI();

        btnImportFragAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 = LayoutInflater.from(getActivity()).inflate(R.layout.add_import_dialog, null);

                newImportDrink = view2.findViewById(R.id.newImportDrink);
                newImportQuantity = view2.findViewById(R.id.newImportQuantity);
                newImportUnitPrice = view2.findViewById(R.id.newImportUnitPrice);
                newImportUnit = view2.findViewById(R.id.newImportUnit);
                btnnewImportOk = view2.findViewById(R.id.btnnewImportOk);
                btnnewImportCancel = view2.findViewById(R.id.btnnewImportCancel);

                SpinnerDrinkAdapter adapter = new SpinnerDrinkAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, drinkList);
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

                int position = -1;
                for (int i = 0; i < drinkList.size(); i++) {
                    Drink drink = drinkList.get(i);
                    if (drink.getId().equals(target.getId())) {
                        position = i;
                        break;
                    }
                }
                newImportDrink.setSelection(position);
                newImportDrink.setEnabled(false);

                final AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
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

        return view;
    }

    private void initUI(){
        rclvImportFragList = view.findViewById(R.id.rclvImportFragList);
        btnImportFragAdd = view.findViewById(R.id.btnImportFragAdd);
        importFragTotalQuantity = view.findViewById(R.id.importFragTotalQuantity);
        importFragTotalPrice = view.findViewById(R.id.importFragTotalPrice);
        drinkList = new ArrayList<Drink>();
        importList = new ArrayList<Import>();
        progressDialog = new ProgressDialog(getActivity());

        yearCurrent = Calendar.getInstance().get(Calendar.YEAR);
        monthCurrent = Calendar.getInstance().get(Calendar.MONTH) + 1 ;
        dayOfMonthCurrent = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        getDrinks();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rclvImportFragList.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        rclvImportFragList.addItemDecoration(dividerItemDecoration);

        importFragAdapter = new ImportFragAdapter(importList, new ImportFragAdapter.IClickListener() {
            @Override
            public void onClickEditItem(Import import_t) {
                View view2 = LayoutInflater.from(getActivity()).inflate(R.layout.edit_import_dialog, null);

                editImportDrink = view2.findViewById(R.id.editImportDrink);
                editImportQuantity = view2.findViewById(R.id.editImportQuantity);
                editImportUnitPrice = view2.findViewById(R.id.editImportUnitPrice);
                editImportUnit = view2.findViewById(R.id.editImportUnit);
                btneditImportOk = view2.findViewById(R.id.btneditImportOk);
                btneditImportCancel = view2.findViewById(R.id.btneditImportCancel);

                SpinnerDrinkAdapter adapter = new SpinnerDrinkAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, drinkList);
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
                editImportDrink.setEnabled(false);
                editImportQuantity.setText(import_t.getQuantity() + "");
                editImportUnitPrice.setText(import_t.getUnitPrice() + "");

                final AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
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
                View view2 = LayoutInflater.from(getActivity()).inflate(R.layout.yes_no_dialog, null);

                TextView tile = view2.findViewById(R.id.yesNoDialogConfirmTile);
                TextView content = view2.findViewById(R.id.yesNoDialogContent);
                Button btnNo = view2.findViewById(R.id.btnYesNoDialogNo);
                Button btnYes = view2.findViewById(R.id.btnYesNoDialogYes);

                tile.setText("Confirm Box");
                content.setText("Do you really want to delete this import?");

                final AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
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
                        importFragAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
            }
        });

        rclvImportFragList.setAdapter(importFragAdapter);

        getData();
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
                    if(import_temp.getIdDrink().equals(target.getId())){
                        importList.add(import_temp);
                    }
                }
                progressDialog.dismiss();
                importFragAdapter.notifyDataSetChanged();
                calQuantity();
                calTotalPriceOfAll();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                showDangerMessage("Failed to read value.");
                progressDialog.dismiss();
            }
        });
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

    private void calQuantity(){
        if ( importList == null || importList.isEmpty()) {
            importFragTotalQuantity.setText(String.valueOf(0));
            return;
        }

        int totalQuantity = 0;
        for (int i = 0; i < importList.size(); i++) {
            Import import_t = importList.get(i);
            totalQuantity += import_t.getQuantity();
        }
        importFragTotalQuantity.setText(String.valueOf(totalQuantity));
    }
    private void calTotalPriceOfAll(){
        if ( importList == null || importList.isEmpty()) {
            importFragTotalPrice.setText("- " + String.valueOf(0) + " 000 VNĐ");

            return;
        }

        int totalPriceOfAll = 0;
        for (int i = 0; i < importList.size(); i++) {
            Import import_t = importList.get(i);
            totalPriceOfAll += import_t.getTotalPrice();
        }
        importFragTotalPrice.setText("- " + String.valueOf(totalPriceOfAll) + " 000 VNĐ");
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

    private void showDangerMessage(String s){
        View parentLayout = view.findViewById(android.R.id.content);
        Snackbar snackbar =  Snackbar.make(getView(), s, Snackbar.LENGTH_LONG);
        snackbar.getView().setZ(9999);
        snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        snackbar.getView().setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.snackbar_danger)));
        snackbar.show();
    }

    private void showWarningMessage(String s){
        View parentLayout = view.findViewById(android.R.id.content);
        Snackbar snackbar =  Snackbar.make(getView(), s, Snackbar.LENGTH_LONG);
        snackbar.getView().setZ(9999);
        snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        snackbar.getView().setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.snackbar_warning)));
        snackbar.show();
    }

    private void showInfoMessage(String s){
        View parentLayout = view.findViewById(android.R.id.content);
        Snackbar snackbar =  Snackbar.make(getView(), s, Snackbar.LENGTH_LONG);
        snackbar.getView().setZ(9999);
        snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        snackbar.getView().setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.snackbar_infor)));
        snackbar.show();
    }
    private void showSuccessMessage(String s){
        View parentLayout = view.findViewById(android.R.id.content);
        Snackbar snackbar =  Snackbar.make(getView(), s, Snackbar.LENGTH_LONG);
        snackbar.getView().setZ(9999);
        snackbar.setActionTextColor(ContextCompat.getColor(getActivity(), R.color.white));
        snackbar.getView().setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(getActivity(), R.color.snackbar_success)));
        snackbar.show();
    }

    private void showToast(String message){
        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
    }
}