package com.example.bookwormconnect;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class MyBooksPagerAdapter extends FragmentStateAdapter {

    public MyBooksPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {

            case 0:
                return new MyPostsFragment();

            case 1:
                return new BorrowedBooksFragment();

            case 2:
                return new LentBooksFragment();

            default:
                return new MyPostsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}