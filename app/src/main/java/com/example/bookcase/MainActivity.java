package com.example.bookcase;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import edu.temple.audiobookplayer.AudiobookService;

public class MainActivity extends AppCompatActivity implements BookListFragment.BookInterface {


    private boolean singlePane;
    private BookDetailsFragment detailsFragment;
    private ViewPagerFragment viewPagerFragment;
    private BookListFragment listFragment;
    private EditText searchText;
    private Button searchBtn;
    private JSONArray bookArray;

    private String searchWord;



    boolean isConnected;
    AudiobookService.MediaControlBinder mediaControlBinder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        searchText = findViewById(R.id.searchText);
        searchBtn = findViewById(R.id.searchButton);


        //check for single pane
        singlePane = findViewById(R.id.container_2) == null;
        detailsFragment = new BookDetailsFragment();
        listFragment = new BookListFragment();
        viewPagerFragment = new ViewPagerFragment();


        bindService(new Intent(this, AudiobookService.class), serviceConnection, BIND_AUTO_CREATE);


        if (!singlePane) {
            addFragment(listFragment, R.id.container_1);
            addFragment(detailsFragment, R.id.container_2);
        } else {
            addFragment(viewPagerFragment, R.id.container_1);
        }

        //search click
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchWord = searchText.getText().toString();

                fetchBooks(searchWord);

            }
        });
        searchBtn.performClick();
    }

    public void addFragment(Fragment fragment, int id) {
        getSupportFragmentManager().
                beginTransaction().
                replace(id, fragment).
                addToBackStack(null).
                commit();
    }

    public void fetchBooks(final String search) {
        new Thread() {
            public void run() {
                try {
                    String urlString = "https://kamorris.com/lab/audlib/booksearch.php?search=" + search;
                    URL url = new URL(urlString);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                    StringBuilder builder = new StringBuilder();
                    String tmpString;
                    while ((tmpString = reader.readLine()) != null) {
                        builder.append(tmpString);
                    }
                    Message msg = Message.obtain();
                    msg.obj = builder.toString();
                    urlHandler.sendMessage(msg);
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

        }.start();
    }

    Handler urlHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            try {
                bookArray = new JSONArray((String) msg.obj);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            if (singlePane) {
                viewPagerFragment.addPager(bookArray);
            } else {
                listFragment.parseBooks(bookArray);
            }
            return false;
        }
    });

    @Override
    public void bookSelected(com.example.bookcase.Book bookObj) {
        detailsFragment.displayBook(bookObj);
    }

    ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaControlBinder = ((AudiobookService.MediaControlBinder) service);
            isConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isConnected = false;
            mediaControlBinder = null;
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(isConnected) {
            unbindService(serviceConnection);
            isConnected = false;
        }
    }


    @Override
    public void playBook(int id)
    {
        mediaControlBinder.play(id);
    }

    @Override
    public void pauseBook() {

        mediaControlBinder.pause();
    }

    @Override
    public void stopBook() {

        mediaControlBinder.stop();
    }

    @Override
    public void seekBook(int position) {
        mediaControlBinder.seekTo(position);
    }

    public void setProgress(int position, boolean fromUser) {
        if(fromUser) {
            mediaControlBinder.seekTo(position);
        }
    }

    public void setProgHand(Handler handler)
    {
        mediaControlBinder.setProgressHandler(handler);
    }
}


}
