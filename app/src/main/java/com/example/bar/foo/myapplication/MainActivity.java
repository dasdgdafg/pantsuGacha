package com.example.bar.foo.myapplication;

import android.os.Bundle;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Timer timer = new Timer();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                farmPantsu();
                //updateStatus(); how the heck do threads work again
            }
        };

        FloatingActionButton fabManual = (FloatingActionButton) findViewById(R.id.fabManual);
        fabManual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fetchPantsu(); // Maybe show some pantsu images in the background as you click, with a golden one when it's an ssr
                updateStatus();
            }
        });

        FloatingActionButton fabBuyFarmer = (FloatingActionButton) findViewById(R.id.fabBuyFarmer);
        fabBuyFarmer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                buyFarmer();
                if(getFarmers()==1)
                    timer.schedule(timerTask,0,10000);
                updateStatus();
            }
        });
        updateStatus();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void updateStatus() {
        TextView tv = (TextView) findViewById(R.id.sample_text);
        tv.setText(status());
    }


    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */
    public native String status();

    public native void fetchPantsu();

    public native void buyFarmer();

    public native int getFarmers();

    public native void farmPantsu();

}
