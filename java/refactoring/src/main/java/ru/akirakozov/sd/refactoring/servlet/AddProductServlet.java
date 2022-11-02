package ru.akirakozov.sd.refactoring.servlet;

import ru.akirakozov.sd.refactoring.util.SQLAccessor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author akirakozov
 */
public class AddProductServlet extends ProductServlet {
    public AddProductServlet(SQLAccessor sqlAccessor) {
        super(sqlAccessor);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String name = request.getParameter("name");
        long price = Long.parseLong(request.getParameter("price"));

        withStatementUnchecked(stmt ->
            stmt.executeUpdate(String.format("INSERT INTO %s (%s, %s) VALUES (\"%s\", %d)",
                    PRODUCT_TABLE, PRODUCT_NAME_ATTR, PRODUCT_PRICE_ATTR, name, price))
        );

        response.setContentType("text/html");
        response.setStatus(HttpServletResponse.SC_OK);
        response.getWriter().println("OK");
    }
}
