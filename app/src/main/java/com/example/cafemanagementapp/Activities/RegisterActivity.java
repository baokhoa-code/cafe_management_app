package com.example.cafemanagementapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import com.example.cafemanagementapp.Models.User;
import com.example.cafemanagementapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private Button btnRegist, btnGoLogin;
    private TextInputLayout txtFname, txtAddress, txtEmail, txtPhone, txtPassword;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_register);

        initUI();

        btnRegist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

        btnGoLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });

    }
    private void initUI(){
        btnRegist = findViewById(R.id.btnRegist);
        btnGoLogin = findViewById(R.id.btnGoLogin);
        txtFname = findViewById(R.id.register_name);
        txtAddress = findViewById(R.id.register_address);
        txtPhone = findViewById(R.id.register_phoneNo);
        txtEmail = findViewById(R.id.register_email);
        txtPassword = findViewById(R.id.register_password);
        progressDialog = new ProgressDialog(this);
    }
    private void  register(){
        String email, password, fname, address, phone;
        fname = String.valueOf(txtFname.getEditText().getText());
        address = String.valueOf(txtAddress.getEditText().getText());
        phone = String.valueOf(txtPhone.getEditText().getText());
        email = String.valueOf(txtEmail.getEditText().getText());
        password = String.valueOf(txtPassword.getEditText().getText());


        if(isNullOrEmpty(email) || isNullOrEmpty(password)) {
            showDangerMessage("Email and password can not be empty!");
        }else{
            progressDialog.setMessage("Registering...");
            progressDialog.show();

            FirebaseAuth auth = FirebaseAuth.getInstance();

            auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {

                                FirebaseDatabase database = FirebaseDatabase.getInstance();
                                DatabaseReference usersRef = database.getReference("users");

                                FirebaseUser firebaseUser = auth.getCurrentUser();
                                User user = new User();
                                user.setEmail(email);
                                user.setFullName(isNullOrEmpty(fname)?"":fname);
                                user.setAddress(isNullOrEmpty(address)?"":address);
                                user.setPhoneNumber(isNullOrEmpty(phone)?"":phone);

                                usersRef.child(firebaseUser.getUid()).setValue(user)
                                        .addOnSuccessListener(aVoid -> {
                                            progressDialog.dismiss();
                                            showSuccessMessage("Create success!");
                                        })
                                        .addOnFailureListener(e -> {
                                            progressDialog.dismiss();
                                            showDangerMessage("Create fail!");
                                        });
                                FirebaseAuth.getInstance().signOut();
                            } else {
                                progressDialog.dismiss();
                                Exception e = task.getException();
                                if (e instanceof FirebaseAuthUserCollisionException) {
                                    showDangerMessage("Your email already exists!");
                                } else {
                                    showDangerMessage(e.getMessage());
                                }

                            }
                        }
                    });
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
}