package com.example.libraryapptest;

import static com.example.libraryapptest.EditBookActivity.EXTRA_EDIT_BOOK_AUTHOR;
import static com.example.libraryapptest.EditBookActivity.EXTRA_EDIT_BOOK_TITLE;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.core.view.WindowCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.libraryapptest.databinding.ActivityMainBinding;

import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    private BookViewModel bookViewModel;

    public static final int NEW_BOOK_ACTIVITY_REQUEST_CODE = 1;

    public static final int EDIT_BOOK_ACTIVITY_REQUEST_CODE = 2;

    private Book editedBook = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        final BookAdapter adapter = new BookAdapter();
        recyclerView.setAdapter(adapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        bookViewModel = new ViewModelProvider(this).get(BookViewModel.class);
        bookViewModel.findAll().observe(this, adapter::setBooks);



        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false; // nic nie robimy tutaj
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                Snackbar.make(recyclerView, "Zarchiwizowano książkę", Snackbar.LENGTH_LONG).show();
                adapter.notifyItemChanged(position); // brak usuwania
            }
        });

        itemTouchHelper.attachToRecyclerView(recyclerView);
        FloatingActionButton addBookButton = findViewById(R.id.add_button);
        addBookButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, EditBookActivity.class);
                startActivityForResult(intent, NEW_BOOK_ACTIVITY_REQUEST_CODE);
                Snackbar.make(findViewById(R.id.coordinator_layout), getString(R.string.book_added),
                        Snackbar.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private class BookHolder extends RecyclerView.ViewHolder implements View.OnClickListener, View.OnLongClickListener {

        private TextView bookTitleTextView;
        private TextView bookAuthorTextView;
        private Book currentBook;


        public BookHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.book_list_item, parent, false));
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(this);

            bookTitleTextView = itemView.findViewById(R.id.book_title);
            bookAuthorTextView = itemView.findViewById(R.id.book_author);
        }

        public void bind(Book book) {
            currentBook = book;
            bookTitleTextView.setText(book.getTitle());
            bookAuthorTextView.setText(book.getAuthor());
        }

        @Override
        public void onClick(View view) {
            MainActivity.this.editedBook = this.currentBook;


            if (currentBook != null) {
                Intent intent = new Intent(MainActivity.this, EditBookActivity.class);
                intent.putExtra(EXTRA_EDIT_BOOK_TITLE, currentBook.getTitle());
                intent.putExtra(EXTRA_EDIT_BOOK_AUTHOR, currentBook.getAuthor());
                startActivityForResult(intent, EDIT_BOOK_ACTIVITY_REQUEST_CODE);

            }
        }

        @Override
        public boolean onLongClick(View v) {
            MainActivity.this.bookViewModel.delete(this.currentBook);
            return true;
        }
    }

    private class BookAdapter extends RecyclerView.Adapter<BookHolder> {
        private List<Book> books;

        @NonNull
        @Override
        public BookHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new BookHolder(getLayoutInflater(), parent);
        }

        @Override
        public void onBindViewHolder(@NonNull BookHolder holder, int position) {
            if (books != null) {
                Book book = books.get(position);
                holder.bind(book);
            }   else
                Log.d("MainActivity", "No books");

        }

        @Override
        public int getItemCount() {
            if (books != null)
                return books.size();
            else
                return 0;
        }

        void setBooks(List<Book> books) {
            {
                this.books = books;
                notifyDataSetChanged();
            }
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);


        if (resultCode == RESULT_OK) {
            if (requestCode == NEW_BOOK_ACTIVITY_REQUEST_CODE) {
                Book book = new Book(data.getStringExtra(EXTRA_EDIT_BOOK_TITLE),
                        data.getStringExtra(EXTRA_EDIT_BOOK_AUTHOR));
                bookViewModel.insert(book);
                Snackbar.make(findViewById(R.id.coordinator_layout), getString(R.string.book_added),
                        Snackbar.LENGTH_LONG).show();
            } else if (requestCode == EDIT_BOOK_ACTIVITY_REQUEST_CODE) {
                editedBook.setTitle(data.getStringExtra(EXTRA_EDIT_BOOK_TITLE));
                editedBook.setAuthor(data.getStringExtra(EXTRA_EDIT_BOOK_AUTHOR));
                bookViewModel.update(editedBook);
                editedBook = null;
                Snackbar.make(findViewById(R.id.coordinator_layout),
                                "Book edited",
                                Snackbar.LENGTH_LONG)
                        .show();
            }


        } else {
            Snackbar.make(findViewById(R.id.coordinator_layout),
                            getString(R.string.empty_not_saved),
                            Snackbar.LENGTH_LONG)
                    .show();
        }
    }
}
