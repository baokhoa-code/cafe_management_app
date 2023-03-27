package com.example.cafemanagementapp.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.cafemanagementapp.Models.Drink;
import com.example.cafemanagementapp.R;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class TestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        Button button = findViewById(R.id.btnTest);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DatabaseReference drinksRef = FirebaseDatabase.getInstance().getReference().child("drinks");

                Drink drink = new Drink();
                drink.setName("Cola");
                drink.setManufacture("Coca-Cola");
                drink.setDescription("A carbonated soft drink");
                drink.setUnit("Can");
                drink.setImage("Image link");

                String key = drinksRef.push().getKey();
                drink.setId(key);
                drinksRef.child(key).setValue(drink);
            }
        });
    }
}