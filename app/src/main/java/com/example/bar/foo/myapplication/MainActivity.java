package com.example.bar.foo.myapplication;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {

    static final int[] pantsuLayoutIds = { R.id.pantsu10, R.id.pantsu11, R.id.pantsu12, R.id.pantsu13,
            R.id.pantsu20, R.id.pantsu21, R.id.pantsu22, R.id.pantsu23,
            R.id.pantsu30, R.id.pantsu31, R.id.pantsu32, R.id.pantsu33,
            R.id.pantsu40, R.id.pantsu41, R.id.pantsu42, R.id.pantsu43,
            R.id.pantsu50, R.id.pantsu51, R.id.pantsu52, R.id.pantsu53 };

    private Model model_ = Model.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        model_.load(this);

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
                        model_.farmPantsu();
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
                boolean success = model_.buyFarmer();
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
                model_.levelAll();
                updateStatus();
            }
        });

        Button sellButton = findViewById(R.id.sellButton);
        sellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                model_.sellExtras();
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
        freeRolls.findViewById(R.id.rollButton).setOnClickListener(new RollClickListener(Rolls.FREE));
        TextView freeRollText = freeRolls.findViewById(R.id.rollPrice);
        freeRollText.setText(String.format(getString(R.string.freeRollText), Rolls.FREE.getPrice()));

        View lowRolls = findViewById(R.id.rollLow);
        lowRolls.findViewById(R.id.rollButton).setOnClickListener(new RollClickListener(Rolls.LOW));
        TextView lowRollText = lowRolls.findViewById(R.id.rollPrice);
        lowRollText.setText(String.format(getString(R.string.lowRollText), Rolls.LOW.getPrice()));

        View medRolls = findViewById(R.id.rollMed);
        medRolls.findViewById(R.id.rollButton).setOnClickListener(new RollClickListener(Rolls.MED));
        TextView medRollText = medRolls.findViewById(R.id.rollPrice);
        medRollText.setText(String.format(getString(R.string.medRollText), Rolls.MED.getPrice()));

        View highRolls = findViewById(R.id.rollHigh);
        highRolls.findViewById(R.id.rollButton).setOnClickListener(new RollClickListener(Rolls.HIGH));
        TextView highRollText = highRolls.findViewById(R.id.rollPrice);
        highRollText.setText(String.format(getString(R.string.highRollText), Rolls.HIGH.getPrice()));

        View iv = findViewById(R.id.receivedPantsuBackground);
        iv.setAlpha(0);

        updateStatus();
    }

    @Override
    public void onPause() {
        model_.save(this);
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private class RollClickListener implements View.OnClickListener {
        private final Rolls rollLevel_;

        public RollClickListener(Rolls rollLevel) {
            rollLevel_ = rollLevel;
        }

        @Override
        public void onClick(View view) {
            Pantsu result = model_.fetchPantsu(rollLevel_);
            if (result == null) {
                showNotEnoughMessage();
            } else {
                final ImageView iv = findViewById(R.id.receivedPantsu);
                final View bg = findViewById(R.id.receivedPantsuBackground);
                iv.setImageResource(imageForPantsu(result.getStars(), result.getType()));
                bg.setAlpha(1);
                bg.setBackgroundColor(Color.argb(10 * result.getStars() * result.getStars() - 10,255,215,0));

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

        if (id == R.id.action_reset) {
            // https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle("Reset all data")
                    .setMessage("Are you sure you want to reset your data?")
                    .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            model_.reset();
                            updateStatus();
                        }
                    })
                    .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // do nothing
                        }
                    })
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show();
        }

        return super.onOptionsItemSelected(item);
    }

    private void showNotEnoughMessage() {
        Toast.makeText(MainActivity.this, R.string.notEnoughPantsu, Toast.LENGTH_SHORT).show();
    }

    @SuppressLint("SetTextI18n")
    private void updateStatus() {
        TextView tv = findViewById(R.id.sample_text);
        tv.setText(String.format(getString(R.string.farmerStatus), model_.getFarmers(), model_.farmerCost()));

        String pointString = getString(R.string.points) + Integer.toString(model_.getPoints());
        TextView points = findViewById(R.id.pantyPoints);
        points.setText(pointString);
        TextView points2 = findViewById(R.id.roll_pantyPoints);
        points2.setText(pointString);

        List<Pantsu> pantsus = model_.getPantsu();
        for (Pantsu pantsu : pantsus) {
            View layout = findViewById(pantsuLayoutIds[(pantsu.getStars() - 1) * 4 + pantsu.getType()]);
            TextView num = layout.findViewById(R.id.textQuantity);
            num.setText(Integer.toString(pantsu.getCount()));

            TextView level = layout.findViewById(R.id.textDupLevel);
            if (pantsu.getDupLevel() == 0) {
                level.setVisibility(View.INVISIBLE);
            } else {
                level.setVisibility(View.VISIBLE);
            }
            level.setText("+" + Integer.toString(pantsu.getDupLevel()));

            TextView expLevel = layout.findViewById(R.id.textExpLevel);
            if (pantsu.getExpLevel() == 0) {
                expLevel.setVisibility(View.INVISIBLE);
            } else {
                expLevel.setVisibility(View.VISIBLE);
            }
            expLevel.setText("+" + Integer.toString(pantsu.getExpLevel()));
        }
    }
}
