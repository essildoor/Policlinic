package com.exigen.util;

import java.util.Random;

/**
 * generates random unique int
 */
public class InsuranceID {
    private static InsuranceID instance;
    private int id;
    private Random random;


    private InsuranceID() {
        id = 1000000000;
        random = new Random();
    }

    public static synchronized InsuranceID getInstance() {
        if (instance == null)
            instance = new InsuranceID();
        return instance;
    }

    public synchronized int getId() {
        id += 1 + random.nextInt(50);
        return id;
    }

    public static void main(String[] args) {
        InsuranceID id1 = InsuranceID.getInstance();
        for (int i = 0; i < 1000; i ++)
            System.out.println(id1.getId());
    }
}
