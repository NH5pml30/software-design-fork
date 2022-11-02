package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.util.SQLAccessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author akirakozov
 */
public class QueryServlet extends ProductServlet {
    public QueryServlet(SQLAccessor sqlAccessor) {
        super(sqlAccessor);
    }

    private static class QueryAction {
        String query;
        SingleQueryResponse action;

        public QueryAction(String query, SingleQueryResponse action) {
            this.query = query;
            this.action = action;
        }
    }

    private final Map<String, QueryAction> queryActionMap = Map.of(
            "max", new QueryAction(
                    String.format("SELECT * FROM %s ORDER BY PRICE DESC LIMIT 1", PRODUCT_TABLE),
                    (htmlGenerator, rs) -> {
                        htmlGenerator.appendInTag("h1", "Product with max price: ").newline();
                        forEachResult(rs, formatProduct(htmlGenerator));
                    }
            ),
            "min", new QueryAction(
                    String.format("SELECT * FROM %s ORDER BY PRICE LIMIT 1", PRODUCT_TABLE),
                    (htmlGenerator, rs) -> {
                        htmlGenerator.appendInTag("h1", "Product with min price: ").newline();
                        forEachResult(rs, formatProduct(htmlGenerator));
                    }
            ),
            "sum", new QueryAction(
                    String.format("SELECT SUM(%s) FROM %s", PRODUCT_PRICE_ATTR, PRODUCT_TABLE),
                    (htmlGenerator, rs) -> {
                        htmlGenerator.append("Summary price: ").newline();
                        forFirstResult(rs, res ->
                                htmlGenerator.append(String.valueOf(res.getInt(1))).newline());
                    }
            ),
            "count", new QueryAction(
                    String.format("SELECT COUNT(*) FROM %s", PRODUCT_TABLE),
                    (htmlGenerator, rs) -> {
                        htmlGenerator.append("Number of products: ").newline();
                        forFirstResult(rs, res ->
                                htmlGenerator.append(String.valueOf(res.getInt(1))).newline());
                    }
            )
    );

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        var action = queryActionMap.getOrDefault(command, null);
        if (action != null) {
            singleQueryResponse(response, action.query, action.action);
        } else {
            response.getWriter().println("Unknown command: " + command);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
