package ru.akirakozov.sd.refactoring.servlet;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.mockito.stubbing.Answer;
import ru.akirakozov.sd.refactoring.util.SQLAccessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class TestUtils {
    protected final static String DROP_SQL = "drop table if exists product";
    protected final static String INIT_SQL =
            "create table if not exists product (" +
                    " id integer primary key autoincrement not null," +
                    " name text not null," +
                    " price int not null" +
                    ")";
    protected final static String INSERT_SQL_STUB = "insert into product (name, price) values ";
    protected final static String SEP = System.lineSeparator();

    protected static <K, V> Map<K, V> optionalToMap(Optional<Map.Entry<K, V>> opt) {
        return opt.map(e -> Map.of(e.getKey(), e.getValue())).orElse(Map.of());
    }

    protected final static Map<String, Function<Map<String, Long>, String>> QUERIES = Map.of(
            "min", prods -> addHeader("Product with min price: ",
                    SEP + formatProducts(
                            optionalToMap(prods.entrySet().stream().min(Map.Entry.comparingByValue()))
                    ), ""),
            "max", prods -> addHeader("Product with max price: ",
                    SEP + formatProducts(
                            optionalToMap(prods.entrySet().stream().max(Map.Entry.comparingByValue()))
                    ), ""),
            "sum", prods -> "Summary price: " + SEP + prods.values().stream().reduce(0L, Long::sum) + SEP,
            "count", prods -> "Number of products: " + SEP + prods.size() + SEP
    );

    private static String generateDbUrl() {
        return "jdbc:sqlite:test-tmp.db";
    }

    protected String sqlUrl = generateDbUrl();
    protected SQLAccessor sqlAccessor = new SQLAccessor(sqlUrl);

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

    protected void runSQLUpdates(List<String> updates) throws Exception {
        try (Connection c = DriverManager.getConnection(sqlUrl);
             Statement stmt = c.createStatement()) {
            for (var update : updates) {
                stmt.executeUpdate(update);
            }
        }
    }

    @Before
    public void setUp() throws Exception {
        responseData.init();
        runSQLUpdates(List.of(DROP_SQL, INIT_SQL));
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
        responseData.init();
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

    protected static String formatProducts(Stream<Map.Entry<String, Long>> products) {
        return products
                .map(entry -> String.format("%s\t%d</br>", entry.getKey(), entry.getValue()) + System.lineSeparator())
                .collect(Collectors.joining());
    }

    protected static String formatProducts(Map<String, Long> products) {
        return formatProducts(products.entrySet().stream());
    }

    protected static String wrapTags(List<String> tags, String content, String sep) {
        var start = tags.stream().map(s -> "<" + s + ">").collect(Collectors.joining());
        var revTags = new ArrayList<>(tags);
        Collections.reverse(revTags);
        var end = revTags.stream().map(s -> "</" + s + ">").collect(Collectors.joining());
        return start + sep + content + end + sep;
    }

    protected static String wrapTags(List<String> tags, String content) {
        return wrapTags(tags, content, SEP);
    }

    protected static String wrapHtml(String content) {
        return wrapTags(List.of("html", "body"), content);
    }

    protected static String addHeader(String header, String content, String sep) {
        return wrapTags(List.of("h1"), header, sep) + content + sep;
    }

    protected String addHeader(String header, String content) {
        return addHeader(header, content, SEP);
    }

    protected String getAddSQLUpdate(Map<String, Long> products) {
        if (products.isEmpty()) {
            return "";
        }

        return INSERT_SQL_STUB +
                products.entrySet().stream()
                        .map(entry -> String.format("(\"%s\", %d)", entry.getKey(), entry.getValue()))
                        .collect(Collectors.joining(", "));
    }

    protected void testAdd(AddProductServlet servlet, String name, long price) throws IOException {
        servlet.doGet(mockRequestNamePrice(name, price), mockResponse());
        assertResponseOK();
        Assert.assertEquals("OK" + System.lineSeparator(), responseData.getText());
    }

    protected String testGetStub(GetProductsServlet servlet) throws IOException {
        servlet.doGet(mockRequest(Map.of()), mockResponse());
        assertResponseOK();
        return responseData.getText();
    }

    protected void testGetSingle(GetProductsServlet servlet, Map<String, Long> prods) throws IOException {
        Assert.assertTrue(prods.size() <= 1);
        Assert.assertEquals(wrapHtml(formatProducts(prods)), testGetStub(servlet));
    }

    protected void testGetTwo(GetProductsServlet servlet, Map<String, Long> prods) throws IOException {
        Assert.assertTrue(prods.size() <= 2);
        if (prods.size() <= 1) {
            testGetSingle(servlet, prods);
        } else {
            var entries = new ArrayList<>(prods.entrySet());
            var revEntries = new ArrayList<>(entries);
            Collections.reverse(revEntries);
            assertThat(testGetStub(servlet),
                    anyOf(
                            is(wrapHtml(formatProducts(entries.stream()))),
                            is(wrapHtml(formatProducts(revEntries.stream())))
                    ));
        }
    }

    void testQuery(QueryServlet servlet, Map<String, Long> prods) throws IOException {
        for (var query : QUERIES.entrySet()) {
            servlet.doGet(mockRequestCommand(query.getKey()), mockResponse());
            assertResponseOK();
            Assert.assertEquals(wrapHtml(query.getValue().apply(prods)), responseData.getText());
        }
    }
}
