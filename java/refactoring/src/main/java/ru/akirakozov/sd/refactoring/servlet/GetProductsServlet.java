package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.util.SQLAccessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author akirakozov
 */
public class GetProductsServlet extends ProductServlet {
    public GetProductsServlet(SQLAccessor sqlAccessor) {
        super(sqlAccessor);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        singleQueryResponse(response, String.format("SELECT * FROM %s", PRODUCT_TABLE),
                (htmlGenerator, rs) -> forEachResult(rs, formatProduct(htmlGenerator)));

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
    }
}
