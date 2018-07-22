package com.example.bar.foo.myapplication;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.widget.Button;
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

    static final int[] pantsuLayoutIds = { R.id.pantsu10, R.id.pantsu11, R.id.pantsu12, R.id.pantsu13,
            R.id.pantsu20, R.id.pantsu21, R.id.pantsu22, R.id.pantsu23,
            R.id.pantsu30, R.id.pantsu31, R.id.pantsu32, R.id.pantsu33,
            R.id.pantsu40, R.id.pantsu41, R.id.pantsu42, R.id.pantsu43,
            R.id.pantsu50, R.id.pantsu51, R.id.pantsu52, R.id.pantsu53 };

    // should match Rolls enum in C++
    public static final int ROLL_FREE = 0;
    public static final int ROLL_LOW = 1;
    public static final int ROLL_MED = 2;
    public static final int ROLL_HIGH = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.contentFragment).setVisibility(View.VISIBLE);
        findViewById(R.id.rollFragment).setVisibility(View.GONE);

        final Timer timer = new Timer();
        final TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        farmPantsu();
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
                if (findViewById(R.id.contentFragment).getVisibility() == View.GONE) {
                    findViewById(R.id.contentFragment).setVisibility(View.VISIBLE);
                    findViewById(R.id.rollFragment).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.contentFragment).setVisibility(View.GONE);
                    findViewById(R.id.rollFragment).setVisibility(View.VISIBLE);
                }
            }
        });

        FloatingActionButton fabBuyFarmer = (FloatingActionButton) findViewById(R.id.fabBuyFarmer);
        fabBuyFarmer.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                boolean success = buyFarmer();
                if (!success) {
                    showNotEnoughMessage();
                }
                updateStatus();
            }
        });

        Button levelButton = findViewById(R.id.levelButton);
        levelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                levelAll();
                updateStatus();
            }
        });

        Button sellButton = findViewById(R.id.sellButton);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sellExtras();
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

        View freeRolls = findViewById(R.id.rollFree);
        freeRolls.findViewById(R.id.rollButton).setOnClickListener(new RollClickListener(ROLL_FREE));
        TextView freeRollText = freeRolls.findViewById(R.id.rollPrice);
        freeRollText.setText(String.format(getString(R.string.freeRollText), getRollPrice(ROLL_FREE)));

        View lowRolls = findViewById(R.id.rollLow);
        lowRolls.findViewById(R.id.rollButton).setOnClickListener(new RollClickListener(ROLL_LOW));
        TextView lowRollText = lowRolls.findViewById(R.id.rollPrice);
        lowRollText.setText(String.format(getString(R.string.lowRollText), getRollPrice(ROLL_LOW)));

        View medRolls = findViewById(R.id.rollMed);
        medRolls.findViewById(R.id.rollButton).setOnClickListener(new RollClickListener(ROLL_MED));
        TextView medRollText = medRolls.findViewById(R.id.rollPrice);
        medRollText.setText(String.format(getString(R.string.medRollText), getRollPrice(ROLL_MED)));

        View highRolls = findViewById(R.id.rollHigh);
        highRolls.findViewById(R.id.rollButton).setOnClickListener(new RollClickListener(ROLL_HIGH));
        TextView highRollText = highRolls.findViewById(R.id.rollPrice);
        highRollText.setText(String.format(getString(R.string.highRollText), getRollPrice(ROLL_HIGH)));

        updateStatus();
    }

    private class RollClickListener implements View.OnClickListener {
        private final int level;

        public RollClickListener(int level) {
            this.level = level;
        }

        @Override
        public void onClick(View view) {
            boolean success = fetchPantsu(level);
            if (!success) {
                showNotEnoughMessage();
            }
            updateStatus();
        }
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

    private void showNotEnoughMessage() {
        Toast.makeText(MainActivity.this, R.string.notEnoughPantsu, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    private void updateStatus() {
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(String.format(getString(R.string.farmerStatus), getFarmers(), getFarmerCost()));

        String pointString = getString(R.string.points) + Integer.toString(getPoints());
        TextView points = findViewById(R.id.pantyPoints);
        points.setText(pointString);
        TextView points2 = findViewById(R.id.roll_pantyPoints);
        points2.setText(pointString);

        int[] pantsuValues = pantsuStatus();
        int[] levels = getLevels();
        for (int i = 0; i < 20; i++) {
            View layout = findViewById(pantsuLayoutIds[i]);
            TextView num = layout.findViewById(R.id.textQuantity);
            num.setText(Integer.toString(pantsuValues[i]));
            TextView level = layout.findViewById(R.id.textLevel);
            if (levels[i] == 0) {
                level.setVisibility(View.INVISIBLE);
            } else {
                level.setVisibility(View.VISIBLE);
            }
            level.setText("+" + Integer.toString(levels[i]));
        }
    }

    public native int[] pantsuStatus();

    public native int[] getLevels();

    public native boolean fetchPantsu(int rollType);

    public native boolean buyFarmer();

    public native int getFarmers();

    public native int getFarmerCost();

    public native int getPoints();

    public native int getRollPrice(int rollType);

    public native void farmPantsu();

    public native void levelAll();

    public native void sellExtras();

}
