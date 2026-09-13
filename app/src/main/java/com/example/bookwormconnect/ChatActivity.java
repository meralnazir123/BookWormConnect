package com.example.bookwormconnect;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ChatActivity extends AppCompatActivity {
    TextView txtAddress;
    private RecyclerView chatRecycler;
    private LinearLayout optionsLayout;
    private TextView ChatUsername;
    ImageButton btnMore;
    private ImageView imgChatProfile;
    private Button btnAvailable;
    private Button no;
    Button hi;
    private Button btnMeet;
    private Button btnDeposit;
    Button btnGift;
    Button btnbookreturn;
    Button btnbookReceived;
    private String chatId;
    private String requestId;
    private String currentUserId;
    private TextView txtDepositStatus;
    private ArrayList<ChatMessage> messages;
    private ChatAdapter adapter;
    private ActivityResultLauncher<String> imagePicker;
    private String ownerStartDate = "";
    private String ownerEndDate = "";
    private String ownerStartTime = "";
    private String ownerEndTime = "";
    private String selectedPickupDate = "";
    private String selectedPickupTime = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        View root = findViewById(R.id.optionsLayout);
        View messageOptions = findViewById(R.id.messageOptions);

        ViewCompat.setOnApplyWindowInsetsListener(root, (v, insets) -> {
            androidx.core.graphics.Insets systemBars =
                    insets.getInsets(WindowInsetsCompat.Type.systemBars());

            root.setPadding(
                    root.getPaddingLeft(),
                    systemBars.top,
                    root.getPaddingRight(),
                    root.getPaddingBottom()
            );

            messageOptions.setPadding(
                    messageOptions.getPaddingLeft(),
                    messageOptions.getPaddingTop(),
                    messageOptions.getPaddingRight(),
                    systemBars.bottom
            );

            return insets;
        });

        txtAddress = findViewById(R.id.txtAddress);
        btnMore = findViewById(R.id.btnMore);
        chatRecycler = findViewById(R.id.chatRecycler);
        ChatUsername = findViewById(R.id.chatUsername);
        imgChatProfile = findViewById(R.id.imgChatProfile);
        btnAvailable = findViewById(R.id.btnAvailable);
        btnMeet = findViewById(R.id.btnMeet);
        btnDeposit = findViewById(R.id.btnDeposit);
        no = findViewById(R.id.btnN);
        hi = findViewById(R.id.btnHi);
        btnGift = findViewById(R.id.btngiftbook);
        btnbookreturn = findViewById(R.id.btnreturnbook);
        btnbookReceived = findViewById(R.id.btnreceivedbook);
        optionsLayout = findViewById(R.id.optionsLayout);
        txtDepositStatus = findViewById(R.id.txtDepositStatus);

        String otherUserId = getIntent().getStringExtra("otherUserId");
        chatId = getIntent().getStringExtra("chatId");
        requestId = getIntent().getStringExtra("requestId");
        currentUserId = FirebaseAuth.getInstance().getUid();

        imagePicker = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        uploadReceipt(uri);
                    }
                }
        );

        if (otherUserId != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(otherUserId)
                    .get()
                    .addOnSuccessListener(document -> {
                        if (!document.exists()) {
                            return;
                        }

                        String username = document.getString("username");

                        if (username != null) {
                            ChatUsername.setText(username);
                        }

                        String imageUrl = document.getString("profileImageUrl");

                        Glide.with(ChatActivity.this)
                                .load(imageUrl)
                                .placeholder(R.drawable.user)
                                .circleCrop()
                                .into(imgChatProfile);
                    });
        }

        messages = new ArrayList<>();
        adapter = new ChatAdapter(this, messages);

        chatRecycler.setLayoutManager(new LinearLayoutManager(this));
        chatRecycler.setAdapter(adapter);

        loadRequestInformation();
        checkForOverdue();

        btnMore.setOnClickListener(v -> showMoreMenu());

        btnAvailable.setOnClickListener(
                v -> sendMessage(btnAvailable.getText().toString())
        );

        btnMeet.setOnClickListener(
                v -> sendMessage(btnMeet.getText().toString())
        );

        btnDeposit.setOnClickListener(
                v -> sendMessage(btnDeposit.getText().toString())
        );

        checkDepositStatus();
        checkChatEnabled();
        loadMessages();
    }

    private void loadRequestInformation() {
        if (requestId == null || requestId.isEmpty()) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(requestId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        return;
                    }

                    String address = document.getString("address");

                    if (address != null && !address.isEmpty()) {
                        txtAddress.setText("Address: " + address);
                    }

                    String senderId = document.getString("senderId");

                    if (currentUserId != null && currentUserId.equals(senderId)) {
                        btnAvailable.setText("Is this book available?");
                        btnMeet.setText("When can we meet?");
                        btnDeposit.setText("I have sent the deposit.");
                        hi.setText("Hi");
                        btnbookreturn.setText("I want to return this book.");
                    } else {
                        btnAvailable.setText("Yes, it is available.");
                        btnMeet.setText("We can meet tomorrow.");
                        btnDeposit.setText("I have received the deposit.");
                        no.setText("No");
                        btnGift.setText("I want to gift this book.");
                        btnbookReceived.setText("I have received the book.");
                    }

                    ownerStartDate = getStringValue(document, "availabilityStartDate");
                    ownerEndDate = getStringValue(document, "availabilityEndDate");
                    ownerStartTime = getStringValue(document, "availabilityStartTime");
                    ownerEndTime = getStringValue(document, "availabilityEndTime");
                    selectedPickupDate = getStringValue(document, "requestedPickupDate");
                    selectedPickupTime = getStringValue(document, "requestedPickupTime");
                });
    }

    private String getStringValue(DocumentSnapshot document, String field) {
        String value = document.getString(field);
        return value == null ? "" : value;
    }

    private void checkForOverdue() {
        if (requestId == null || requestId.isEmpty()) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(requestId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        return;
                    }

                    String status = document.getString("status");
                    String returnDate = document.getString("returnDate");

                    if (!"borrowed".equals(status)
                            || returnDate == null
                            || returnDate.isEmpty()) {
                        return;
                    }

                    if (isReturnDatePassed(returnDate)) {
                        FirebaseFirestore.getInstance()
                                .collection("requests")
                                .document(requestId)
                                .update("status", "overdue");
                    }
                });
    }

    private boolean isReturnDatePassed(String returnDate) {
        Calendar returnCalendar = parseDateToCalendar(returnDate);

        if (returnCalendar == null) {
            return false;
        }

        Calendar today = Calendar.getInstance();
        clearTime(today);

        return today.after(returnCalendar);
    }

    private void showMoreMenu() {
        if (requestId == null || requestId.isEmpty()) {
            showBasicMoreMenu(false);
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(requestId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        return;
                    }

                    String senderId = document.getString("senderId");
                    String receiverId = document.getString("receiverId");
                    String status = document.getString("status");
                    String returnDate = document.getString("returnDate");

                    boolean isOwner = currentUserId != null
                            && currentUserId.equals(receiverId);

                    boolean isRequester = currentUserId != null
                            && currentUserId.equals(senderId);

                    if ("borrowed".equals(status)
                            && returnDate != null
                            && !returnDate.isEmpty()
                            && isReturnDatePassed(returnDate)) {

                        status = "overdue";

                        FirebaseFirestore.getInstance()
                                .collection("requests")
                                .document(requestId)
                                .update("status", "overdue");
                    }

                    PopupMenu popupMenu =
                            new PopupMenu(ChatActivity.this, btnMore);

                    popupMenu.getMenu().add("Pay Deposit");
                    popupMenu.getMenu().add("Send Receipt");
                    popupMenu.getMenu().add("Share Email");
                    popupMenu.getMenu().add("Pickup Address");
                    popupMenu.getMenu().add("View Availability");


                    if (isOwner) {
                        popupMenu.getMenu().add("Set Availability");
                        popupMenu.getMenu().add("Set Return Date");

                        if ("borrowed".equals(status)
                                || "overdue".equals(status)) {

                            popupMenu.getMenu().add("Request Return");
                            popupMenu.getMenu().add("Mark as Returned");
                        }
                    }

                    if (isRequester
                            && ("borrowed".equals(status)
                            || "overdue".equals(status))) {

                        popupMenu.getMenu().add("I Have Returned the Book");
                        popupMenu.getMenu().add("Select Pickup Date/Time");
                    }

                    popupMenu.setOnMenuItemClickListener(item -> {
                        String selected = item.getTitle().toString();

                        if (selected.equals("Pay Deposit")) {
                            Intent intent = new Intent(
                                    ChatActivity.this,
                                    DepositActivity.class
                            );

                            intent.putExtra("chatId", chatId);
                            intent.putExtra("requestId", requestId);

                            startActivity(intent);
                            return true;
                        }

                        if (selected.equals("Send Receipt")) {
                            imagePicker.launch("image/*");
                            return true;
                        }

                        if (selected.equals("Share Email")) {
                            showShareEmailDialog();
                            return true;
                        }

                        if (selected.equals("Pickup Address")) {
                            showPickupAddressDialog();
                            return true;
                        }

                        if (selected.equals("View Availability")) {
                            showAvailability();
                            return true;
                        }

                        if (selected.equals("Set Availability")) {
                            showSetAvailabilityDialog();
                            return true;
                        }

                        if (selected.equals("Select Pickup Date/Time")) {
                            showSelectPickupDialog();
                            return true;
                        }

                        if (selected.equals("Set Return Date")) {
                            showReturnDateDialog();
                            return true;
                        }

                        if (selected.equals("Request Return")) {
                            sendReturnRequest();
                            return true;
                        }

                        if (selected.equals("Mark as Returned")) {
                            confirmBookReturned();
                            return true;
                        }

                        if (selected.equals("I Have Returned the Book")) {
                            sendReturnConfirmationMessage();
                            return true;
                        }

                        return false;
                    });

                    popupMenu.show();
                });
    }

    private void showBasicMoreMenu(boolean ignored) {
        PopupMenu popupMenu =
                new PopupMenu(ChatActivity.this, btnMore);

        popupMenu.getMenu().add("Pay Deposit");
        popupMenu.getMenu().add("Send Receipt");
        popupMenu.getMenu().add("Share Email");
        popupMenu.getMenu().add("Pickup Address");


        popupMenu.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString();

            if (selected.equals("Pay Deposit")) {
                Intent intent = new Intent(
                        ChatActivity.this,
                        DepositActivity.class
                );

                intent.putExtra("chatId", chatId);
                intent.putExtra("requestId", requestId);

                startActivity(intent);
                return true;
            }

            if (selected.equals("Send Receipt")) {
                imagePicker.launch("image/*");
                return true;
            }

            if (selected.equals("Share Email")) {
                showShareEmailDialog();
                return true;
            }

            if (selected.equals("Pickup Address")) {
                showPickupAddressDialog();
                return true;
            }

            return false;
        });

        popupMenu.show();
    }

    private void sendReturnRequest() {
        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(requestId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        return;
                    }

                    String status = document.getString("status");
                    String returnDate = document.getString("returnDate");

                    String message;

                    if ("overdue".equals(status)) {
                        if (returnDate != null && !returnDate.isEmpty()) {
                            message = "Overdue Return Reminder\nThe book was due to be returned on "
                                    + returnDate
                                    + ".\nPlease return the book as soon as possible.";
                        } else {
                            message = "Overdue Return Reminder\nThe book is overdue. Please return it as soon as possible.";
                        }
                    } else {
                        if (returnDate != null && !returnDate.isEmpty()) {
                            message = "Return Reminder\nPlease return the book by "
                                    + returnDate
                                    + ".";
                        } else {
                            message = "Return Reminder\nPlease arrange the return of the book.";
                        }
                    }

                    sendMessage(message);

                    Toast.makeText(
                            this,
                            "Return request sent",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to send return request",
                                Toast.LENGTH_SHORT
                        ).show());
    }

    private void confirmBookReturned() {
        new AlertDialog.Builder(this)
                .setTitle("Confirm Return")
                .setMessage("Have you received the book back from the borrower?")
                .setNegativeButton("Cancel", null)
                .setPositiveButton(
                        "Confirm Return",
                        (dialog, which) -> markBookAsReturned()
                )
                .show();
    }

    private void markBookAsReturned() {
        String returnedDate =
                new SimpleDateFormat(
                        "d/M/yyyy",
                        Locale.getDefault()
                ).format(new Date());

        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(requestId)
                .update(
                        "status", "returned",
                        "returnedDate", returnedDate
                )
                .addOnSuccessListener(unused -> {
                    sendMessage(
                            "Book Returned\nThe owner has confirmed that the book has been returned."
                    );

                    Toast.makeText(
                            this,
                            "Book marked as returned",
                            Toast.LENGTH_SHORT
                    ).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to mark book as returned",
                                Toast.LENGTH_SHORT
                        ).show());
    }

    private void sendReturnConfirmationMessage() {
        sendMessage(
                "Return Update\nI have returned the book. Please confirm that you have received it."
        );

        Toast.makeText(
                this,
                "Return message sent",
                Toast.LENGTH_SHORT
        ).show();
    }

    private void showShareEmailDialog() {
        View dialogView = LayoutInflater.from(ChatActivity.this)
                .inflate(R.layout.dialog_share_email, null);

        EditText editEmail =
                dialogView.findViewById(R.id.editEmail);

        Button btnSendEmail =
                dialogView.findViewById(R.id.btnSendEmail);

        AlertDialog dialog = new AlertDialog.Builder(ChatActivity.this)
                .setTitle("Share Email")
                .setView(dialogView)
                .create();

        btnSendEmail.setOnClickListener(view -> {
            String email =
                    editEmail.getText().toString().trim();

            if (email.isEmpty()) {
                editEmail.setError("Please enter your email");
                return;
            }

            if (!android.util.Patterns.EMAIL_ADDRESS
                    .matcher(email)
                    .matches()) {

                editEmail.setError("Enter a valid email");
                return;
            }

            sendMessage("My email is: " + email);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showSetAvailabilityDialog() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        int padding =
                (int) (20 * getResources()
                        .getDisplayMetrics().density);

        layout.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        Button startDateButton = new Button(this);
        Button endDateButton = new Button(this);
        Button startTimeButton = new Button(this);
        Button endTimeButton = new Button(this);

        startDateButton.setText("Select Start Date");
        endDateButton.setText("Select End Date");
        startTimeButton.setText("Select Start Time");
        endTimeButton.setText("Select End Time");

        layout.addView(startDateButton);
        layout.addView(endDateButton);
        layout.addView(startTimeButton);
        layout.addView(endTimeButton);

        final String[] startDate = {""};
        final String[] endDate = {""};
        final String[] startTime = {""};
        final String[] endTime = {""};

        final int[] startDay = {0};
        final int[] startMonth = {0};
        final int[] startYear = {0};
        final int[] endDay = {0};
        final int[] endMonth = {0};
        final int[] endYear = {0};

        final boolean[] startDateSelected = {false};
        final boolean[] endDateSelected = {false};

        startDateButton.setOnClickListener(v -> {
            Calendar today = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        startDay[0] = day;
                        startMonth[0] = month;
                        startYear[0] = year;

                        startDate[0] =
                                day + "/" + (month + 1) + "/" + year;

                        startDateSelected[0] = true;

                        startTime[0] = "";
                        endTime[0] = "";

                        startTimeButton.setText("Select Start Time");
                        endTimeButton.setText("Select End Time");

                        if (endDateSelected[0]
                                && isDateBefore(
                                endYear[0],
                                endMonth[0],
                                endDay[0],
                                startYear[0],
                                startMonth[0],
                                startDay[0])) {

                            endDate[0] = "";
                            endDateSelected[0] = false;
                            endDateButton.setText("Select End Date");
                        }

                        startDateButton.setText(
                                "Start Date: " + startDate[0]
                        );
                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
            );

            dialog.getDatePicker()
                    .setMinDate(getTodayStartMillis());

            dialog.show();
        });

        endDateButton.setOnClickListener(v -> {
            if (!startDateSelected[0]) {
                Toast.makeText(
                        this,
                        "Please select start date first",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Calendar today = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        if (isDateBefore(
                                year,
                                month,
                                day,
                                startYear[0],
                                startMonth[0],
                                startDay[0])) {

                            Toast.makeText(
                                    this,
                                    "End date cannot be before start date",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        endDay[0] = day;
                        endMonth[0] = month;
                        endYear[0] = year;

                        endDate[0] =
                                day + "/" + (month + 1) + "/" + year;

                        endDateSelected[0] = true;

                        endTime[0] = "";

                        endTimeButton.setText("Select End Time");
                        endDateButton.setText(
                                "End Date: " + endDate[0]
                        );
                    },
                    today.get(Calendar.YEAR),
                    today.get(Calendar.MONTH),
                    today.get(Calendar.DAY_OF_MONTH)
            );

            dialog.getDatePicker()
                    .setMinDate(getTodayStartMillis());

            dialog.show();
        });

        startTimeButton.setOnClickListener(v -> {
            if (!startDateSelected[0]) {
                Toast.makeText(
                        this,
                        "Please select start date first",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();

            TimePickerDialog dialog = new TimePickerDialog(
                    this,
                    (view, hour, minute) -> {
                        if (isToday(
                                startYear[0],
                                startMonth[0],
                                startDay[0])) {

                            Calendar selected = Calendar.getInstance();

                            selected.set(
                                    Calendar.HOUR_OF_DAY,
                                    hour
                            );

                            selected.set(
                                    Calendar.MINUTE,
                                    minute
                            );

                            selected.set(
                                    Calendar.SECOND,
                                    0
                            );

                            selected.set(
                                    Calendar.MILLISECOND,
                                    0
                            );

                            if (selected.before(
                                    Calendar.getInstance())) {

                                Toast.makeText(
                                        this,
                                        "Start time cannot be in the past",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }
                        }

                        startTime[0] =
                                convertTo12HourTime(hour, minute);

                        startTimeButton.setText(
                                "Start Time: " + startTime[0]
                        );

                        if (endDateSelected[0]
                                && isSameDate(
                                startYear[0],
                                startMonth[0],
                                startDay[0],
                                endYear[0],
                                endMonth[0],
                                endDay[0])
                                && !endTime[0].isEmpty()) {

                            int startMinutes =
                                    convertTimeToMinutes(
                                            startTime[0]
                                    );

                            int endMinutes =
                                    convertTimeToMinutes(
                                            endTime[0]
                                    );

                            if (endMinutes < startMinutes) {
                                endTime[0] = "";
                                endTimeButton.setText(
                                        "Select End Time"
                                );
                            }
                        }
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
            );

            dialog.show();
        });

        endTimeButton.setOnClickListener(v -> {
            if (!endDateSelected[0]) {
                Toast.makeText(
                        this,
                        "Please select end date first",
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            Calendar calendar = Calendar.getInstance();

            TimePickerDialog dialog = new TimePickerDialog(
                    this,
                    (view, hour, minute) -> {
                        if (isToday(
                                endYear[0],
                                endMonth[0],
                                endDay[0])) {

                            Calendar selected = Calendar.getInstance();

                            selected.set(
                                    Calendar.HOUR_OF_DAY,
                                    hour
                            );

                            selected.set(
                                    Calendar.MINUTE,
                                    minute
                            );

                            selected.set(
                                    Calendar.SECOND,
                                    0
                            );

                            selected.set(
                                    Calendar.MILLISECOND,
                                    0
                            );

                            if (selected.before(
                                    Calendar.getInstance())) {

                                Toast.makeText(
                                        this,
                                        "End time cannot be in the past",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }
                        }

                        String selectedTime =
                                convertTo12HourTime(
                                        hour,
                                        minute
                                );

                        if (startDateSelected[0]
                                && isSameDate(
                                startYear[0],
                                startMonth[0],
                                startDay[0],
                                endYear[0],
                                endMonth[0],
                                endDay[0])
                                && !startTime[0].isEmpty()) {

                            int startMinutes =
                                    convertTimeToMinutes(
                                            startTime[0]
                                    );

                            int endMinutes =
                                    convertTimeToMinutes(
                                            selectedTime
                                    );

                            if (endMinutes < startMinutes) {
                                Toast.makeText(
                                        this,
                                        "End time cannot be before start time",
                                        Toast.LENGTH_SHORT
                                ).show();
                                return;
                            }
                        }

                        endTime[0] = selectedTime;

                        endTimeButton.setText(
                                "End Time: " + endTime[0]
                        );
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
            );

            dialog.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Set Book Availability")
                .setView(layout)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(
                        AlertDialog.BUTTON_POSITIVE
                ).setOnClickListener(v -> {

                    if (startDate[0].isEmpty()
                            || endDate[0].isEmpty()
                            || startTime[0].isEmpty()
                            || endTime[0].isEmpty()) {

                        Toast.makeText(
                                this,
                                "Please select complete availability",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    FirebaseFirestore.getInstance()
                            .collection("requests")
                            .document(requestId)
                            .update(
                                    "availabilityStartDate",
                                    startDate[0],
                                    "availabilityEndDate",
                                    endDate[0],
                                    "availabilityStartTime",
                                    startTime[0],
                                    "availabilityEndTime",
                                    endTime[0],
                                    "requestedPickupDate",
                                    "",
                                    "requestedPickupTime",
                                    "",
                                    "returnDate",
                                    ""
                            )
                            .addOnSuccessListener(unused -> {
                                ownerStartDate = startDate[0];
                                ownerEndDate = endDate[0];
                                ownerStartTime = startTime[0];
                                ownerEndTime = endTime[0];

                                selectedPickupDate = "";
                                selectedPickupTime = "";

                                String availabilityMessage =
                                        "Book Availability\nAvailable from: "
                                                + startDate[0]
                                                + " to "
                                                + endDate[0]
                                                + "\nAvailable time: "
                                                + formatTimeForDisplay(
                                                startTime[0]
                                        )
                                                + " - "
                                                + formatTimeForDisplay(
                                                endTime[0]
                                        );

                                sendMessage(availabilityMessage);

                                Toast.makeText(
                                        this,
                                        "Availability saved",
                                        Toast.LENGTH_SHORT
                                ).show();

                                dialog.dismiss();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Failed to save availability",
                                            Toast.LENGTH_SHORT
                                    ).show());
                }));

        dialog.show();
    }

    private void showAvailability() {
        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(requestId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        return;
                    }

                    String startDate =
                            document.getString("availabilityStartDate");

                    String endDate =
                            document.getString("availabilityEndDate");

                    String startTime =
                            document.getString("availabilityStartTime");

                    String endTime =
                            document.getString("availabilityEndTime");

                    if (startDate == null
                            || endDate == null
                            || startTime == null
                            || endTime == null
                            || startDate.isEmpty()
                            || endDate.isEmpty()
                            || startTime.isEmpty()
                            || endTime.isEmpty()) {

                        new AlertDialog.Builder(this)
                                .setTitle("Book Availability")
                                .setMessage(
                                        "The owner has not set book availability yet."
                                )
                                .setPositiveButton("OK", null)
                                .show();

                        return;
                    }

                    String requestedDate =
                            document.getString("requestedPickupDate");

                    String requestedTime =
                            document.getString("requestedPickupTime");

                    String returnDate =
                            document.getString("returnDate");

                    String message =
                            "Available Dates:\n"
                                    + startDate
                                    + " - "
                                    + endDate
                                    + "\n\nAvailable Time:\n"
                                    + formatTimeForDisplay(startTime)
                                    + " - "
                                    + formatTimeForDisplay(endTime);

                    if (requestedDate != null
                            && !requestedDate.isEmpty()
                            && requestedTime != null
                            && !requestedTime.isEmpty()) {

                        message +=
                                "\n\nSelected Pickup:\n"
                                        + requestedDate
                                        + " at "
                                        + requestedTime;
                    }

                    if (returnDate != null
                            && !returnDate.isEmpty()) {

                        message +=
                                "\n\nReturn Date:\n"
                                        + returnDate;
                    }

                    new AlertDialog.Builder(this)
                            .setTitle("Book Availability")
                            .setMessage(message)
                            .setPositiveButton("OK", null)
                            .show();
                });
    }

    private void showSelectPickupDialog() {
        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(requestId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        return;
                    }

                    String startDate =
                            document.getString("availabilityStartDate");

                    String endDate =
                            document.getString("availabilityEndDate");

                    String startTime =
                            document.getString("availabilityStartTime");

                    String endTime =
                            document.getString("availabilityEndTime");

                    if (startDate == null
                            || endDate == null
                            || startTime == null
                            || endTime == null
                            || startDate.isEmpty()
                            || endDate.isEmpty()
                            || startTime.isEmpty()
                            || endTime.isEmpty()) {

                        new AlertDialog.Builder(this)
                                .setTitle("Pickup Time")
                                .setMessage(
                                        "The owner has not set availability yet."
                                )
                                .setPositiveButton("OK", null)
                                .show();

                        return;
                    }

                    showPickupSelection(
                            startDate,
                            endDate,
                            startTime,
                            endTime
                    );
                });
    }

    private void showPickupSelection(
            String startDate,
            String endDate,
            String startTime,
            String endTime) {

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);

        int padding =
                (int) (20 * getResources()
                        .getDisplayMetrics().density);

        layout.setPadding(
                padding,
                padding,
                padding,
                padding
        );

        Button dateButton = new Button(this);
        Button timeButton = new Button(this);

        dateButton.setText("Select Pickup Date");
        timeButton.setText("Select Pickup Time");

        layout.addView(dateButton);
        layout.addView(timeButton);

        final String[] selectedDate = {""};
        final String[] selectedTime = {""};

        dateButton.setOnClickListener(v -> {
            Calendar calendar = Calendar.getInstance();

            DatePickerDialog dialog = new DatePickerDialog(
                    this,
                    (view, year, month, day) -> {
                        String date =
                                day + "/" + (month + 1) + "/" + year;

                        if (!isDateWithinAvailability(
                                date,
                                startDate,
                                endDate)) {

                            Toast.makeText(
                                    this,
                                    "Please select a date within the owner's availability",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        selectedDate[0] = date;
                        selectedTime[0] = "";

                        dateButton.setText(
                                "Pickup Date: " + date
                        );

                        timeButton.setText(
                                "Select Pickup Time"
                        );
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
            );

            dialog.getDatePicker()
                    .setMinDate(getDateMillis(startDate));

            dialog.getDatePicker()
                    .setMaxDate(getDateMillis(endDate));

            dialog.show();
        });

        timeButton.setOnClickListener(v -> {
            if (selectedDate[0].isEmpty()) {
                Toast.makeText(
                        this,
                        "Please select pickup date first",
                        Toast.LENGTH_SHORT
                ).show();

                return;
            }

            Calendar calendar = Calendar.getInstance();

            TimePickerDialog dialog = new TimePickerDialog(
                    this,
                    (view, hour, minute) -> {
                        String time =
                                convertTo12HourTime(
                                        hour,
                                        minute
                                );

                        if (!isRequestedTimeAllowed(
                                selectedDate[0],
                                time,
                                startDate,
                                endDate,
                                startTime,
                                endTime)) {

                            Toast.makeText(
                                    this,
                                    "Please select a time within the owner's availability",
                                    Toast.LENGTH_LONG
                            ).show();

                            return;
                        }

                        selectedTime[0] = time;

                        timeButton.setText(
                                "Pickup Time: " + time
                        );
                    },
                    calendar.get(Calendar.HOUR_OF_DAY),
                    calendar.get(Calendar.MINUTE),
                    false
            );

            dialog.show();
        });

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Select Pickup Date/Time")
                .setView(layout)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(
                        AlertDialog.BUTTON_POSITIVE
                ).setOnClickListener(v -> {

                    if (selectedDate[0].isEmpty()) {
                        Toast.makeText(
                                this,
                                "Please select pickup date",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    if (selectedTime[0].isEmpty()) {
                        Toast.makeText(
                                this,
                                "Please select pickup time",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    if (!isRequestedTimeAllowed(
                            selectedDate[0],
                            selectedTime[0],
                            startDate,
                            endDate,
                            startTime,
                            endTime)) {

                        Toast.makeText(
                                this,
                                "Selected pickup time is outside the owner's availability",
                                Toast.LENGTH_LONG
                        ).show();

                        return;
                    }

                    FirebaseFirestore.getInstance()
                            .collection("requests")
                            .document(requestId)
                            .update(
                                    "requestedPickupDate",
                                    selectedDate[0],
                                    "requestedPickupTime",
                                    selectedTime[0],
                                    "returnDate",
                                    ""
                            )
                            .addOnSuccessListener(unused -> {
                                selectedPickupDate =
                                        selectedDate[0];

                                selectedPickupTime =
                                        selectedTime[0];

                                String pickupMessage =
                                        "Pickup Date/Time\nPickup date: "
                                                + selectedDate[0]
                                                + "\nPickup time: "
                                                + selectedTime[0];

                                sendMessage(pickupMessage);

                                Toast.makeText(
                                        this,
                                        "Pickup date and time saved",
                                        Toast.LENGTH_SHORT
                                ).show();

                                dialog.dismiss();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Failed to save pickup time",
                                            Toast.LENGTH_SHORT
                                    ).show());
                }));

        dialog.show();
    }

    private void showReturnDateDialog() {
        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(requestId)
                .get()
                .addOnSuccessListener(requestDocument -> {
                    if (!requestDocument.exists()) {
                        return;
                    }

                    String pickupDate =
                            requestDocument.getString("requestedPickupDate");

                    String pickupTime =
                            requestDocument.getString("requestedPickupTime");

                    String bookId =
                            requestDocument.getString("bookId");

                    if (pickupDate == null
                            || pickupDate.isEmpty()) {

                        new AlertDialog.Builder(this)
                                .setTitle("Set Return Date")
                                .setMessage(
                                        "The requester must select a pickup date first."
                                )
                                .setPositiveButton("OK", null)
                                .show();

                        return;
                    }

                    if (bookId == null || bookId.isEmpty()) {
                        Toast.makeText(
                                this,
                                "Book information not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    FirebaseFirestore.getInstance()
                            .collection("posts")
                            .document(bookId)
                            .get()
                            .addOnSuccessListener(postDocument -> {
                                if (!postDocument.exists()) {
                                    Toast.makeText(
                                            this,
                                            "Book post not found",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                String duration =
                                        postDocument.getString("duration");

                                if (duration == null
                                        || duration.trim().isEmpty()) {

                                    Toast.makeText(
                                            this,
                                            "Book duration is not available",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                Calendar maxReturnDate =
                                        calculateReturnDate(
                                                pickupDate,
                                                duration
                                        );

                                if (maxReturnDate == null) {
                                    Toast.makeText(
                                            this,
                                            "Invalid book duration",
                                            Toast.LENGTH_SHORT
                                    ).show();

                                    return;
                                }

                                showReturnDatePicker(
                                        pickupDate,
                                        pickupTime,
                                        duration,
                                        maxReturnDate
                                );
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Failed to load book duration",
                                            Toast.LENGTH_SHORT
                                    ).show());
                });
    }

    private void showReturnDatePicker(
            String pickupDate,
            String pickupTime,
            String duration,
            Calendar maxReturnDate) {

        Calendar pickupCalendar =
                parseDateToCalendar(pickupDate);

        if (pickupCalendar == null) {
            Toast.makeText(
                    this,
                    "Invalid pickup date",
                    Toast.LENGTH_SHORT
            ).show();

            return;
        }

        DatePickerDialog dialog = new DatePickerDialog(
                this,
                (view, year, month, day) -> {
                    String returnDate =
                            day + "/" + (month + 1) + "/" + year;

                    Calendar selected = Calendar.getInstance();

                    selected.set(
                            year,
                            month,
                            day,
                            0,
                            0,
                            0
                    );

                    selected.set(
                            Calendar.MILLISECOND,
                            0
                    );

                    Calendar minimum =
                            (Calendar) pickupCalendar.clone();

                    clearTime(minimum);

                    Calendar maximum =
                            (Calendar) maxReturnDate.clone();

                    clearTime(maximum);

                    if (selected.before(minimum)) {
                        Toast.makeText(
                                this,
                                "Return date cannot be before pickup date",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    if (selected.after(maximum)) {
                        Toast.makeText(
                                this,
                                "Return date exceeds the allowed lending duration",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    FirebaseFirestore.getInstance()
                            .collection("requests")
                            .document(requestId)
                            .update("returnDate", returnDate)
                            .addOnSuccessListener(unused -> {
                                String message =
                                        "Return Date\nPlease return the book by: "
                                                + returnDate;

                                if (pickupTime != null
                                        && !pickupTime.isEmpty()) {

                                    message +=
                                            "\nPickup was scheduled for: "
                                                    + pickupDate
                                                    + " at "
                                                    + pickupTime;
                                }

                                sendMessage(message);

                                Toast.makeText(
                                        this,
                                        "Return date saved",
                                        Toast.LENGTH_SHORT
                                ).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Failed to save return date",
                                            Toast.LENGTH_SHORT
                                    ).show());

                },
                pickupCalendar.get(Calendar.YEAR),
                pickupCalendar.get(Calendar.MONTH),
                pickupCalendar.get(Calendar.DAY_OF_MONTH)
        );

        dialog.getDatePicker()
                .setMinDate(getDateMillis(pickupDate));

        dialog.getDatePicker()
                .setMaxDate(maxReturnDate.getTimeInMillis());

        dialog.show();

        Toast.makeText(
                this,
                "Duration: " + duration,
                Toast.LENGTH_LONG
        ).show();
    }

    private Calendar calculateReturnDate(
            String pickupDate,
            String duration) {

        Calendar calendar =
                parseDateToCalendar(pickupDate);

        if (calendar == null) {
            return null;
        }

        String normalized =
                duration.trim().toLowerCase(Locale.ENGLISH);

        String[] parts =
                normalized.split("\\s+");

        if (parts.length < 2) {
            return null;
        }

        int amount;

        try {
            amount =
                    Integer.parseInt(parts[0]);
        } catch (NumberFormatException e) {
            return null;
        }

        if (amount <= 0) {
            return null;
        }

        String unit = parts[1];

        if (unit.startsWith("day")) {
            calendar.add(
                    Calendar.DAY_OF_MONTH,
                    amount
            );
        } else if (unit.startsWith("week")) {
            calendar.add(
                    Calendar.DAY_OF_MONTH,
                    amount * 7
            );
        } else if (unit.startsWith("month")) {
            calendar.add(
                    Calendar.MONTH,
                    amount
            );
        } else if (unit.startsWith("year")) {
            calendar.add(
                    Calendar.YEAR,
                    amount
            );
        } else {
            return null;
        }
        clearTime(calendar);
        return calendar;
    }
    private Calendar parseDateToCalendar(String date) {
        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "d/M/yyyy",
                            Locale.getDefault()
                    );
            sdf.setLenient(false);
            Date parsed = sdf.parse(date);
            if (parsed == null) {
                return null;
            }
            Calendar calendar =
                    Calendar.getInstance();
            calendar.setTime(parsed);
            clearTime(calendar);
            return calendar;
        } catch (Exception e) {
            return null;
        }
    }
    private void showPickupAddressDialog() {
        FirebaseFirestore db =
                FirebaseFirestore.getInstance();
        db.collection("requests")
                .document(requestId)
                .get()
                .addOnSuccessListener(document -> {
                    if (!document.exists()) {
                        Toast.makeText(
                                this,
                                "Request not found",
                                Toast.LENGTH_SHORT
                        ).show();

                        return;
                    }

                    String senderId =
                            document.getString("senderId");

                    String receiverId =
                            document.getString("receiverId");

                    String pickupAddress =
                            document.getString("pickupAddress");

                    if (currentUserId != null
                            && currentUserId.equals(receiverId)) {

                        showOwnerPickupAddressDialog(
                                pickupAddress
                        );

                    } else if (currentUserId != null
                            && currentUserId.equals(senderId)) {

                        showRequesterPickupAddressDialog(
                                pickupAddress
                        );
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to load pickup address",
                                Toast.LENGTH_SHORT
                        ).show());
    }

    private void showOwnerPickupAddressDialog(
            String pickupAddress) {

        EditText input = new EditText(this);

        input.setHint("Enter pickup address");
        input.setPadding(40, 20, 40, 20);

        if (pickupAddress != null
                && !pickupAddress.isEmpty()) {

            input.setText(pickupAddress);
        }

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Pickup Address")
                .setMessage(
                        "Enter the address where the requester can pick up the book."
                )
                .setView(input)
                .setPositiveButton("Save", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(d ->
                dialog.getButton(
                        AlertDialog.BUTTON_POSITIVE
                ).setOnClickListener(v -> {

                    String address =
                            input.getText().toString().trim();

                    if (address.isEmpty()) {
                        input.setError(
                                "Please enter an address"
                        );

                        return;
                    }

                    FirebaseFirestore.getInstance()
                            .collection("requests")
                            .document(requestId)
                            .update(
                                    "pickupAddress",
                                    address
                            )
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(
                                        this,
                                        "Pickup address saved",
                                        Toast.LENGTH_SHORT
                                ).show();

                                dialog.dismiss();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(
                                            this,
                                            "Failed to save address",
                                            Toast.LENGTH_SHORT
                                    ).show());
                }));

        dialog.show();
    }

    private void showRequesterPickupAddressDialog(
            String pickupAddress) {

        if (pickupAddress == null
                || pickupAddress.isEmpty()) {

            new AlertDialog.Builder(this)
                    .setTitle("Pickup Address")
                    .setMessage(
                            "The owner has not provided a pickup address yet."
                    )
                    .setPositiveButton("OK", null)
                    .show();

            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Owner's Pickup Address")
                .setMessage(pickupAddress)
                .setPositiveButton("OK", null)
                .show();
    }

    private void checkChatEnabled() {
        if (requestId == null) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("requests")
                .document(requestId)
                .addSnapshotListener((doc, error) -> {
                    if (error != null
                            || doc == null
                            || !doc.exists()) {

                        return;
                    }

                    Boolean enabled =
                            doc.getBoolean("chatEnabled");

                    if (enabled != null && enabled) {
                        optionsLayout.setEnabled(true);
                    } else {
                        finish();
                    }
                });
    }

    private void sendMessage(String text) {
        if (chatId == null
                || chatId.isEmpty()
                || currentUserId == null) {

            return;
        }

        String key =
                FirebaseDatabase.getInstance()
                        .getReference()
                        .child("Chats")
                        .child(chatId)
                        .child("messages")
                        .push()
                        .getKey();

        if (key == null) {
            return;
        }

        ChatMessage msg =
                new ChatMessage(
                        currentUserId,
                        text,
                        "",
                        "text",
                        System.currentTimeMillis()
                );

        FirebaseDatabase.getInstance()
                .getReference()
                .child("Chats")
                .child(chatId)
                .child("messages")
                .child(key)
                .setValue(msg);
    }

    private void uploadReceipt(Uri uri) {
        StorageReference ref =
                FirebaseStorage.getInstance()
                        .getReference()
                        .child("receipts")
                        .child(
                                System.currentTimeMillis()
                                        + ".jpg"
                        );

        ref.putFile(uri)
                .continueWithTask(task ->
                        ref.getDownloadUrl()
                )
                .addOnSuccessListener(downloadUri -> {
                    String key =
                            FirebaseDatabase.getInstance()
                                    .getReference()
                                    .child("Chats")
                                    .child(chatId)
                                    .child("messages")
                                    .push()
                                    .getKey();

                    if (key == null) {
                        return;
                    }

                    ChatMessage msg =
                            new ChatMessage(
                                    currentUserId,
                                    "",
                                    downloadUri.toString(),
                                    "image",
                                    System.currentTimeMillis()
                            );

                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child("Chats")
                            .child(chatId)
                            .child("messages")
                            .child(key)
                            .setValue(msg);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(
                                this,
                                "Failed to upload receipt",
                                Toast.LENGTH_SHORT
                        ).show());
    }

    private void checkDepositStatus() {
        if (requestId == null) {
            return;
        }

        FirebaseFirestore.getInstance()
                .collection("deposits")
                .whereEqualTo("requestId", requestId)
                .limit(1)
                .addSnapshotListener((snapshot, error) -> {
                    if (error != null) {
                        return;
                    }

                    if (snapshot == null
                            || snapshot.isEmpty()) {

                        txtDepositStatus.setText(
                                "Deposit: Not Paid"
                        );

                        return;
                    }

                    String status =
                            snapshot.getDocuments()
                                    .get(0)
                                    .getString("status");

                    if ("pending".equals(status)) {
                        txtDepositStatus.setText(
                                "Deposit: Pending Verification"
                        );
                    } else if ("approved".equals(status)) {
                        txtDepositStatus.setText(
                                "Deposit: Verified"
                        );
                    } else if ("rejected".equals(status)) {
                        txtDepositStatus.setText(
                                "Deposit: Rejected"
                        );
                    }
                });
    }

    private void loadMessages() {
        if (chatId == null || chatId.isEmpty()) {
            return;
        }

        FirebaseDatabase.getInstance()
                .getReference()
                .child("Chats")
                .child(chatId)
                .child("messages")
                .addValueEventListener(
                        new ValueEventListener() {
                            @Override
                            public void onDataChange(
                                    @NonNull DataSnapshot snapshot) {

                                messages.clear();

                                for (DataSnapshot ds :
                                        snapshot.getChildren()) {

                                    ChatMessage msg =
                                            ds.getValue(
                                                    ChatMessage.class
                                            );

                                    if (msg != null) {
                                        messages.add(msg);
                                    }
                                }

                                adapter.notifyDataSetChanged();

                                if (!messages.isEmpty()) {
                                    chatRecycler.smoothScrollToPosition(
                                            messages.size() - 1
                                    );
                                }
                            }

                            @Override
                            public void onCancelled(
                                    @NonNull DatabaseError error) {
                            }
                        }
                );
    }

    private boolean isDateWithinAvailability(
            String selectedDate,
            String startDate,
            String endDate) {

        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "d/M/yyyy",
                            Locale.getDefault()
                    );

            sdf.setLenient(false);

            Date selected = sdf.parse(selectedDate);
            Date start = sdf.parse(startDate);
            Date end = sdf.parse(endDate);

            if (selected == null
                    || start == null
                    || end == null) {

                return false;
            }

            return !selected.before(start)
                    && !selected.after(end);

        } catch (ParseException e) {
            return false;
        }
    }

    private boolean isRequestedTimeAllowed(
            String selectedDate,
            String selectedTime,
            String startDate,
            String endDate,
            String startTime,
            String endTime) {

        try {
            SimpleDateFormat dateFormat =
                    new SimpleDateFormat(
                            "d/M/yyyy",
                            Locale.getDefault()
                    );

            dateFormat.setLenient(false);

            Date selected =
                    dateFormat.parse(selectedDate);

            Date start =
                    dateFormat.parse(startDate);

            Date end =
                    dateFormat.parse(endDate);

            if (selected == null
                    || start == null
                    || end == null) {

                return false;
            }

            if (selected.before(start)
                    || selected.after(end)) {

                return false;
            }

            Calendar selectedCalendar =
                    Calendar.getInstance();

            Calendar startCalendar =
                    Calendar.getInstance();

            Calendar endCalendar =
                    Calendar.getInstance();

            selectedCalendar.setTime(selected);
            startCalendar.setTime(start);
            endCalendar.setTime(end);

            clearTime(selectedCalendar);
            clearTime(startCalendar);
            clearTime(endCalendar);

            int selectedMinutes =
                    convertTimeToMinutes(selectedTime);

            int startMinutes =
                    convertTimeToMinutes(startTime);

            int endMinutes =
                    convertTimeToMinutes(endTime);

            if (selectedMinutes == -1
                    || startMinutes == -1
                    || endMinutes == -1) {

                return false;
            }

            boolean sameAsStart =
                    selectedCalendar.equals(startCalendar);

            boolean sameAsEnd =
                    selectedCalendar.equals(endCalendar);

            if (sameAsStart && sameAsEnd) {
                return selectedMinutes >= startMinutes
                        && selectedMinutes <= endMinutes;
            }

            if (sameAsStart) {
                return selectedMinutes >= startMinutes;
            }

            if (sameAsEnd) {
                return selectedMinutes <= endMinutes;
            }

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    private void clearTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    private int convertTimeToMinutes(String time) {
        try {
            time = time.trim();

            SimpleDateFormat format =
                    new SimpleDateFormat(
                            "h:mm a",
                            Locale.ENGLISH
                    );

            format.setLenient(false);

            Date date = format.parse(time);

            if (date == null) {
                return -1;
            }

            Calendar calendar =
                    Calendar.getInstance();

            calendar.setTime(date);

            return calendar.get(Calendar.HOUR_OF_DAY) * 60
                    + calendar.get(Calendar.MINUTE);

        } catch (Exception e) {
            try {
                SimpleDateFormat format =
                        new SimpleDateFormat(
                                "HH:mm",
                                Locale.ENGLISH
                        );

                format.setLenient(false);

                Date date = format.parse(time);

                if (date == null) {
                    return -1;
                }

                Calendar calendar =
                        Calendar.getInstance();

                calendar.setTime(date);

                return calendar.get(Calendar.HOUR_OF_DAY) * 60
                        + calendar.get(Calendar.MINUTE);

            } catch (Exception ignored) {
                return -1;
            }
        }
    }

    private String convertTo12HourTime(
            int hourOfDay,
            int minute) {

        String amPm =
                hourOfDay >= 12 ? "PM" : "AM";

        int hour12 =
                hourOfDay % 12;

        if (hour12 == 0) {
            hour12 = 12;
        }

        return String.format(
                Locale.getDefault(),
                "%d:%02d %s",
                hour12,
                minute,
                amPm
        );
    }

    private String formatTimeForDisplay(String time) {
        if (time == null || time.trim().isEmpty()) {
            return "";
        }

        try {
            SimpleDateFormat input =
                    new SimpleDateFormat(
                            "h:mm a",
                            Locale.ENGLISH
                    );

            input.setLenient(false);

            Date date = input.parse(time);

            if (date != null) {
                return new SimpleDateFormat(
                        "h:mm a",
                        Locale.ENGLISH
                ).format(date);
            }

        } catch (Exception ignored) {
        }

        return time;
    }

    private boolean isToday(
            int year,
            int month,
            int day) {

        Calendar today =
                Calendar.getInstance();

        return year == today.get(Calendar.YEAR)
                && month == today.get(Calendar.MONTH)
                && day == today.get(Calendar.DAY_OF_MONTH);
    }

    private boolean isSameDate(
            int year1,
            int month1,
            int day1,
            int year2,
            int month2,
            int day2) {

        return year1 == year2
                && month1 == month2
                && day1 == day2;
    }

    private boolean isDateBefore(
            int year1,
            int month1,
            int day1,
            int year2,
            int month2,
            int day2) {

        Calendar date1 =
                Calendar.getInstance();

        date1.set(
                year1,
                month1,
                day1,
                0,
                0,
                0
        );

        date1.set(
                Calendar.MILLISECOND,
                0
        );

        Calendar date2 =
                Calendar.getInstance();

        date2.set(
                year2,
                month2,
                day2,
                0,
                0,
                0
        );

        date2.set(
                Calendar.MILLISECOND,
                0
        );

        return date1.before(date2);
    }

    private long getTodayStartMillis() {
        Calendar calendar =
                Calendar.getInstance();

        clearTime(calendar);

        return calendar.getTimeInMillis();
    }

    private long getDateMillis(String date) {
        try {
            SimpleDateFormat sdf =
                    new SimpleDateFormat(
                            "d/M/yyyy",
                            Locale.getDefault()
                    );

            sdf.setLenient(false);

            Date parsed = sdf.parse(date);

            if (parsed == null) {
                return System.currentTimeMillis();
            }
            Calendar calendar =
                    Calendar.getInstance();
            calendar.setTime(parsed);
            clearTime(calendar);
            return calendar.getTimeInMillis();
        } catch (Exception e) {
            return System.currentTimeMillis();
        }
    }
}