package ru.akirakozov.sd.app.shared.model;

public class NegativeBalanceException extends RuntimeException {
    public NegativeBalanceException(String what) {
        super(what);
    }
}
