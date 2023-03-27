package com.example.cafemanagementapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.example.cafemanagementapp.Models.User;
import com.example.cafemanagementapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class UpdateUserActivity extends AppCompatActivity {

    private Button btnUpdate, btnEnterOldPass;
    private TextInputLayout txtFname, txtAddress, txtEmail, txtPhone,txtPassword;
    private ProgressDialog progressDialog;
    private TextInputLayout txtOldPass;
    private String oldPass;
    private User user_t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_user);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Update User Information");

        initUI();

        btnUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                update();
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
        btnUpdate = findViewById(R.id.btnUpdate);
        txtFname = findViewById(R.id.update_name);
        txtAddress = findViewById(R.id.update_address);
        txtPhone = findViewById(R.id.update_phoneNo);
        txtEmail = findViewById(R.id.update_email);
        txtPassword = findViewById(R.id.update_password);
        progressDialog = new ProgressDialog(this);
        getUserData();

    }

    private void getUserData(){
        progressDialog.setMessage("Getting data...");
        progressDialog.show();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference userRef = database.getReference("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());

        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // Get User object and use the values to update the UI
                User user = snapshot.getValue(User.class);
                progressDialog.dismiss();
                if(user != null) {
                    // User object contains the user information
                    String email = user.getEmail();
                    String fullName = user.getFullName();
                    String address = user.getAddress();
                    String phoneNumber = user.getPhoneNumber();

                    user_t = new User(email, fullName, address, phoneNumber);

                    txtEmail.getEditText().setText(email);
                    txtFname.getEditText().setText(fullName);
                    txtAddress.getEditText().setText(address);
                    txtPhone.getEditText().setText(phoneNumber);
                }else{
                    showDangerMessage("Cannot get data from server!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Failed to read value
                progressDialog.dismiss();
                showDangerMessage("Failed to read value!");
            }
        });
    }

    private void  update(){
        String password = String.valueOf(txtPassword.getEditText().getText());

        progressDialog.setMessage("Updating...");
        progressDialog.show();

        if(!isNullOrEmpty(password)){
            showEnterOldPassDialog();
        }else{
            updateNonPassword();
        }
    }

    private void updateNonPassword(){
        String fname, address, phone;
        fname = String.valueOf(txtFname.getEditText().getText());
        address = String.valueOf(txtAddress.getEditText().getText());
        phone = String.valueOf(txtPhone.getEditText().getText());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        DatabaseReference userRef = database.getReference("users").child(userId);

        user_t.setFullName(fname);
        user_t.setAddress(address);
        user_t.setPhoneNumber(phone);
        userRef.setValue(user_t)
                .addOnSuccessListener(aVoid -> {
                    progressDialog.dismiss();
                    showSuccessMessage("Update success!");
                })
                .addOnFailureListener(e -> {
                    progressDialog.dismiss();
                    showDangerMessage("Update fail!");
                });
    }

    private void updatePassword(){
        String password = String.valueOf(txtPassword.getEditText().getText());

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        DatabaseReference userRef = database.getReference("users").child(userId);


        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPass);

        user.reauthenticate(credential)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            // User re-authenticated successfully, update the email
                            user.updatePassword(password)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                updateNonPassword();
                                            } else {
                                                showDangerMessage("Update fail!");
                                            }
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            showDangerMessage("Update fail!");
                                        }
                                    });
                        } else {
                            // Failed to re-authenticate user
                        }
                    }
                });
    }

    private void showEnterOldPassDialog(){
        View view = LayoutInflater.from(this).inflate(R.layout.custom_dialog, null);

        txtOldPass = view.findViewById(R.id.old_password);
        btnEnterOldPass = view.findViewById(R.id.btnEnterOldPass);

        final AlertDialog.Builder builder= new AlertDialog.Builder(this);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();

        btnEnterOldPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String oldPassword = String.valueOf(txtOldPass.getEditText().getText());
                if(!isNullOrEmpty(oldPassword)){
                    oldPass = oldPassword;
                    dialog.dismiss();
                    updatePassword();
                }else{
                    showDangerMessage("Please enter old password!");
                }
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