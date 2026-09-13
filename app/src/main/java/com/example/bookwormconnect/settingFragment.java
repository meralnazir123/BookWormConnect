package com.example.bookwormconnect;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

public class settingFragment extends Fragment {

    Switch switchNotifications;
    LinearLayout logoutLayout, deleteAccountLayout;

    FirebaseAuth mAuth;
    FirebaseUser user;

    DatabaseReference settingsRef;
    SharedPreferences settingsPrefs;

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
        settingsPrefs = requireContext()
                .getSharedPreferences("SettingsPrefs", Context.MODE_PRIVATE);


        if (user != null) {

            settingsRef = FirebaseDatabase.getInstance()
                    .getReference("users")
                    .child(user.getUid())
                    .child("Settings");

            loadNotificationSetting();
        }

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (settingsRef != null) {

                settingsRef.child("notificationsEnabled")
                        .setValue(isChecked);

                settingsPrefs.edit()
                        .putBoolean("notificationsEnabled", isChecked)
                        .apply();

                Toast.makeText(
                        requireContext(),
                        "Settings Updated",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        logoutLayout.setOnClickListener(v -> {

            mAuth.signOut();

            Intent intent = new Intent(requireContext(), LoginActivity.class);

            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                    Intent.FLAG_ACTIVITY_CLEAR_TASK);

            startActivity(intent);

            requireActivity().finish();
        });

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

        boolean savedValue = settingsPrefs.getBoolean(
                "notificationsEnabled",
                true
        );

        switchNotifications.setOnCheckedChangeListener(null);

        switchNotifications.setChecked(savedValue);

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {

            if (settingsRef != null) {

                settingsRef.child("notificationsEnabled")
                        .setValue(isChecked);

                settingsPrefs.edit()
                        .putBoolean("notificationsEnabled", isChecked)
                        .apply();

                Toast.makeText(
                        requireContext(),
                        "Settings Updated",
                        Toast.LENGTH_SHORT
                ).show();
            }
        });

        if (settingsRef != null) {

            settingsRef.child("notificationsEnabled")
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        if (snapshot.exists()) {

                            Boolean enabled =
                                    snapshot.getValue(Boolean.class);

                            if (enabled != null) {

                                switchNotifications
                                        .setOnCheckedChangeListener(null);

                                switchNotifications.setChecked(enabled);

                                settingsPrefs.edit()
                                        .putBoolean(
                                                "notificationsEnabled",
                                                enabled
                                        )
                                        .apply();

                                switchNotifications
                                        .setOnCheckedChangeListener(
                                                (buttonView, isChecked) -> {

                                                    settingsRef
                                                            .child("notificationsEnabled")
                                                            .setValue(isChecked);

                                                    settingsPrefs.edit()
                                                            .putBoolean(
                                                                    "notificationsEnabled",
                                                                    isChecked
                                                            )
                                                            .apply();

                                                    Toast.makeText(
                                                            requireContext(),
                                                            "Settings Updated",
                                                            Toast.LENGTH_SHORT
                                                    ).show();
                                                }
                                        );
                            }
                        }
                    });
        }
    }
    private void deleteAccount() {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

            if (user == null) {
                Toast.makeText(
                        requireContext(),
                        "No user logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            String uid = user.getUid();

            FirebaseFirestore db = FirebaseFirestore.getInstance();

            db.collection("posts")
                    .whereEqualTo("userId", uid)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {

                        WriteBatch batch = db.batch();

                        for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                            batch.delete(document.getReference());
                        }

                        batch.commit()
                                .addOnSuccessListener(aVoid -> {

                                    FirebaseDatabase.getInstance()
                                            .getReference("users")
                                            .child(uid)
                                            .removeValue()
                                            .addOnSuccessListener(unused -> {

                                                FirebaseDatabase.getInstance()
                                                        .getReference("usernames")
                                                        .orderByChild("uid")
                                                        .equalTo(uid)
                                                        .get()
                                                        .addOnSuccessListener(usernameSnapshot -> {

                                                            for (DataSnapshot snapshot :
                                                                    usernameSnapshot.getChildren()) {

                                                                snapshot.getRef().removeValue();
                                                            }

                                                            user.delete()
                                                                    .addOnSuccessListener(unused2 -> {

                                                                        Toast.makeText(
                                                                                requireContext(),
                                                                                "Account deleted successfully",
                                                                                Toast.LENGTH_SHORT
                                                                        ).show();

                                                                        Intent intent =
                                                                                new Intent(
                                                                                       requireContext(),
                                                                                        LoginActivity.class
                                                                                );

                                                                        intent.addFlags(
                                                                                Intent.FLAG_ACTIVITY_NEW_TASK |
                                                                                        Intent.FLAG_ACTIVITY_CLEAR_TASK
                                                                        );

                                                                        startActivity(intent);
                                                                        requireActivity().finish();

                                                                    })
                                                                    .addOnFailureListener(e ->
                                                                            Toast.makeText(
                                                                                    requireContext(),
                                                                                    "Account deletion failed: "
                                                                                            + e.getMessage(),
                                                                                    Toast.LENGTH_LONG
                                                                            ).show()
                                                                    );
                                                        });
                                            });
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(
                                                requireContext(),
                                                "Failed to delete posts: " + e.getMessage(),
                                                Toast.LENGTH_LONG
                                        ).show()
                                );
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(
                                    requireContext(),
                                    "Failed to find user's posts: " + e.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show()
                    );
        }
    }