package markethw.model;

import markethw.model.util.WithId;
import markethw.exchange.Currency;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import javax.money.CurrencyQueryBuilder;
import javax.money.CurrencyUnit;
import javax.money.Monetary;
import java.util.Objects;

public class User extends WithId {
    private String name;
    private Currency preferredCurrency;

    @BsonCreator
    public User() {}

    public User(ObjectId id, String name, Currency preferredCurrency) {
        super(id);
        this.name = name;
        this.preferredCurrency = preferredCurrency;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Currency getPreferredCurrency() {
        return preferredCurrency;
    }

    public void setPreferredCurrency(Currency preferredCurrency) {
        this.preferredCurrency = preferredCurrency;
    }

    @BsonIgnore
    public CurrencyUnit getPreferredCurrencyUnit() {
        return Monetary.getCurrency(CurrencyQueryBuilder.of().setCurrencyCodes(preferredCurrency.toString()).build());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        User user = (User) o;
        return Objects.equals(name, user.name) && preferredCurrency == user.preferredCurrency;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, preferredCurrency);
    }
}
