package ru.akirakozov.sd.app.account.dao;

import org.springframework.web.client.RestTemplate;
import ru.akirakozov.sd.app.shared.model.Share;
import ru.akirakozov.sd.app.account.model.UserAccount;

import ru.akirakozov.sd.app.account.model.UserAccountStats;
import ru.akirakozov.sd.app.shared.model.TransactionInfo;

public abstract class AccountDao {
    protected final String exchangeUrl;
    protected RestTemplate template = new RestTemplate();

    public AccountDao(String exchangeUrl) {
        this.exchangeUrl = exchangeUrl;
    }

    protected TransactionInfo startExchangeTransaction(int shareId, int howMuch) {
        var res = template.postForObject(exchangeUrl + "/start-transaction?shareId={sid}&howMuch={hm}",
                null, TransactionInfo.class, shareId, howMuch);
        if (res == null || Integer.compare(res.getPrice(), 0) != Integer.compare(res.getHowMuch(), 0)) {
            throw new IllegalArgumentException();
        }
        return res;
    }

    protected void finishExchangeTransaction(int transactionId) {
        template.postForObject(exchangeUrl + "/finish-transaction?transactionId={tid}", null, Void.class,
                transactionId);
    }

    protected void cancelExchangeTransaction(int transactionId) {
        template.put(exchangeUrl + "/cancel-transaction?transactionId={tid}", null, transactionId);
    }

    protected Share getShare(int shareId) {
        return template.getForObject(exchangeUrl + "/get-share?shareId={sid}", Share.class, shareId);
    }

    public UserAccountStats getStats(int accountId) {
        return getAccount(accountId).getStats(this::getShare);
    }

    public abstract UserAccount addAccount();
    public abstract UserAccount getAccount(int accountId);
    public abstract UserAccount addAccountBalance(int accountId, int howMuch);

    public abstract UserAccount buyOrSellShares(int accountId, int shareId, int howMuch);
}
