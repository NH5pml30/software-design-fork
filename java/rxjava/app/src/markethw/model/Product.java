package markethw.model;

import markethw.exchange.Currency;
import markethw.exchange.PerfectRateProvider;
import markethw.model.util.WithId;
import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonIgnore;
import org.bson.types.ObjectId;

import javax.money.*;
import javax.money.convert.*;
import java.math.BigDecimal;
import java.util.Objects;

public class Product extends WithId {
    private final static CurrencyUnit STORED_CURRENCY =
            Monetary.getCurrency(CurrencyQueryBuilder.of().setCurrencyCodes(Currency.RUB.toString()).build());
    private static final MonetaryAmountFactory<?> factory =
            Monetary.getDefaultAmountFactory().setCurrency(STORED_CURRENCY);
    private static final CurrencyConversion TO_STORED =
            new PerfectRateProvider().getCurrencyConversion(STORED_CURRENCY);

    private String name;
    private MonetaryAmount price;

    @BsonCreator
    public Product() {}

    private static BigDecimal fromMonetaryAmount(MonetaryAmount price) {
        return price.with(TO_STORED).getNumber().numberValue(BigDecimal.class);
    }

    private static MonetaryAmount toMonetaryAmount(BigDecimal price) {
        return factory.setNumber(price).create();
    }

    public Product(ObjectId id, String name, MonetaryAmount price) {
        super(id);
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getPrice() {
        return fromMonetaryAmount(price);
    }

    public void setPrice(BigDecimal price) {
        this.price = toMonetaryAmount(price);
    }

    @BsonIgnore
    public MonetaryAmount getPriceAmount() {
        return price;
    }

    @BsonIgnore
    public void setPriceAmount(MonetaryAmount price) {
        this.price = price;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        Product product = (Product) o;
        return Objects.equals(name, product.name) && Objects.equals(price, product.price);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), name, price);
    }
}
