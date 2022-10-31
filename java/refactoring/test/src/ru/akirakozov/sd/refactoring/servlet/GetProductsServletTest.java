package ru.akirakozov.sd.refactoring.servlet;

import org.junit.Test;

import java.util.List;
import java.util.Map;

public class GetProductsServletTest extends TestUtils {
    protected void testProds(GetProductsServlet servlet, Map<String, Long> prods) throws Exception {
        runSQLUpdates(List.of(DROP_SQL, INIT_SQL, getAddSQLUpdate(prods)));
        testGetSingle(servlet, prods);
    }

    @Test
    public void simpleOperability() throws Exception {
        testProds(new GetProductsServlet(), Map.of());
    }

    @Test
    public void nonEmptyTest() throws Exception {
        var prods = Map.of("product name", 300L);
        testProds(new GetProductsServlet(), prods);
    }
}
