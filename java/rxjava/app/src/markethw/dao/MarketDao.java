package markethw.dao;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Single;
import markethw.exchange.Currency;
import markethw.model.Product;
import markethw.model.User;
import org.bson.types.ObjectId;

import javax.money.MonetaryAmount;

public interface MarketDao {
    Single<ObjectId> addProduct(String name, MonetaryAmount price);
    Observable<Product> getProducts();
    Single<ObjectId> addUser(String name, Currency preferredCurrency);
    Maybe<User> authorizeUser(ObjectId id);
}
