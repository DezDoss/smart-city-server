package com.example.polls.util;

public class Utils {

    public String generateBarCode() {
        long number = (long) Math.floor(Math.random() * 9_000_000_000L) + 1_000_000_000L;
        System.out.println(number);
        return String.valueOf(number);
    }
}
