package com.example.bookwormconnect;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class settingFragment extends Fragment {

    Switch switchNotifications;
    LinearLayout logoutLayout, deleteAccountLayout;

    FirebaseAuth mAuth;
    FirebaseUser user;

    DatabaseReference settingsRef;

    public settingFragment() {
    }

    @Override
    public void onResume() {
        super.onResume();
        ((HomeActivity) requireActivity()).hideSearchBar();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view= inflater.inflate(R.layout.fragment_setting, container, false);

        switchNotifications = view.findViewById(R.id.switchNotifications);
        logoutLayout = view.findViewById(R.id.logoutLayout);
        deleteAccountLayout = view.findViewById(R.id.deleteAccountLayout);


        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();


        if (user != null) {

            settingsRef = FirebaseDatabase.getInstance()
                    .getReference("Users")
                    .child(user.getUid())
                    .child("Settings");

            loadNotificationSetting();
        }

        // NOTIFICATION SWITCH
        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {

            settingsRef.child("notificationsEnabled").setValue(isChecked);

            Toast.makeText(requireContext(),
                    "Settings Updated",
                    Toast.LENGTH_SHORT).show();
        });

        // LOGOUT
        logoutLayout.setOnClickListener(v -> {

            mAuth.signOut();

            Intent intent = new Intent(requireContext(), LoginActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            requireActivity().finish();
        });

        // DELETE ACCOUNT
        deleteAccountLayout.setOnClickListener(v -> {

            new AlertDialog.Builder(requireContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to permanently delete your account?")
                    .setPositiveButton("Delete", (dialog, which) -> deleteAccount())
                    .setNegativeButton("Cancel", null)
                    .show();
        });

        return view;
    }

    private void loadNotificationSetting() {

        settingsRef.child("notificationsEnabled")
                .get()
                .addOnSuccessListener(snapshot -> {

                    if (snapshot.exists()) {

                        Boolean enabled = snapshot.getValue(Boolean.class);

                        if (enabled != null) {

                            switchNotifications.setChecked(enabled);
                        }
                    }
                });
    }

    private void deleteAccount() {

        if (user == null)
            return;

        String uid = user.getUid();

        // DELETE DATABASE DATA
        FirebaseDatabase.getInstance()
                .getReference("Users")
                .child(uid)
                .removeValue()
                .addOnCompleteListener(task -> {

                    // DELETE AUTH ACCOUNT
                    user.delete()
                            .addOnCompleteListener(task1 -> {

                                if (task1.isSuccessful()) {

                                    Toast.makeText(requireContext(),
                                            "Account Deleted",
                                            Toast.LENGTH_SHORT).show();

                                    Intent intent = new Intent(requireContext(),
                                            LoginActivity.class);

                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                                            Intent.FLAG_ACTIVITY_CLEAR_TASK);

                                    startActivity(intent);

                                    requireActivity().finish();

                                } else {

                                    Toast.makeText(requireContext(),
                                            task1.getException().getMessage(),
                                            Toast.LENGTH_LONG).show();
                                }
                            });
                });
    }
}