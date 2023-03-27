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

import android.text.InputFilter;
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
import com.example.cafemanagementapp.Activities.SellActivity;
import com.example.cafemanagementapp.Adapters.ImportAdapter;
import com.example.cafemanagementapp.Adapters.ImportFragAdapter;
import com.example.cafemanagementapp.Adapters.SellFragAdapter;
import com.example.cafemanagementapp.Adapters.SpinnerDrinkAdapter;
import com.example.cafemanagementapp.Helpers.InputFilterMinMax;
import com.example.cafemanagementapp.Models.Drink;
import com.example.cafemanagementapp.Models.Import;
import com.example.cafemanagementapp.Models.Revenue;
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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class SellFragment extends Fragment {

    private Drink target;
    private RecyclerView rclvSellFragList;
    private SellFragAdapter sellFragAdapter;
    private TextView sellFragTotalQuantity;
    private TextView sellFragTotalPrice;
    private FloatingActionButton btnSellFragAdd;

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

    private View view;

    private int yearCurrent, monthCurrent, dayOfMonthCurrent;
    public SellFragment() {
    }

    public static SellFragment newInstance(String param1, String param2) {
        SellFragment fragment = new SellFragment();
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
        view = inflater.inflate(R.layout.fragment_sell, container, false);

        target = (Drink) getArguments().getSerializable("test");

        initUI();

        btnSellFragAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                View view2 = LayoutInflater.from(getActivity()).inflate(R.layout.add_sell_dialog, null);

                newSellDrink = view2.findViewById(R.id.newSellDrink);
                newSellQuantity = view2.findViewById(R.id.newSellQuantity);
                newSellUnitPrice = view2.findViewById(R.id.newSellUnitPrice);
                newSellUnit = view2.findViewById(R.id.newSellUnit);
                btnnewSellOk = view2.findViewById(R.id.btnnewSellOk);
                btnnewSellCancel = view2.findViewById(R.id.btnnewSellCancel);

                SpinnerDrinkAdapter adapter = new SpinnerDrinkAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, drinkList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                newSellDrink.setAdapter(adapter);

                newSellDrink.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view,
                                               int position, long id) {
                        Drink drink = adapter.getItem(position);
                        newSellUnit.setText(drink.getUnit());

                        SellActivity.InStockCallback callback = new SellActivity.InStockCallback() {
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

                int position = -1;
                for (int i = 0; i < drinkList.size(); i++) {
                    Drink drink = drinkList.get(i);
                    if (drink.getId().equals(target.getId())) {
                        position = i;
                        break;
                    }
                }
                newSellDrink.setSelection(position);
                newSellDrink.setEnabled(false);

                final AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
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

        return view;
    }

    private void initUI(){
        rclvSellFragList = view.findViewById(R.id.rclvSellFragList);
        btnSellFragAdd = view.findViewById(R.id.btnSellFragAdd);
        sellFragTotalQuantity = view.findViewById(R.id.sellFragTotalQuantity);
        sellFragTotalPrice = view.findViewById(R.id.sellFragTotalPrice);
        drinkList = new ArrayList<Drink>();
        sellList = new ArrayList<Sell>();
        progressDialog = new ProgressDialog(getActivity());

        yearCurrent = Calendar.getInstance().get(Calendar.YEAR);
        monthCurrent = Calendar.getInstance().get(Calendar.MONTH) + 1 ;
        dayOfMonthCurrent = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);

        getDrinks();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        rclvSellFragList.setLayoutManager(linearLayoutManager);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL);
        rclvSellFragList.addItemDecoration(dividerItemDecoration);

        sellFragAdapter = new SellFragAdapter(sellList, new SellFragAdapter.IClickListener() {
            @Override
            public void onClickEditItem(Sell sell_t) {
                View view2 = LayoutInflater.from(getActivity()).inflate(R.layout.edit_sell_dialog, null);

                editSellDrink = view2.findViewById(R.id.editSellDrink);
                editSellQuantity = view2.findViewById(R.id.editSellQuantity);
                editSellUnitPrice = view2.findViewById(R.id.editSellUnitPrice);
                editSellUnit = view2.findViewById(R.id.editSellUnit);
                btneditSellOk = view2.findViewById(R.id.btneditSellOk);
                btneditSellCancel = view2.findViewById(R.id.btneditSellCancel);

                SpinnerDrinkAdapter adapter = new SpinnerDrinkAdapter(getActivity(), android.R.layout.simple_spinner_dropdown_item, drinkList);
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

                SellActivity.InStockCallback callback = new SellActivity.InStockCallback() {
                    @Override
                    public void onInStockReceived(int inStock) {
                        editSellQuantity.setFilters(new InputFilter[]{ new InputFilterMinMax("1", inStock + "")});
                        String temps = editSellUnit.getText().toString();
                        editSellUnit.setText("(Max: " + inStock +") " + temps );
                    }
                };

                inStock(sell_t.getIdDrink(), callback);

                final AlertDialog.Builder builder= new AlertDialog.Builder(getActivity());
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
                View view2 = LayoutInflater.from(getActivity()).inflate(R.layout.yes_no_dialog, null);

                TextView tile = view2.findViewById(R.id.yesNoDialogConfirmTile);
                TextView content = view2.findViewById(R.id.yesNoDialogContent);
                Button btnNo = view2.findViewById(R.id.btnYesNoDialogNo);
                Button btnYes = view2.findViewById(R.id.btnYesNoDialogYes);

                tile.setText("Confirm Box");
                content.setText("Do you really want to delete this sell?");

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
                        DatabaseReference sellsRef = FirebaseDatabase.getInstance().getReference().child("sells");
                        sellsRef.child(sell_t.getId()).removeValue();
                        sellFragAdapter.notifyDataSetChanged();
                        dialog.dismiss();
                    }
                });
            }
        });

        rclvSellFragList.setAdapter(sellFragAdapter);

        getData();
    }
    private void getData(){
        progressDialog.setMessage("Getting data...");
        progressDialog.show();

        DatabaseReference unitsRef = FirebaseDatabase.getInstance().getReference("sells");

        unitsRef.orderByKey().addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(sellList != null ) sellList.clear();
                for(DataSnapshot snapshot : dataSnapshot.getChildren()){
                    Sell sell_temp = snapshot.getValue(Sell.class);
                    if(sell_temp.getIdDrink().equals(target.getId())){
                        sellList.add(sell_temp);
                    }
                }
                progressDialog.dismiss();
                sellFragAdapter.notifyDataSetChanged();
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

    private void inStock(String drinkId, final SellActivity.InStockCallback callback) {
        SellActivity.TotalQuantityCallback importCallback = new SellActivity.TotalQuantityCallback() {
            @Override
            public void onTotalQuantityReceived(int importQuantity) {
                SellActivity.TotalQuantityCallback sellCallback = new SellActivity.TotalQuantityCallback() {
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

    private void totalImport(String drinkId, SellActivity.TotalQuantityCallback callback){
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
                Toast.makeText(getActivity(), "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void totalSell(String drinkId, SellActivity.TotalQuantityCallback callback){
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
                Toast.makeText(getActivity(), "Database Error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void calQuantity(){
        if ( sellList == null || sellList.isEmpty()) {
            sellFragTotalQuantity.setText(String.valueOf(0));
            return;
        }

        int totalQuantity = 0;
        for (int i = 0; i < sellList.size(); i++) {
            Sell sell_t = sellList.get(i);
            totalQuantity += sell_t.getQuantity();
        }
        sellFragTotalQuantity.setText(String.valueOf(totalQuantity));
    }
    private void calTotalPriceOfAll(){
        if ( sellList == null || sellList.isEmpty()) {
            sellFragTotalPrice.setText("+ " + String.valueOf(0) + " 000 VNĐ");
            return;
        }

        int totalPriceOfAll = 0;
        for (int i = 0; i < sellList.size(); i++) {
            Sell sell_t = sellList.get(i);
            totalPriceOfAll += sell_t.getTotalPrice();
        }
        sellFragTotalPrice.setText("+ " + String.valueOf(totalPriceOfAll) + " 000 VNĐ");
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