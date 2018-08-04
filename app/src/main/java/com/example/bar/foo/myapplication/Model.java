package com.example.bar.foo.myapplication;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static java.lang.Math.floor;
import static java.lang.Math.pow;
import static java.lang.Math.random;

public class Model {

    private static final int BASE_FARMER_COST = 50;
    private static final double FARMER_COST_INCREASE = 1.15;
    private static final int DEFAULT_POINTS = 200;

    private static final String OLD_DUP_LEVEL_INFO = "levelInfo";
    private static final String OLD_EXP_LEVEL_INFO = "expLevelInfo";
    private static final String OLD_PANTSU_INFO = "pantsuInfo";
    private static final String PANTSU = "pantsu";
    private static final String POINTS = "points";
    private static final String FARMERS = "farmers";

    private static final Model INSTANCE = new Model();

    private List<Pantsu> pantsu_;
    private int farmers_ = 0;
    private int pantsuPoints_ = 200;

    private Model() {
        pantsu_ = new ArrayList<Pantsu>(){{
            add(new Pantsu(1,0));
            add(new Pantsu(1,1));
            add(new Pantsu(1,2));
            add(new Pantsu(1,3));

            add(new Pantsu(2,0));
            add(new Pantsu(2,1));
            add(new Pantsu(2,2));
            add(new Pantsu(2,3));

            add(new Pantsu(3,0));
            add(new Pantsu(3,1));
            add(new Pantsu(3,2));
            add(new Pantsu(3,3));

            add(new Pantsu(4,0));
            add(new Pantsu(4,1));
            add(new Pantsu(4,2));
            add(new Pantsu(4,3));

            add(new Pantsu(5,0));
            add(new Pantsu(5,1));
            add(new Pantsu(5,2));
            add(new Pantsu(5,3));
        }};

        farmers_ = 0;
        pantsuPoints_ = DEFAULT_POINTS;
    }

    public static Model getInstance() {
        return INSTANCE;
    }

    public void save(Activity context) {
        SharedPreferences prefs = context.getPreferences(MODE_PRIVATE);
        Gson gson = new Gson();
        prefs.edit().putString(PANTSU, gson.toJson(pantsu_))
                .putString(POINTS, gson.toJson(getPoints()))
                .putString(FARMERS, gson.toJson(getFarmers()))
                .apply();
    }

    public void load(Activity context) {
        SharedPreferences prefs = context.getPreferences(MODE_PRIVATE);
        Gson gson = new Gson();

        String pantsuJson = prefs.getString(PANTSU, null);
        if (pantsuJson != null) {
            pantsu_ = gson.fromJson(pantsuJson, new TypeToken<List<Pantsu>>(){}.getType());
        } else {
            // check for saved data from an older version of the app
            String pantsuInfoJson = prefs.getString(OLD_PANTSU_INFO, null);
            String dupInfoJson = prefs.getString(OLD_DUP_LEVEL_INFO, null);
            String expInfoJson = prefs.getString(OLD_EXP_LEVEL_INFO, null);

            if (pantsuInfoJson != null && dupInfoJson != null) {
                int[] pantsu = gson.fromJson(pantsuInfoJson, int[].class);
                int[] dupLevels = gson.fromJson(dupInfoJson, int[].class);
                int[] expLevels;
                if (expInfoJson != null) {
                    expLevels = gson.fromJson(expInfoJson, int[].class);
                } else {
                    expLevels = new int[]{0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
                }

                ArrayList<Pantsu> newPantsu = new ArrayList<>();
                for (int stars = 1; stars <= 5; stars++) {
                    for (int type = 0; type < 4; type++) {
                        int index = (stars - 1) * 4 + type;
                        newPantsu.add(new Pantsu(stars, type, pantsu[index], expLevels[index], dupLevels[index]));
                    }
                }
                pantsu_ = newPantsu;
            }
        }

        pantsuPoints_ = gson.fromJson(prefs.getString(POINTS, gson.toJson(pantsuPoints_)), int.class);
        farmers_ = gson.fromJson(prefs.getString(FARMERS, gson.toJson(farmers_)), int.class);
    }

    public void reset() {
        for (Pantsu pantsu : pantsu_) {
            pantsu.reset();
        }
        pantsuPoints_ = DEFAULT_POINTS;
        farmers_ = 0;
    }

    public List<Pantsu> getPantsu() {
        return Collections.unmodifiableList(pantsu_);
    }

    @Nullable
    public Pantsu fetchPantsu(Rolls rollType) {
        if (pantsuPoints_ < rollType.getPrice()) {
            return null;
        } else {
            pantsuPoints_ -= rollType.getPrice();
            Pantsu result = randPantsu(rollType);
            getExistingPantsu(result).incrementCount(1);
            return result;
        }
    }

    public boolean buyFarmer() {
        if(pantsuPoints_ < farmerCost())
            return false;
        else {
            pantsuPoints_ -= farmerCost();
            farmers_++;
            return true;
        }
    }

    public int getFarmers() {
        return farmers_;
    }

    public int farmerCost() {
        return (int)pow(BASE_FARMER_COST, pow(FARMER_COST_INCREASE, farmers_));
    }

    public int getPoints() {
        return pantsuPoints_;
    }

    public void farmPantsu() {
        Pantsu result = randPantsu(Rolls.LOW);
        getExistingPantsu(result).incrementCount(farmers_);
    }

    public void levelAll() {
        for (Pantsu pantsu : pantsu_) {
            pantsu.dupLevelMax();
        }
    }

    public void sellExtras() {
        for (Pantsu pantsu : pantsu_) {
            pantsuPoints_ += pantsu.sellExtra();
        }
    }

    private Pantsu randPantsu(Rolls rollType) {
        double rare = random();
        int type = (int)floor(random()*4);
        int stars;
        final double[] chances = rollType.getOdds();
        if (rare > chances[0]) {
            stars = 5;
        } else  if (rare > chances[1]) {
            stars = 4;
        } else  if (rare > chances[2]) {
            stars = 3;
        } else  if (rare > chances[3]) {
            stars = 2;
        } else {
            stars = 1;
        }
        return new Pantsu(stars, type);
    }

    private Pantsu getExistingPantsu(Pantsu template) {
        return pantsu_.get((template.getStars() - 1) * 4 + template.getType());
    }
}
