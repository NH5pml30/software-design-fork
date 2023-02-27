package ru.akirakozov.sd.app.shared.model;

import java.beans.ConstructorProperties;

public class TransactionInfo extends WithId {
    private final int shareId;
    private final int howMuch;
    private final int price;

    @ConstructorProperties({"id", "shareId", "howMuch", "price"})
    public TransactionInfo(int id, int shareId, int howMuch, int price) {
        super(id);
        this.shareId = shareId;
        this.howMuch = howMuch;
        this.price = price;
    }

    public int getPrice() {
        return price;
    }

    public int getHowMuch() {
        return howMuch;
    }

    public int getShareId() {
        return shareId;
    }

    public TransactionInfo invert() {
        return new TransactionInfo(id, shareId, -howMuch, -price);
    }
}