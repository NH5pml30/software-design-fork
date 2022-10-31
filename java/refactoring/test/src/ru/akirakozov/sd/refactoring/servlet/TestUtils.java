package ru.akirakozov.sd.refactoring.servlet;

import org.junit.Assert;
import org.junit.Before;
import org.mockito.stubbing.Answer;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Map;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestUtils<ServletT extends HttpServlet> {
    protected final ServletT servlet;
    protected StringWriter writer;

    TestUtils(Supplier<ServletT> factory) {
        this.servlet = factory.get();
    }

    @Before
    public void setUp() {
        writer = new StringWriter();
    }

    protected HttpServletRequest mockRequest(Map<String, String> content) {
        var res = mock(HttpServletRequest.class);
        when(res.getParameter(any())).thenAnswer(
                (Answer<String>) invocationOnMock ->
                        content.getOrDefault(invocationOnMock.getArgument(0, String.class), null)
        );
        return res;
    }

    protected HttpServletRequest mockRequestNamePrice(String name, long price) {
        return mockRequest(Map.of("name", name, "price", String.valueOf(price)));
    }

    protected HttpServletRequest mockRequestCommand(String command) {
        return mockRequest(Map.of("command", command));
    }

    protected HttpServletResponse mockResponse(Writer writer) throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(writer));
        return response;
    }

    protected HttpServletResponse mockResponse() throws IOException {
        return mockResponse(writer);
    }

    protected void assertResponseOK(HttpServletResponse response) {
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
}
