package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.util.HtmlGenerator;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Consumer;

public abstract class ProductServlet extends HttpServlet {
    private static final String PRODUCT_NAME_ATTR = "name";
    private static final String PRODUCT_PRICE_ATTR = "price";

    protected HtmlGenerator createHtmlGenerator(HttpServletResponse response) throws IOException {
        return new HtmlGenerator(new PrintWriter(response.getWriter())).beginTag("html").beginTag("body").newline();
    }

    protected interface SQLResultAction {
        void accept(ResultSet rs) throws SQLException;
    }

    protected void forEachResult(ResultSet rs, SQLResultAction action) throws SQLException {
        while (rs.next()) {
            action.accept(rs);
        }
    }

    protected HtmlGenerator formatProduct(HtmlGenerator gen, String name, long price) {
        gen.append(String.format("%s\t%d", name, price)).appendBr().newline();
        return gen;
    }

    protected HtmlGenerator formatProducts(HtmlGenerator gen, ResultSet products) throws SQLException {
        forEachResult(products, product ->
            formatProduct(gen, product.getString(PRODUCT_NAME_ATTR), product.getInt(PRODUCT_PRICE_ATTR)));
        return gen;
    }
}
