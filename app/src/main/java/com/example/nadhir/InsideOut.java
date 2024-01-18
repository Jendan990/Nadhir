package com.example.nadhir;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;

public class InsideOut extends AppCompatActivity {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private InsideOutAdapter insideOutAdapter;
    String[] titles = new String[]{"InsideTenants","OutsideTenants"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inside_out);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setElevation(0);
        //assigning  our xml components
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewpager);

        //initializing viewpager adapter
        insideOutAdapter = new InsideOutAdapter(InsideOut.this);

        //setting the adapter to viewpager
        viewPager.setAdapter(insideOutAdapter);

        //implementing the mediator
        new TabLayoutMediator(tabLayout,viewPager,(((tab, position) -> tab.setText(titles[position])))).attach();
        //extracting extras
        String name = getIntent().getStringExtra("house_name");



    }


}