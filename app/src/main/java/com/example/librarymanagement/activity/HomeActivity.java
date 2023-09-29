package com.example.librarymanagement.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.librarymanagement.R;
import com.example.librarymanagement.datamodel.Book;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    private Toolbar toolbar;
    private RecyclerView recyclerView;
    private BookAdapter bookAdapter;
    private List<Book> bookList;
    private FirebaseFirestore db;
    private FirebaseAuth auth; // Firebase Authentication

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        // Initialize Firebase Firestore
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance(); // Initialize Firebase Authentication

        // Set up the toolbar (app bar)
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Books"); // Set the title

        // Initialize RecyclerView and its adapter
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(bookList);
        recyclerView.setAdapter(bookAdapter);

        // Set item click listener on RecyclerView
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                // Get the clicked book
                Book clickedBook = bookList.get(position);

                // Show the book details overlay
                showBookDetailsOverlay(clickedBook);
            }
        }));

        // Load data from Firestore
        loadDataFromFirestore();
    }

    private void loadDataFromFirestore() {
        // Replace "Books" with the actual name of your Firestore collection
        db.collection("Books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Parse the book data
                                String author = document.getString("author");
                                String bookName = document.getString("bookName");
                                String image = document.getString("image"); // Assuming image is stored as Base64
                                boolean available = document.getBoolean("available");

                                // Create a book object with image
                                Book book = new Book(document.getId(), bookName, author, image, available);
                                bookList.add(book);
                            }
                            bookAdapter.notifyDataSetChanged(); // Refresh the RecyclerView
                        } else {
                            // Handle errors here
                            Log.e("FirestoreError", "Error fetching data: " + task.getException());
                        }
                    }
                });
    }

    @SuppressLint("SetTextI18n")
    private void showBookDetailsOverlay(Book book) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.activity_book_detail, null);
        builder.setView(dialogView);

        // Find views in the dialog layout
        TextView authorTextView = dialogView.findViewById(R.id.authorTextView);
        TextView availabilityTextView = dialogView.findViewById(R.id.availabilityTextView);
        TextView bookNameTextView = dialogView.findViewById(R.id.bookNameTextView);
        Button rentButton = dialogView.findViewById(R.id.rentButton);

        // Set book details in the dialog
        authorTextView.setText("Author: " + book.getAuthor());
        availabilityTextView.setText("Availability: " + (book.isAvailable() ? "Available" : "Rented"));
        bookNameTextView.setText("Book Name: " + book.getBookName());

        // Set button text and click listener based on availability
        if (book.isAvailable()) {
            rentButton.setText("Rent");

            rentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Perform the rent action here
                    // Check if the user is authenticated
                    if (auth.getCurrentUser() != null) {
                        // Update book availability in Firestore
                        String bookId = book.getId(); // Assuming you have a unique ID for each book
                        DocumentReference bookRef = db.collection("Books").document(bookId);

                        // Set the book's "available" field to false
                        // Update book availability in Firestore
                        bookRef
                                .update("available", false)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        // Book availability updated successfully

                                        // Show a toast message indicating that the book has been rented
                                        Toast.makeText(HomeActivity.this, "Book rented successfully", Toast.LENGTH_SHORT).show();

                                        // Update the book's availability locally
                                        book.setAvailable(false);
                                        availabilityTextView.setText("Availability: Rented");
                                        rentButton.setText("Rented");
                                        rentButton.setEnabled(false);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        // Handle errors if the update fails
                                        Log.e("FirestoreError", "Error updating book availability: " + e.getMessage());
                                    }
                                });

                    } else {
                        // If the user is not authenticated, you can prompt them to log in
                        // or take any other desired action
                        Toast.makeText(HomeActivity.this, "Please log in to rent a book", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            rentButton.setText("Rented");
            rentButton.setEnabled(false);
        }

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
