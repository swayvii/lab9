package com.example.bookcase;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;

public class BookAdapter extends BaseAdapter implements Filterable {

    private Context c;
    private ArrayList<com.example.bookcase.Book> books;

    public BookAdapter(Context context, ArrayList<com.example.bookcase.Book> books) {
        this.c = context;
        this.books = books;
    }

    @Override
    public int getCount() {

        return books.size();
    }

    @Override
    public Object getItem(int position) {
        return books.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView textView = new TextView(c);
        textView.setText(books.get(position).getTitle());

        return textView;
    }

    @Override
    public Filter getFilter() {
        return null;
    }
}
