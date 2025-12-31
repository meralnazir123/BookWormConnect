package com.example.bookwormconnect;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MybooksFragment extends Fragment {

    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) requireActivity()).hideSearchBar();
        ((HomeActivity) requireActivity()).hideCamera();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_mybooks, container, false);
    }
}
