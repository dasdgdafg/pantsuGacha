package com.example.bar.foo.myapplication;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    public static final String DUP_LEVEL_INFO = "levelInfo";
    public static final String EXP_LEVEL_INFO = "expLevelInfo";
    public static final String PANTSU_INFO = "pantsuInfo";
    public static final String POINTS = "points";
    public static final String FARMERS = "farmers";

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

        // load saved data
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        Gson gson = new Gson();

        // get saved values, defaulting to the current values if there is no saved data
        int[] pantsu = gson.fromJson(prefs.getString(PANTSU_INFO, gson.toJson(pantsuStatus())), int[].class);
        int[] dupLevels = gson.fromJson(prefs.getString(DUP_LEVEL_INFO, gson.toJson(getDupLevels())), int[].class);
        int[] expLevels = gson.fromJson(prefs.getString(EXP_LEVEL_INFO, gson.toJson(getExpLevels())), int[].class);
        int points = gson.fromJson(prefs.getString(POINTS, gson.toJson(getPoints())), int.class);
        int farmers = gson.fromJson(prefs.getString(FARMERS, gson.toJson(getFarmers())), int.class);
        setLoadedData(pantsu, dupLevels, expLevels, points, farmers);

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
                ImageView iv = layout.findViewById(R.id.imageIcon);
                iv.setImageResource(imageForPantsu(stars, type));
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

        View iv = findViewById(R.id.receivedPantsuBackground);
        iv.setAlpha(0);

        updateStatus();
    }

    @Override
    public void onPause() {
        super.onPause();
        // save data
        SharedPreferences prefs = getPreferences(MODE_PRIVATE);
        Gson gson = new Gson();
        prefs.edit().putString(PANTSU_INFO, gson.toJson(pantsuStatus()))
                .putString(DUP_LEVEL_INFO, gson.toJson(getDupLevels()))
                .putString(EXP_LEVEL_INFO, gson.toJson(getExpLevels()))
                .putString(POINTS, gson.toJson(getPoints()))
                .putString(FARMERS, gson.toJson(getFarmers()))
                .apply();
    }

    private class RollClickListener implements View.OnClickListener {
        private final int level;

        public RollClickListener(int level) {
            this.level = level;
        }

        @Override
        public void onClick(View view) {
            int[] result = fetchPantsu(level);
            if (result[0] == 0) {
                showNotEnoughMessage();
            } else {
                final ImageView iv = findViewById(R.id.receivedPantsu);
                final View bg = findViewById(R.id.receivedPantsuBackground);
                iv.setImageResource(imageForPantsu(result[1], result[2]));
                bg.setAlpha(1);
                bg.setBackgroundColor(Color.argb(10 * result[1] * result[1] - 10,255,215,0));

                Animation fadeOut = new AlphaAnimation(1, 0);
                fadeOut.setInterpolator(new AccelerateInterpolator());
                fadeOut.setDuration(1000);
                fadeOut.setStartOffset(200);

                fadeOut.setAnimationListener(new Animation.AnimationListener()
                {
                    public void onAnimationEnd(Animation animation)
                    {
                        bg.setAlpha(0);
                        bg.setBackgroundColor(Color.argb(0,0,0,0));
                    }
                    public void onAnimationRepeat(Animation animation) {}
                    public void onAnimationStart(Animation animation) {}
                });

                bg.startAnimation(fadeOut);
            }
            updateStatus();
        }
    }

    private int imageForPantsu(int stars, int type) {
        return R.drawable.aaa50;
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
        int[] dupLevels = getDupLevels();
        int[] expLevels = getExpLevels();
        for (int i = 0; i < 20; i++) {
            View layout = findViewById(pantsuLayoutIds[i]);
            TextView num = layout.findViewById(R.id.textQuantity);
            num.setText(Integer.toString(pantsuValues[i]));

            TextView level = layout.findViewById(R.id.textDupLevel);
            if (dupLevels[i] == 0) {
                level.setVisibility(View.INVISIBLE);
            } else {
                level.setVisibility(View.VISIBLE);
            }
            level.setText("+" + Integer.toString(dupLevels[i]));

            TextView expLevel = layout.findViewById(R.id.textExpLevel);
            if (expLevels[i] == 0) {
                expLevel.setVisibility(View.INVISIBLE);
            } else {
                expLevel.setVisibility(View.VISIBLE);
            }
            expLevel.setText("+" + Integer.toString(expLevels[i]));
        }
    }

    public native int[] pantsuStatus();

    public native int[] getDupLevels();

    public native int[] getExpLevels();

    // return value is [success, stars, type]
    public native int[] fetchPantsu(int rollType);

    public native boolean buyFarmer();

    public native int getFarmers();

    public native int getFarmerCost();

    public native int getPoints();

    public native int getRollPrice(int rollType);

    public native void farmPantsu();

    public native void levelAll();

    public native void sellExtras();

    public native void setLoadedData(int[] pantsu, int[] dupLevels, int[] expLevels, int points, int farmers);
}
