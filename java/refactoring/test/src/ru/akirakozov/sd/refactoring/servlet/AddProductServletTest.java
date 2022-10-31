package ru.akirakozov.sd.refactoring.servlet;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class AddProductServletTest extends TestUtils {
    private static final String PRODUCT_NAME = "product name";

    @Test
    public void simpleOperability() throws IOException {
        var servlet = new AddProductServlet();
        servlet.doGet(mockRequestNamePrice(PRODUCT_NAME, 200), mockResponse());
        assertResponseOK();
        Assert.assertEquals("OK" + System.lineSeparator(), responseData.getText());
    }
}
