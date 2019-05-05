package com.example.bookcase;


import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;


/**
 * A simple {@link Fragment} subclass.
 */
public class BookDetailsFragment extends Fragment {

    public BookDetailsFragment() {
        // Required empty public constructor
    }

    private Context c;

    private TextView textView;
    private ImageView imageView;
    private String title;
    private String author;
    private String publish;
    private static final String BOOK_KEY = "BOOK";
    private Book book;



    private ImageButton playBtn;
    private ImageButton pauseBtn;
    private ImageButton stopBtn;
    private SeekBar seekBar;
    private SeekBar downloadProgress;
    private ProgressBar progressBar;
    private TextView progressTextView;
    private Button downloadBtn;

    private BookDetailsInterface mListener;

    public static BookDetailsFragment newInstance(com.example.bookcase.Book bookList) {
        BookDetailsFragment fragment = new BookDetailsFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(BOOK_KEY, bookList);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            book = getArguments().getParcelable(BOOK_KEY);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_book_details, container, false);
        textView = view.findViewById(R.id.bookTitle);
        imageView = view.findViewById(R.id.bookImage);

        playBtn = view.findViewById(R.id.playBtn);
        pauseBtn = view.findViewById(R.id.pauseBtn);
        stopBtn = view.findViewById(R.id.stopBtn);
        seekBar = view.findViewById(R.id.seekBar);
        progressBar = view.findViewById(R.id.progressBar);
        progressTextView = view.findViewById(R.id.progressTextView);
        downloadBtn = view.findViewById(R.id.downloadBtn);
        downloadProgress = view.findViewById(R.id.downloadProgress);

        if (getArguments() != null) {
            displayBook(book);
        }downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View V) {

                // check the button value
                if(downloadBtn.getText().toString().equals("Download")) {
                    String query = "https://kamorris.com/lab/audlib/download.php?id=" + Integer.toString(book.getId());
                    new downloadBook(downloadProgress, downloadBtn, c).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, query);



                } else {


                    downloadBtn.setText(R.string.download_txt);
                }
            }
        });

        return view;
    }


    public void displayBook(final com.example.bookcase.Book bookObj) {

        author = bookObj.getAuthor();
        title = bookObj.getTitle();
        publish = bookObj.getPublished();
        textView.setText(" \"" + title + "\" ");
        textView.append(" " + author);
        textView.append(" " + publish);

        String imageURL = bookObj.getCoverURL();
        Picasso.get().load(imageURL).into(imageView);


        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BookDetailsInterface)c).playBook(bookObj.getId());
            }
        });

        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BookDetailsInterface)c).pauseBook();
            }
        });

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((BookDetailsInterface)c).stopBook();
            }
        });


        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                progressBar.setProgress(progress);
                ((BookDetailsInterface)c).seekBook(progress);
                progressTextView.setText(" " + progress + "%");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
    }

    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof BookListFragment.BookInterface) {
            mListener = (BookDetailsInterface) context;
        } else {
            throw new RuntimeException(context.toString()
            );
        }
        this.c = context;
    }

    public interface BookDetailsInterface{
        void playBook(int id);
        void pauseBook();
        void stopBook();
        void seekBook(int position);
    }

    private static class downloadBook extends AsyncTask<String, Integer, String> {
        SeekBar downloadProgress;
        Button downloadBtn;
        Context c;
        char id;

        public downloadBook(SeekBar dl_progress, Button dl_btn, Context c) {
            this.downloadProgress = dl_progress;
            this.downloadBtn = dl_btn;
            this.c = c;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            //make download progress bar visible
            downloadProgress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... sUrl) {

            id = sUrl[0].charAt(sUrl[0].length() - 1);


            OutputStream output = null;
            InputStream input = null;
            HttpURLConnection connection = null;
            try {
                URL url = new URL(sUrl[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    return "Server returned HTTP " + connection.getResponseCode()
                            + " " + connection.getResponseMessage();
                }

                int fileLength = connection.getContentLength();

                input = connection.getInputStream();

                output = new FileOutputStream(c.getFilesDir().getPath() + "/" + id + ".mp3");

                byte data[] = new byte[4096];

                long total = 0;

                int count;

                while ((count = input.read(data)) != -1) {

                    if (isCancelled()) {

                        input.close();
                        return null;
                    }

                    total += count;

                    if (fileLength > 0)

                        publishProgress((int) (total * 100 / fileLength));

                    output.write(data, 0, count);
                }
            } catch (Exception e) {

                return e.toString();

            } finally {
                try {
                    if (output != null)
                             output.close();

                    if (input != null)
                            input.close();
                }
                catch (IOException ignored) {

                }

                if (connection != null)
                    connection.disconnect();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {

            super.onProgressUpdate(progress);

            downloadProgress.setIndeterminate(false);

            downloadProgress.setMax(100);

            downloadProgress.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {

            downloadProgress.setVisibility(View.INVISIBLE);


            if (result != null) { //check if file has downloaded
                Toast.makeText(c, "Error: " + result, Toast.LENGTH_LONG).show();

            } else {
                Toast.makeText(c, "File has downloaded", Toast.LENGTH_SHORT).show();


            }
        }
    }
}