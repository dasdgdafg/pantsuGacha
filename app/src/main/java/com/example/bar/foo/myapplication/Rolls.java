package com.example.bar.foo.myapplication;

public enum Rolls {
    // free  * - ****  65% 24% 10% 01% 00% - pantsu sells for  7.35 average
    // low   * - ****  60% 25% 10% 05% 00% - pantsu sells for  8.00 average
    // med   * - ***** 30% 25% 20% 20% 05% - pantsu sells for 12.25 average -  5% chance of *****
    // high ** - ***** 00% 30% 25% 25% 20% - pantsu sells for 16.75 average - 20% chance of *****
    FREE(0, new double[]{1.00, 0.99, 0.89, 0.65}),
    LOW(25, new double[]{1.00, 0.95, 0.85, 0.60}), // also used for farming
    MED(1000, new double[]{0.95, 0.75, 0.55, 0.30}),
    HIGH(4000, new double[]{0.80, 0.55, 0.30, 0.00});

    private final int price_;
    private final double[] odds_;

    private Rolls(int price, double[] odds) {
        price_ = price;
        odds_ = odds;
    }

    public int getPrice() {
        return price_;
    }

    public double[] getOdds() {
        return odds_;
    }
}
