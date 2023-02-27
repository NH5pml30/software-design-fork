package ru.akirakozov.sd.app.exchange.model;

import java.util.Random;

public class RandomPriceGetter implements PriceGetter {
    private final int min;
    private final int max;


    public RandomPriceGetter(int min, int max) {
        this.min = min;
        this.max = max;
    }

    @Override
    public int getPrice(String shareName) {
        return new Random().nextInt(max - min) + min;
    }
}
