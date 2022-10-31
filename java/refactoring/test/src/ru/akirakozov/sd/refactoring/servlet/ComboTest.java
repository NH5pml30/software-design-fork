package ru.akirakozov.sd.refactoring.servlet;

import org.junit.Test;

import java.io.IOException;
import java.util.*;

public class ComboTest extends TestUtils {
    @Test
    public void comboTest() throws IOException {
        var addServlet = new AddProductServlet();
        var getServlet = new GetProductsServlet();
        var queryServlet = new QueryServlet();

        var prods = Map.of("product1", 200L, "product2", 300L);
        var buildProds = new HashMap<String, Long>();

        for (var entry : prods.entrySet()) {
            testAdd(addServlet, entry.getKey(), entry.getValue());
            buildProds.put(entry.getKey(), entry.getValue());
            testGetTwo(getServlet, buildProds);
            testQuery(queryServlet, buildProds);
        }

        testQuery(queryServlet, prods);
    }
}
