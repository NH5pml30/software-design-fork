package ru.akirakozov.sd.app.account.model;

import ru.akirakozov.sd.app.shared.model.NegativeBalanceException;
import ru.akirakozov.sd.app.shared.model.Share;
import ru.akirakozov.sd.app.shared.model.WithId;

import java.util.*;
import java.util.stream.Collectors;

public class UserAccount extends WithId {
    private final int balance;
    private final Map<Integer, Integer> shares;

    public UserAccount(int id, int balance, Map<Integer, Integer> shares) {
        super(id);
        this.balance = balance;
        this.shares = Collections.unmodifiableMap(shares);
    }

    public int getBalance() {
        return balance;
    }

    public UserAccount changeBalance(int howMuch) {
        var res = balance + howMuch;
        if (res < 0) {
            throw new NegativeBalanceException("Insufficient funds for the purchase");
        }
        return new UserAccount(id, res, shares);
    }

    public Map<Integer, Integer> getShares() {
        return shares;
    }

    public UserAccount setShares(Map<Integer, Integer> shares) {
        return new UserAccount(id, balance, Collections.unmodifiableMap(shares));
    }

    public UserAccount changeShares(int shareId, int howMuch) {
        var newShares = new HashMap<>(shares);
        newShares.compute(shareId, (__, was) -> {
            int prev = 0;
            if (was != null) {
                prev = was;
            }
            var res = prev + howMuch;
            if (res < 0) {
                throw new NegativeBalanceException("Insufficient shares for the change");
            }
            if (res == 0) {
                return null;
            }
            return res;
        });
        return setShares(newShares);
    }

    public UserAccountStats getStats(ShareInfoGetter infoGetter) {
        List<UserAccountStats.ShareStat> stats = shares.entrySet().stream().map(
                e -> {
                    Share info = infoGetter.getShareInfo(e.getKey());
                    return new UserAccountStats.ShareStat(
                            info.getName(),
                            e.getValue(),
                            e.getValue() * info.getPrice());
                }).collect(Collectors.toUnmodifiableList());
        return new UserAccountStats(id, stats, balance,
                stats.stream().map(UserAccountStats.ShareStat::getOverallPrice).reduce(0, Integer::sum));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        UserAccount that = (UserAccount) o;
        return balance == that.balance && Objects.equals(shares, that.shares);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), balance, shares);
    }
}
