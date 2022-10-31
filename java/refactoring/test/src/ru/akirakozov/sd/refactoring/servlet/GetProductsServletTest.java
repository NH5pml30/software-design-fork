package ru.akirakozov.sd.refactoring.servlet;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.Map;

public class GetProductsServletTest extends TestUtils {
    @Test
    public void simpleOperability() throws IOException {
        var servlet = new GetProductsServlet();
        servlet.doGet(mockRequest(Map.of()), mockResponse());
        assertResponseOK();
        Assert.assertEquals(getExpectedGetText(Map.of()), responseData.getText());
    }
}
