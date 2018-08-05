package com.example.bar.foo.myapplication;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.pow;

public class Pantsu {

    private static final int MAX_LEVEL = 99;

    private final int stars_;
    private final int type_;
    private int count_;
    private int expLevel_;
    private int dupLevel_;

    Pantsu(int stars, int type) {
        this(stars, type, 0, 0, 0);
    }

    Pantsu(int stars, int type, int count, int expLevel, int dupLevel) {
        stars_ = stars;
        type_ = type;
        count_ = count;
        expLevel_ = expLevel;
        dupLevel_ = dupLevel;
    }

    public int getStars() {
        return stars_;
    }

    public int getType() {
        return type_;
    }

    public int getCount() {
        return count_;
    }

    public int getExpLevel() {
        return expLevel_;
    }

    public int getDupLevel() {
        return dupLevel_;
    }

    public int salePrice() {
        return 5 * stars_;
    }

    public int power() {
        // stars^3 + expLevel + dupLevel
        // *     :   1 - 199
        // **    :   8 - 206
        // ***   :  27 - 225
        // ****  :  64 - 262
        // ***** : 125 - 323
        return ((int)pow(stars_, 3)) + dupLevel_ + expLevel_;
    }

    public boolean hasExtras() {
        int spares = max(count_ - 1, 0);
        int remainingLevels = MAX_LEVEL - dupLevel_;
        int extra = max(spares - remainingLevels, 0);
        return extra > 0;
    }

    public boolean canLevel() {
        return count_ > 1 && dupLevel_ < MAX_LEVEL;
    }

    public void incrementCount(int i) {
        count_ += i;
    }

    // level up as many times as possible
    public void dupLevelMax() {
        int spares = max(count_ - 1, 0);
        int remainingLevels = MAX_LEVEL - dupLevel_;
        int newLevels = min(spares, remainingLevels);
        count_ -= newLevels;
        dupLevel_ += newLevels;
    }

    // returns the points from selling the extras
    public int sellExtra() {
        int spares = max(count_ - 1, 0);
        int remainingLevels = MAX_LEVEL - dupLevel_;
        int extra = max(spares - remainingLevels, 0);

        count_ -= extra;
        return extra * salePrice();
    }

    public void reset() {
        expLevel_ = 0;
        dupLevel_ = 0;
        count_ = 0;
    }
}
