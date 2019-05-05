package com.example.bookcase;


import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookListFragment extends Fragment {

    private Context c;
    private ListView listView;

    private ArrayList<Book> books;
    private BookAdapter adapter;


    private BookInterface mListener;

    public BookListFragment() {
        // Required empty public constructor
    }

    public static BookListFragment newInstance(String param1, String param2) {
        BookListFragment fragment = new BookListFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_book_list, container, false);

        listView = v.findViewById(R.id.bookList);
        books = new ArrayList<>();

        return v;
    }

    public void parseBooks(JSONArray bookArray) {

        books.clear();

        for (int i = 0; i < bookArray.length(); i++) {
            try {
                books.add(new Book(bookArray.getJSONObject(i)));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        adapter = new BookAdapter(c, books);
        adapter.notifyDataSetChanged();
        listView.setAdapter(adapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Book book = books.get(position);
                ((BookInterface) c).bookSelected(book);
            }
        });
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookInterface) {
            this.mListener = (BookInterface) context;
        } else {
            throw new RuntimeException(context.toString());
        }
        this.c = context;
    }

    public interface BookInterface {
        void bookSelected(Book book);
    }
}
