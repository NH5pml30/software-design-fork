import markethw.MarketController;
import markethw.dao.MarketMongoDao;

public class Main {
    public static void main(String[] args) {
        new MarketController(new MarketMongoDao()).run(8080);
    }
}