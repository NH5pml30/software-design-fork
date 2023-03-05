package markethw.dao;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import markethw.db.DriverBase;
import markethw.exchange.Currency;
import markethw.model.Product;
import markethw.model.User;
import org.bson.types.ObjectId;

import javax.money.MonetaryAmount;

public class MarketMongoDao extends DriverBase implements MarketDao {
    private static final CollectionDesc<User> userDesc = new CollectionDesc<>("user", User.class);
    private static final CollectionDesc<Product> productDesc = new CollectionDesc<>("product", Product.class);

    public MarketMongoDao(String connectionUrl, String DbName) {
        super(connectionUrl, DbName);
    }

    public MarketMongoDao(String DbName) {
        this("mongodb://localhost:27017", DbName);
    }

    public MarketMongoDao() {
        this("rxhw");
    }

    public Single<ObjectId> addProduct(String name, MonetaryAmount price) {
        return addToCollection(productDesc, new Product(new ObjectId(), name, price));
    }

    public Observable<Product> getProducts() {
        return Observable.fromPublisher(getCollection(productDesc).find());
    }

    public Single<ObjectId> addUser(String name, Currency preferredCurrency) {
        return addToCollection(userDesc, new User(new ObjectId(), name, preferredCurrency));
    }

    public Maybe<User> authorizeUser(ObjectId id) {
        return findInCollection(userDesc, id);
    }
}