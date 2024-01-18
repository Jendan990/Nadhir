package com.example.nadhir;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.ArrayList;

public class InsideOutAdapter extends FragmentStateAdapter {
    //initializing array of tab titles
    String[] titles = new String[]{"InsideTenants","OutsideTenants"};
    public InsideOutAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0:
                return new InsideFragment();
            case 1:
                return new OutsideFragment();
        }
        return new InsideFragment();
    }

    @Override
    public int getItemCount() {
        return titles.length;
    }
}
