package ru.akirakozov.sd.refactoring.servlet;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;

public class AddProductServletTest extends TestUtils<AddProductServlet> {
    private static final String PRODUCT_NAME = "product name";

    public AddProductServletTest() {
        super(AddProductServlet::new);
    }

    @Test
    public void simpleOperability() throws IOException {
        var response = mockResponse();
        servlet.doGet(mockRequestNamePrice(PRODUCT_NAME, 200), response);
        assertResponseOK(response);
        Assert.assertEquals("OK", writer.toString());
    }
}
