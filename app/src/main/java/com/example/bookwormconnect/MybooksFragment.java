package com.example.bookwormconnect;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MybooksFragment extends Fragment {


    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) requireActivity()).hideSearchBar();

    }
    TabLayout tabLayout;
    ViewPager2 viewPager;
    MyBooksPagerAdapter adapter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=  inflater.inflate(R.layout.fragment_mybooks, container, false);
        tabLayout = view.findViewById(R.id.tabLayout);
        viewPager = view.findViewById(R.id.viewPager);

        adapter = new MyBooksPagerAdapter(requireActivity());
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) -> {

                    switch (position) {

                        case 0:
                            tab.setText("My Posts");
                            break;

                        case 1:
                            tab.setText("Borrowed");
                            break;

                        case 2:
                            tab.setText("Lent");
                            break;
                    }

                }).attach();

        return view;
    }
}
