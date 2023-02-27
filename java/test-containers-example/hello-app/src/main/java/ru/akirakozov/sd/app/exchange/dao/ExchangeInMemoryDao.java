package ru.akirakozov.sd.app.exchange.dao;

import ru.akirakozov.sd.app.exchange.model.PriceGetter;
import ru.akirakozov.sd.app.shared.model.Share;
import ru.akirakozov.sd.app.shared.model.TransactionInfo;
import ru.akirakozov.sd.app.shared.model.WithId;
import ru.akirakozov.sd.app.shared.model.WithIdMap;

import java.util.concurrent.ConcurrentHashMap;

public class ExchangeInMemoryDao extends ExchangeDao {
    private final WithIdMap<Share> shares = new WithIdMap<>(new ConcurrentHashMap<>());
    private final WithIdMap<TransactionInfo> transactions = new WithIdMap<>(new ConcurrentHashMap<>());

    public ExchangeInMemoryDao(PriceGetter priceGetter) {
        super(priceGetter);
    }

    @Override
    public void updatePrices() {
        shares.get().replaceAll((__, s) -> s.setPrice(priceGetter.getPrice(s.getName())));
    }

    @Override
    public Share addShare(String name) {
        if (name == null) {
            throw new IllegalArgumentException();
        }
        int id = shares.genId();
        var share = new Share(id, name, 0, 0);
        shares.get().put(id, share);
        return share;
    }

    @Override
    public Share getShare(int shareId) {
        return WithId.validate(shareId, shares.get().get(shareId));
    }

    private Share tryChangeShareQuantity(int shareId, int howMuch) {
        return WithId.validate(shareId, shares.get().computeIfPresent(shareId, (__, s) -> s.changeQuantity(howMuch)));
    }

    @Override
    public Share addShareQuantity(int shareId, int howMuch) {
        if (howMuch < 0) {
            throw new IllegalArgumentException("Can only add quantity");
        }
        return tryChangeShareQuantity(shareId, howMuch);
    }

    @Override
    public TransactionInfo startTransaction(int shareId, int howMuch) {
        var share = getShare(shareId);
        if (share.getQuantity() < howMuch) {
            throw new IllegalArgumentException("Insufficient quantity");
        }
        int transactionId = transactions.genId();
        var info = new TransactionInfo(transactionId, shareId, howMuch, share.getPrice() * howMuch);
        transactions.get().put(transactionId, info);
        return info;
    }

    @Override
    public void finishTransaction(int transactionId) {
        TransactionInfo info = transactions.get().remove(transactionId);
        if (info != null) {
            tryChangeShareQuantity(info.getShareId(), -info.getHowMuch());
        }
    }

    @Override
    public void cancelTransaction(int transactionId) {
        transactions.get().remove(transactionId);
    }
}
