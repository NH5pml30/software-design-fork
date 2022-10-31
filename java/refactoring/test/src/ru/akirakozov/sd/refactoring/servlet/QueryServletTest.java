package ru.akirakozov.sd.refactoring.servlet;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

public class QueryServletTest extends TestUtils {
    void testProds(QueryServlet servlet, Map<String, Long> prods) throws Exception {
        runSQLUpdates(List.of(DROP_SQL, INIT_SQL, getAddSQLUpdate(prods)));
        for (var query : QUERIES.entrySet()) {
            servlet.doGet(mockRequestCommand(query.getKey()), mockResponse());
            assertResponseOK();
            Assert.assertEquals(wrapHtml(query.getValue().apply(prods)), responseData.getText());
        }
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
