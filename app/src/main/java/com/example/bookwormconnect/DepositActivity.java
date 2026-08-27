package com.example.bookwormconnect;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DepositActivity extends AppCompatActivity {

    TextView txtBook;
    TextView txtDeposit;
    TextView txtBank;
    TextView txtAccountTitle;
    TextView txtAccountNumber;
    TextView txtIBAN;

    EditText etReference;
    Button btnSubmit;

    FirebaseFirestore db;

    String requestId;
    String chatId;
    String currentUserId;

    String depositAmount = "";
    String bookId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.deposit);

        txtBook = findViewById(R.id.txtBook);
        txtDeposit = findViewById(R.id.txtDeposit);
        txtBank = findViewById(R.id.txtBank);
        txtAccountTitle = findViewById(R.id.txtAccountTitle);
        txtAccountNumber = findViewById(R.id.txtAccountNumber);
        txtIBAN = findViewById(R.id.txtIBAN);

        etReference = findViewById(R.id.etReference);
        btnSubmit = findViewById(R.id.btnSubmit);

        db = FirebaseFirestore.getInstance();

        currentUserId = FirebaseAuth.getInstance()
                .getCurrentUser()
                .getUid();

        requestId = getIntent().getStringExtra("requestId");
        chatId = getIntent().getStringExtra("chatId");

        if (requestId == null) {
            Toast.makeText(this,
                    "Request information missing",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        loadDepositInformation();

        btnSubmit.setOnClickListener(v -> submitPayment());
    }

    private void loadDepositInformation() {

        db.collection("requests")
                .document(requestId)
                .get()
                .addOnSuccessListener(requestDocument -> {

                    if (!requestDocument.exists()) {
                        Toast.makeText(this,
                                "Request not found",
                                Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    bookId = requestDocument.getString("bookId");

                    if (bookId == null) {
                        Toast.makeText(this,
                                "Book information missing",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    loadBookInformation(bookId);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load request",
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void loadBookInformation(String bookId) {

        db.collection("posts")
                .document(bookId)
                .get()
                .addOnSuccessListener(postDocument -> {

                    if (!postDocument.exists()) {
                        Toast.makeText(this,
                                "Book post not found",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String description =
                            postDocument.getString("description");

                    depositAmount =
                            postDocument.getString("deposit");

                    if (description != null) {
                        txtBook.setText(description);
                    }

                    if (depositAmount != null) {
                        txtDeposit.setText("Deposit Amount: Rs. "
                                + depositAmount);
                    }
                    txtBank.setText("Bank: Meezan Bank");
                    txtAccountTitle.setText("Account Title: Mahnoor");
                    txtAccountNumber.setText("Account Number: 00300115714266");
                    txtIBAN.setText("IBAN: PK73MEZN0000300115714266");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to load deposit",
                                Toast.LENGTH_SHORT).show()
                );
    }

    private void submitPayment() {

        String reference =
                etReference.getText().toString().trim();

        if (reference.isEmpty()) {
            etReference.setError(
                    "Enter transaction/reference number");
            return;
        }

        if (depositAmount.isEmpty()) {
            Toast.makeText(this,
                    "Deposit amount not available",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> deposit = new HashMap<>();

        deposit.put("requestId", requestId);
        deposit.put("chatId", chatId);
        deposit.put("borrowerId", currentUserId);
        deposit.put("bookId", bookId);
        deposit.put("amount", depositAmount);
        deposit.put("paymentMethod", "Bank Transfer");
        deposit.put("transactionReference", reference);
        deposit.put("status", "pending");
        deposit.put("submittedAt",
                com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("deposits")
                .add(deposit)
                .addOnSuccessListener(documentReference -> {

                    Toast.makeText(this,
                            "Payment submitted for verification",
                            Toast.LENGTH_LONG).show();

                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                 e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }
}