package ru.akirakozov.sd.app.account.dao;

import org.springframework.web.client.RestClientException;
import ru.akirakozov.sd.app.shared.model.NegativeBalanceException;
import ru.akirakozov.sd.app.account.model.UserAccount;
import ru.akirakozov.sd.app.shared.model.TransactionInfo;
import ru.akirakozov.sd.app.shared.model.WithId;
import ru.akirakozov.sd.app.shared.model.WithIdMap;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AccountInMemoryDao extends AccountDao {
    private final WithIdMap<UserAccount> accounts = new WithIdMap<>(new ConcurrentHashMap<>());

    public AccountInMemoryDao(String exchangeUrl) {
        super(exchangeUrl);
    }

    @Override
    public UserAccount addAccount() {
        int id = accounts.genId();
        var account = new UserAccount(id, 0, Map.of());
        accounts.get().put(id, account);
        return account;
    }

    @Override
    public UserAccount getAccount(int accountId) {
        return WithId.validate(accountId, accounts.get().get(accountId));
    }

    private UserAccount tryChangeAccountBalance(int accountId, int howMuch) {
        return accounts.get().computeIfPresent(accountId, (__, a) -> a.changeBalance(howMuch));
    }
    private UserAccount tryChangeShares(int accountId, int shareId, int howMuch) {
        return accounts.get().computeIfPresent(accountId, (__, a) -> a.changeShares(shareId, howMuch));
    }

    @Override
    public UserAccount addAccountBalance(int accountId, int howMuch) {
        if (howMuch < 0) {
            throw new IllegalArgumentException("Can only add balance");
        }
        return WithId.validate(accountId, tryChangeAccountBalance(accountId, howMuch));
    }

    private UserAccount applyInfoToShares(int accountId, TransactionInfo info) {
        return tryChangeShares(accountId, info.getShareId(), info.getHowMuch());
    }
    private UserAccount applyInfoToBalance(int accountId, TransactionInfo info) {
        return tryChangeAccountBalance(accountId, -info.getPrice());
    }

    private interface InfoApplier {
        UserAccount applyInfo(int accountId, TransactionInfo info);
    }

    private UserAccount endExchangeTransaction(int accountId, int shareId, int howMuch,
                                               InfoApplier first, InfoApplier second) {
        var info = startExchangeTransaction(shareId, howMuch); // throws

        try {
            WithId.validate(
                    accountId,
                    first.applyInfo(accountId, info) // throws
            ); // throws
        } catch (NegativeBalanceException | WithId.NotFoundException e) {
            try {
                cancelExchangeTransaction(info.getId());
            } catch (RestClientException ignored) {
            }
            throw new IllegalArgumentException("Payment failed", e);
        }

        try {
            finishExchangeTransaction(info.getId());
        } catch (RestClientException e) {
            first.applyInfo(accountId, info.invert());
            throw e;
        }
        return WithId.validate(
                accountId,
                second.applyInfo(accountId, info) // does not throw
        );
    }

    @Override
    public UserAccount buyOrSellShares(int accountId, int shareId, int howMuch) {
        if (!accounts.get().containsKey(accountId)) {
            throw WithId.createNotFound(accountId);
        }

        if (howMuch > 0) {
            return endExchangeTransaction(accountId, shareId, howMuch, this::applyInfoToBalance, this::applyInfoToShares);
        } else {
            return endExchangeTransaction(accountId, shareId, howMuch, this::applyInfoToShares, this::applyInfoToBalance);
        }
    }
}
