package com.example.bar.foo.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.ImageView;
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

    static int[] pantsuLayoutIds = { R.id.pantsu10, R.id.pantsu11, R.id.pantsu12, R.id.pantsu13,
            R.id.pantsu20, R.id.pantsu21, R.id.pantsu22, R.id.pantsu23,
            R.id.pantsu30, R.id.pantsu31, R.id.pantsu32, R.id.pantsu33,
            R.id.pantsu40, R.id.pantsu41, R.id.pantsu42, R.id.pantsu43,
            R.id.pantsu50, R.id.pantsu51, R.id.pantsu52, R.id.pantsu53 };

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
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateStatus();
                    }
                });
            }
        };
        timer.schedule(timerTask,0,10000);

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
                boolean success = buyFarmer();
                if (!success) {
                    Toast.makeText(MainActivity.this, R.string.notEnoughPantsu, Toast.LENGTH_SHORT).show();
                }
                updateStatus();
            }
        });
        String star = "*";
        for (int stars = 1; stars <= 5; stars++) {
            for (int type = 0; type < 4; type++) {
                int i = (stars - 1) * 4 + type;
                View layout = findViewById(pantsuLayoutIds[i]);
                TextView starText = layout.findViewById(R.id.textStars);
                starText.setText(star);
            }
            star = star + "*";
        }
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


    @SuppressLint("SetTextI18n")
    private void updateStatus() {
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(farmStatus());

        int[] pantsuValues = pantsuStatus();
        for (int i = 0; i < 20; i++) {
            View layout = findViewById(pantsuLayoutIds[i]);
            TextView num = layout.findViewById(R.id.textQuantity);
            num.setText(Integer.toString(pantsuValues[i]));
        }
    }

    public native String farmStatus();

    public native int[] pantsuStatus();

    public native void fetchPantsu();

    public native boolean buyFarmer();

    public native int getFarmers();

    public native void farmPantsu();

}
