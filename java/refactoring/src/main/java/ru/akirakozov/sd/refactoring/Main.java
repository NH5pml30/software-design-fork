package ru.akirakozov.sd.refactoring;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import ru.akirakozov.sd.refactoring.servlet.AddProductServlet;
import ru.akirakozov.sd.refactoring.servlet.GetProductsServlet;
import ru.akirakozov.sd.refactoring.servlet.QueryServlet;
import ru.akirakozov.sd.refactoring.util.SQLAccessor;

/**
 * @author akirakozov
 */
public class Main {
    private static final String SQL_URL = "jdbc:sqlite:test.db";
    private static final String SQL_INIT_UPDATE =
            "CREATE TABLE IF NOT EXISTS PRODUCT" +
            "(ID INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL," +
            " NAME           TEXT    NOT NULL, " +
            " PRICE          INT     NOT NULL)";

    public static void main(String[] args) throws Exception {
        var sqlAccessor = new SQLAccessor(SQL_URL);
        sqlAccessor.withStatement(stmt -> stmt.executeUpdate(SQL_INIT_UPDATE));

        Server server = new Server(8081);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new AddProductServlet(sqlAccessor)), "/add-product");
        context.addServlet(new ServletHolder(new GetProductsServlet(sqlAccessor)),"/get-products");
        context.addServlet(new ServletHolder(new QueryServlet(sqlAccessor)),"/query");

        server.start();
        server.join();
    }
}
