package ru.akirakozov.sd.app.exchange.dao;

import ru.akirakozov.sd.app.exchange.model.PriceGetter;
import ru.akirakozov.sd.app.shared.model.Share;
import ru.akirakozov.sd.app.shared.model.TransactionInfo;

public abstract class ExchangeDao {
    protected final PriceGetter priceGetter;

    public ExchangeDao(PriceGetter priceGetter) {
        this.priceGetter = priceGetter;
    }

    public abstract void updatePrices();

    public abstract Share addShare(String name);
    public abstract Share getShare(int shareId);
    public abstract Share addShareQuantity(int shareId, int howMuch);

    public abstract TransactionInfo startTransaction(int shareId, int howMuch);
    public abstract void finishTransaction(int transactionId);
    public abstract void cancelTransaction(int transactionId);
}
