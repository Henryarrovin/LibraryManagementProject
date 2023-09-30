package com.example.librarymanagement.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
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
    private Button logoutButton;
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Books");

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        bookList = new ArrayList<>();
        bookAdapter = new BookAdapter(bookList);
        recyclerView.setAdapter(bookAdapter);

        logoutButton  = findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                auth.signOut();
                Intent intent = new Intent(HomeActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });


        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Book clickedBook = bookList.get(position);

                showBookDetailsOverlay(clickedBook);
            }
        }));

        loadDataFromFirestore();
    }

    private void loadDataFromFirestore() {
        db.collection("Books")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @SuppressLint("NotifyDataSetChanged")
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                String author = document.getString("author");
                                String bookName = document.getString("bookName");
                                String image = document.getString("image"); // Assuming image is stored as Base64
                                boolean available = document.getBoolean("available");

                                Book book = new Book(document.getId(), bookName, author, image, available);
                                bookList.add(book);
                            }
                            bookAdapter.notifyDataSetChanged();
                        } else {
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

        TextView authorTextView = dialogView.findViewById(R.id.authorTextView);
        TextView availabilityTextView = dialogView.findViewById(R.id.availabilityTextView);
        TextView bookNameTextView = dialogView.findViewById(R.id.bookNameTextView);
        Button rentButton = dialogView.findViewById(R.id.rentButton);


        authorTextView.setText("Author: " + book.getAuthor());
        availabilityTextView.setText("Availability: " + (book.isAvailable() ? "Available" : "Rented"));
        bookNameTextView.setText("Book Name: " + book.getBookName());

        if (book.isAvailable()) {
            rentButton.setText("Rent");

            rentButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (auth.getCurrentUser() != null) {
                        String bookId = book.getId();
                        DocumentReference bookRef = db.collection("Books").document(bookId);


                        bookRef
                                .update("available", false)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {

                                        Toast.makeText(HomeActivity.this, "Book rented successfully", Toast.LENGTH_SHORT).show();

                                        book.setAvailable(false);
                                        availabilityTextView.setText("Availability: Rented");
                                        rentButton.setText("Rented");
                                        rentButton.setEnabled(false);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e("FirestoreError", "Error updating book availability: " + e.getMessage());
                                    }
                                });

                    } else {
                        Toast.makeText(HomeActivity.this, "Please log in to rent a book", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            rentButton.setText("Rented");
            rentButton.setEnabled(false);
        }

        AlertDialog dialog = builder.create();
        dialog.show();
    }


}
