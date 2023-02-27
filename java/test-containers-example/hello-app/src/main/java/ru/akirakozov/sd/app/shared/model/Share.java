package ru.akirakozov.sd.app.shared.model;

import java.beans.ConstructorProperties;

public class Share extends WithId {
    private final String name;
    private final int quantity;
    private final int price;

    @ConstructorProperties({"id", "name", "quantity", "price"})
    public Share(int id, String name, int quantity, int price) {
        super(id);
        this.name = name;
        this.quantity = quantity;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public Share setName(String name) {
        return new Share(id, name, quantity, price);
    }

    public int getQuantity() {
        return quantity;
    }

    public Share changeQuantity(int howMuch) {
        var res = quantity + howMuch;
        if (res < 0)
            throw new NegativeBalanceException("Insufficient shares for the change");
        return new Share(id, name, res, price);
    }

    public int getPrice() {
        return price;
    }

    public Share setPrice(int price) {
        return new Share(id, name, quantity, price);
    }
}
