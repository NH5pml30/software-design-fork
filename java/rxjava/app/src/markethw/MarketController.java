package markethw;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.reactivex.rxjava3.core.Single;
import markethw.dao.MarketDao;
import markethw.exchange.Currency;
import markethw.exchange.PerfectRateProvider;
import markethw.model.User;
import org.bson.Document;
import org.bson.types.ObjectId;

import javax.money.Monetary;
import javax.money.MonetaryAmount;
import javax.money.convert.ExchangeRateProvider;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

import io.reactivex.netty.protocol.http.server.HttpServer;

public class MarketController {
    private final MarketDao marketDao;
    private final ExchangeRateProvider exchangeRateProvider = new PerfectRateProvider();

    public MarketController(MarketDao marketDao) {
        this.marketDao = marketDao;
    }

    private static class NoSuchParameterException extends RuntimeException {
        NoSuchParameterException(String key) {
            super(String.format("Parameter '%s' not found", key));
        }
    }

    private static class InvalidParameterException extends RuntimeException {
        InvalidParameterException(String key, Throwable cause) {
            super(String.format("Parameter '%s' invalid", key), cause);
        }
    }

    private static class InvalidCredentials extends RuntimeException {
        InvalidCredentials(ObjectId userId) {
            super(String.format("User id '%s' is invalid", userId.toHexString()));
        }
    }

    private <T> Single<T> extractParameter(Map<String, List<String>> params, String key, Function<String, T> factory) {
        var res = params.getOrDefault(key, null);
        if (res == null || res.isEmpty()) {
            return Single.error(new NoSuchParameterException(key));
        }
        return Single.just(res.get(0)).map(factory::apply)
                .onErrorResumeNext(e -> Single.error(new InvalidParameterException(key, e)));
    }

    private Single<String> extractParameter(Map<String, List<String>> params, String key) {
        return extractParameter(params, key, Function.identity());
    }

    private Single<String> registerUser(Map<String, List<String>> params) {
        return extractParameter(params, "name").flatMap(name ->
                extractParameter(params, "currency", Currency::valueOf)
                        .flatMap(currency -> marketDao.addUser(name, currency))
                        .map(ObjectId::toHexString));
    }

    private Single<User> authorizeUser(Map<String, List<String>> params) {
        return extractParameter(params, "userId", ObjectId::new)
                .flatMap(userId ->
                        marketDao.authorizeUser(userId)
                                .switchIfEmpty(Single.error(() -> new InvalidCredentials(userId)))
                );
    }

    private MonetaryAmount mapPrice(User user, String price) {
        return Monetary.getDefaultAmountFactory().setCurrency(user.getPreferredCurrencyUnit())
                .setNumber(new BigDecimal(price)).create();
    }

    private Single<String> addProduct(Map<String, List<String>> params) {
        return authorizeUser(params).flatMap(user ->
                extractParameter(params, "name").flatMap(name ->
                        extractParameter(params, "price", x -> mapPrice(user, x))
                                .flatMap(price -> marketDao.addProduct(name, price))
                                .map(ObjectId::toHexString)));
    }

    private Single<String> getProducts(Map<String, List<String>> params) {
        return authorizeUser(params).flatMapObservable(user -> {
            var conv =
                    exchangeRateProvider.getCurrencyConversion(user.getPreferredCurrencyUnit());
            return marketDao.getProducts().map(prod ->
                    new Document(Map.of(
                            "name", prod.getName(),
                            "price", prod.getPriceAmount().with(conv).toString()
                    ))
            );
        }).toList().map(list -> new Document("products", list).toJson());
    }

    private static class RouteInfo {
        public final HttpMethod method;
        public final String path;

        public RouteInfo(HttpMethod method, String path) {
            this.method = method;
            this.path = path;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RouteInfo routeInfo = (RouteInfo) o;
            return Objects.equals(method, routeInfo.method) && Objects.equals(path, routeInfo.path);
        }

        @Override
        public int hashCode() {
            return Objects.hash(method, path);
        }
    }

    private final Map<RouteInfo, Function<Map<String, List<String>>, Single<String>>> routeInfo =
            new HashMap<>(Map.of(
                    new RouteInfo(HttpMethod.GET, "/get-products"), this::getProducts,
                    new RouteInfo(HttpMethod.POST, "/add-product"), this::addProduct,
                    new RouteInfo(HttpMethod.POST, "/register-user"), this::registerUser
            ));

    public void run(int port) {
        HttpServer.newServer(port)
                .start(
                        (req, resp) -> {
                            var action =
                                    routeInfo.getOrDefault(
                                            new RouteInfo(req.getHttpMethod(), req.getDecodedPath()),
                                            null
                                    );
                            if (action == null) {
                                return resp.setStatus(HttpResponseStatus.NOT_FOUND);
                            } else {
                                return resp.writeString(
                                        rx.Observable.from(action.apply(req.getQueryParameters()).toFuture())
                                                .onErrorReturn(Throwable::toString)
                                );
                            }
                        }
                ).awaitShutdown();
    }
}
