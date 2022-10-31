package ru.akirakozov.sd.refactoring.servlet;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class AddProductServletTest extends TestUtils {
    @Test
    public void simpleOperability() throws IOException {
        var servlet = new AddProductServlet();
        testAdd(servlet, "product name", 200);
    }
}
