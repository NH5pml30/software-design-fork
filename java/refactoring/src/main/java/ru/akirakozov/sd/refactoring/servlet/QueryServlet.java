package ru.akirakozov.sd.refactoring.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.stream.Stream;

/**
 * @author akirakozov
 */
public class QueryServlet extends ProductServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String command = request.getParameter("command");

        if ("max".equals(command)) {
            singleStatementResponse(response, (htmlGenerator, stmt) -> {
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT ORDER BY PRICE DESC LIMIT 1")) {
                    htmlGenerator.appendInTag("h1", "Product with max price: ").newline();
                    forEachResult(rs, formatProduct(htmlGenerator));
                }
            });
        } else if ("min".equals(command)) {
            singleStatementResponse(response, (htmlGenerator, stmt) -> {
                try (ResultSet rs = stmt.executeQuery("SELECT * FROM PRODUCT ORDER BY PRICE LIMIT 1")) {
                    htmlGenerator.appendInTag("h1", "Product with min price: ").newline();
                    forEachResult(rs, formatProduct(htmlGenerator));
                }
            });
        } else if ("sum".equals(command)) {
            singleStatementResponse(response, (htmlGenerator, stmt) -> {
                try (ResultSet rs = stmt.executeQuery("SELECT SUM(price) FROM PRODUCT")) {
                    htmlGenerator.append("Summary price: ").newline();
                    forFirstResult(rs, res -> htmlGenerator.append(String.valueOf(res.getInt(1))).newline());
                }
            });
        } else if ("count".equals(command)) {
            singleStatementResponse(response, (htmlGenerator, stmt) -> {
                try (ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM PRODUCT")) {
                    htmlGenerator.append("Number of products: ").newline();
                    forFirstResult(rs, res -> htmlGenerator.append(String.valueOf(res.getInt(1))).newline());
                }
            });
        } else {
            response.getWriter().println("Unknown command: " + command);
        }

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }

}
