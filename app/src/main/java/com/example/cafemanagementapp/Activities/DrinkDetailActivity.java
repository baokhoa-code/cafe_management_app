package com.example.cafemanagementapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;

import com.example.cafemanagementapp.Adapters.ViewPagerAdapter;
import com.example.cafemanagementapp.Fragments.ImportFragment;
import com.example.cafemanagementapp.Fragments.SellFragment;
import com.example.cafemanagementapp.Models.Drink;
import com.example.cafemanagementapp.R;
import com.google.android.material.tabs.TabLayout;

public class DrinkDetailActivity extends AppCompatActivity {

    private Drink target;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private ViewPagerAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drink_detail);

        target = (Drink) getIntent().getSerializableExtra("drink");

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Detail of " + target.getName());

        initUI();

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                Intent intent = new Intent(DrinkDetailActivity.this, StoreActivity.class);
                startActivity(intent);
                this.finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUI(){
        viewPager = findViewById(R.id.view_pager);
        tabLayout = findViewById(R.id.tab_layout);

        adapter = new ViewPagerAdapter(getSupportFragmentManager());

        Bundle bundle = new Bundle();
        bundle.putSerializable("test", target);

        ImportFragment importFragment = new ImportFragment();
        importFragment.setArguments(bundle);
        adapter.addFragment(importFragment, "Import");

        SellFragment sellFragment = new SellFragment();
        sellFragment.setArguments(bundle);
        adapter.addFragment(sellFragment, "Sell");

        viewPager.setAdapter(adapter);
        tabLayout.setupWithViewPager(viewPager);
    }
}