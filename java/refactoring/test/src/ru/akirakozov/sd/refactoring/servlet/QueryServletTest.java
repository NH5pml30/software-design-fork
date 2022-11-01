package ru.akirakozov.sd.refactoring.servlet;

import org.junit.Test;

import java.util.List;
import java.util.Map;

public class QueryServletTest extends TestUtils {
    void testProds(QueryServlet servlet, Map<String, Long> prods) throws Exception {
        runSQLUpdates(List.of(DROP_SQL, INIT_SQL, getAddSQLUpdate(prods)));
        testQuery(servlet, prods);
    }

    @Test
    public void simpleOperability() throws Exception {
        var servlet = new QueryServlet();
        testProds(servlet, Map.of());
    }

    @Test
    public void nonEmptyTest() throws Exception {
        var servlet = new QueryServlet();
        testProds(servlet, Map.of("product1", 200L, "product2", 300L, "product3", 100L));
    }
}
