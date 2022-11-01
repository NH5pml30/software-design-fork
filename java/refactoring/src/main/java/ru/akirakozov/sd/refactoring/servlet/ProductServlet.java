package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.util.HtmlGenerator;
import ru.akirakozov.sd.refactoring.util.SQLAccessor;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public abstract class ProductServlet extends HttpServlet {
    static final String SQL_URL = "jdbc:sqlite:test.db";
    private static final String PRODUCT_NAME_ATTR = "name";
    private static final String PRODUCT_PRICE_ATTR = "price";

    protected interface SingleStatementResponse {
        void accept(HtmlGenerator gen, Statement stmt) throws SQLException;
    }

    ProductServlet(String dbUrl) {
        this.sqlAccessor = new SQLAccessor(dbUrl);
    }

    ProductServlet() {
        this(SQL_URL);
    }

    protected HtmlGenerator createHtmlGenerator(HttpServletResponse response) throws IOException {
        return new HtmlGenerator(new PrintWriter(response.getWriter())).beginTag("html").beginTag("body").newline();
    }

    protected interface SQLResultAction {
        void accept(ResultSet rs) throws SQLException;
    }

    protected boolean forFirstResult(ResultSet rs, SQLResultAction action) throws SQLException {
        if (rs.next()) {
            action.accept(rs);
            return true;
        }
        return false;
    }

    protected void forEachResult(ResultSet rs, SQLResultAction action) throws SQLException {
        while (forFirstResult(rs, action)) {}
    }

    protected HtmlGenerator formatProduct(HtmlGenerator gen, String name, long price) {
        gen.append(String.format("%s\t%d", name, price)).appendBr().newline();
        return gen;
    }

    protected SQLResultAction formatProduct(HtmlGenerator gen) {
        return prod -> formatProduct(gen, prod.getString(PRODUCT_NAME_ATTR), prod.getInt(PRODUCT_PRICE_ATTR));
    }

    protected void withStatementUnchecked(SQLAccessor.SQLAction<Statement> action) {
        try {
            sqlAccessor.withStatement(action);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    protected void singleStatementResponse(HttpServletResponse response, SingleStatementResponse action)
            throws IOException {
        var gen = createHtmlGenerator(response);
        withStatementUnchecked(s -> action.accept(gen, s));
        // don't need to close tags if exception is thrown
        gen.close();
    }

    protected SQLAccessor sqlAccessor;
}
