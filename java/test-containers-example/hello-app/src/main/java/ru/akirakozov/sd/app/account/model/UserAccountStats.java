package ru.akirakozov.sd.app.account.model;

import java.beans.ConstructorProperties;
import java.util.List;
import java.util.Objects;

public class UserAccountStats {
    public static class ShareStat {
        private final String shareName;
        private final int quantity;
        private final int overallPrice;

        public ShareStat(String shareName, int quantity, int overallPrice) {
            this.shareName = shareName;
            this.quantity = quantity;
            this.overallPrice = overallPrice;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ShareStat shareStat = (ShareStat) o;
            return quantity == shareStat.quantity && overallPrice == shareStat.overallPrice && Objects.equals(shareName, shareStat.shareName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(shareName, quantity, overallPrice);
        }

        public String getShareName() {
            return shareName;
        }

        public int getQuantity() {
            return quantity;
        }

        public int getOverallPrice() {
            return overallPrice;
        }
    }
    private final int id;
    private final List<ShareStat> shareStats;
    private final int balance;
    private final int priceSum;

    @ConstructorProperties({"id", "shareStats", "balance", "priceSum"})
    public UserAccountStats(int id, List<ShareStat> shareStats, int balance, int priceSum) {
        this.id = id;
        this.shareStats = shareStats;
        this.balance = balance;
        this.priceSum = priceSum;
    }

    public List<ShareStat> getShareStats() {
        return shareStats;
    }

    public int getBalance() {
        return balance;
    }

    public int getPriceSum() {
        return priceSum;
    }

    public int getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserAccountStats that = (UserAccountStats) o;
        return id == that.id && balance == that.balance && priceSum == that.priceSum && Objects.equals(shareStats, that.shareStats);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, shareStats, balance, priceSum);
    }
}
