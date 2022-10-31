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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestUtils {
    protected static class ResponseData {
        static class StatusRef {
            int status;

            public int getStatus() {
                return status;
            }

            public void setStatus(int status) {
                this.status = status;
            }
        }
        StringWriter responseWriter;
        StatusRef statusRef = new StatusRef();

        ResponseData() {
            init();
        }

        void init() {
            responseWriter = new StringWriter();
        }

        String getText() {
            return responseWriter.toString();
        }
    }

    ResponseData responseData = new ResponseData();

    protected final String DROP_SQL = "drop table if exists product";
    protected final String INIT_SQL =
            "create table if not exists product (" +
            " id integer primary key autoincrement not null," +
            " name text not null," +
            " price int not null" +
                    ")";
    protected final String SQL_URL = "jdbc:sqlite:test.db";

    void initSQL() throws Exception {
        try (Connection c = DriverManager.getConnection(SQL_URL);
             Statement stmt = c.createStatement()) {
            stmt.executeUpdate(DROP_SQL);
            stmt.executeUpdate(INIT_SQL);
        }
    }

    @Before
    public void setUp() throws Exception {
        responseData.init();
        initSQL();
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

    protected HttpServletResponse mockResponse(Writer writer, ResponseData.StatusRef statusRef) throws IOException {
        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(writer));
        doAnswer(invocation -> {
            statusRef.setStatus(invocation.getArgument(0, Integer.class));
            return null;
        }).when(response).setStatus(anyInt());
        doAnswer(ignored -> statusRef.getStatus()).when(response).getStatus();
        return response;
    }

    protected HttpServletResponse mockResponse() throws IOException {
        return mockResponse(responseData.responseWriter, responseData.statusRef);
    }

    protected void assertResponseOK(int status) {
        Assert.assertEquals(HttpServletResponse.SC_OK, status);
    }

    protected void assertResponseOK(HttpServletResponse response) {
        assertResponseOK(response.getStatus());
    }

    protected void assertResponseOK() {
        assertResponseOK(responseData.statusRef.getStatus());
    }

    protected static StringBuilder sbNewLine(StringBuilder builder) {
        return builder.append(System.lineSeparator());
    }

    protected String getExpectedGetText(Map<String, Long> products) {
        StringBuilder builder = sbNewLine(new StringBuilder("<html><body>"));
        products.forEach((name, price) ->
                sbNewLine(builder.append(name).append("\t").append(price).append("</br>")));
        return sbNewLine(builder.append("</body></html>")).toString();
    }
}
