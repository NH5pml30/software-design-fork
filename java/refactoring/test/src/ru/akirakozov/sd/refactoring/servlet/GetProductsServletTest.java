package ru.akirakozov.sd.refactoring.servlet;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GetProductsServletTest extends TestUtils {
    private static final String PRODUCT_NAME = "product name";
    private static final long PRODUCT_PRICE = 300;

    private void testProds(GetProductsServlet servlet, Map<String, Long> prods) throws Exception {
        runSQLUpdates(List.of(DROP_SQL, INIT_SQL, getAddSQLUpdate(prods)));
        servlet.doGet(mockRequest(Map.of()), mockResponse());
        assertResponseOK();
        Assert.assertEquals(wrapHtml(formatProducts(prods)), responseData.getText());
    }

    @Test
    public void simpleOperability() throws Exception {
        testProds(new GetProductsServlet(), Map.of());
    }

    @Test
    public void nonEmptyTest() throws Exception {
        var prods = Map.of(PRODUCT_NAME, PRODUCT_PRICE);
        testProds(new GetProductsServlet(), prods);
    }
}
