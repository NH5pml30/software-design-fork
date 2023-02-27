package ru.akirakozov.sd.app.account.dao;

import org.junit.Assert;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import ru.akirakozov.sd.app.account.model.UserAccount;
import ru.akirakozov.sd.app.account.model.UserAccountStats;
import ru.akirakozov.sd.app.shared.model.Share;

import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;


public class AppTest {

    @ClassRule
    public static final GenericContainer<?> simpleWebServer
            = new GenericContainer<>("hello-app:1.0-SNAPSHOT")
            .withExposedPorts(8080);

    private static String getExchangeUrl(String service) {
        return "http://localhost:" + simpleWebServer.getMappedPort(8080) + service;
    }

    private static String getExchangeUrl() {
        return getExchangeUrl("");
    }

    private AccountDao accountDao;
    private final RestTemplate exchangeTemplate = new RestTemplate();

    private Share addShare(String name, int howMuch) {
        var s = exchangeTemplate.postForObject(getExchangeUrl("/add-share?shareName={sn}"),
                null, Share.class, name);
        return exchangeTemplate.postForObject(getExchangeUrl("/add-quantity?shareId={sid}&howMuch={hm}"),
                null, Share.class, s.getId(), howMuch);
    }
    private void updatePrices() {
        exchangeTemplate.put(getExchangeUrl("/update-prices"), null);
    }
    private Share getShare(int shareId) {
        return exchangeTemplate.postForObject(
                getExchangeUrl("/get-share?shareId={sid}"), null, Share.class, shareId
        );
    }
    private Share addShareAndUpdatePrices(String name, int howMuch) {
        var s = addShare(name, howMuch);
        updatePrices();
        return getShare(s.getId());
    }

    @Before
    public void setUp() {
        accountDao = new AccountInMemoryDao(getExchangeUrl());
    }

    @Test
    public void testAddAccount() {
        var acc = accountDao.addAccount();
        Assert.assertNotNull(acc);
        Assert.assertEquals(new UserAccount(acc.getId(), 0, Map.of()), acc);
    }

    @Test
    public void testGetAccount() {
        var acc = accountDao.addAccount();
        Assert.assertNotNull(acc);
        var acc2 = accountDao.getAccount(acc.getId());
        Assert.assertEquals(acc, acc2);
    }

    @Test
    public void testAddBalance() {
        var acc = accountDao.addAccount();
        Assert.assertNotNull(acc);
        var acc2 = accountDao.addAccountBalance(acc.getId(), 1000);
        Assert.assertNotNull(acc2);
        Assert.assertEquals(acc2, acc.changeBalance(1000));
        var acc3 = accountDao.getAccount(acc.getId());
        Assert.assertEquals(acc2, acc3);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testInvalidAddBalance() {
        accountDao.addAccountBalance(accountDao.addAccount().getId(), -2);
    }

    @Test
    public void testBuyOrSellShares() {
        var share = addShareAndUpdatePrices("TEST", 1000);

        var acc = accountDao.addAccount();
        Assert.assertNotNull(acc);
        acc = accountDao.addAccountBalance(acc.getId(), 1000);
        Assert.assertNotNull(acc);

        var acc2 = accountDao.buyOrSellShares(acc.getId(), share.getId(), 1);
        Assert.assertNotNull(acc2);
        Assert.assertEquals(acc.getId(), acc2.getId());
        Assert.assertEquals(acc.getBalance() - share.getPrice(), acc2.getBalance());
        Assert.assertEquals(Map.of(share.getId(), 1), acc2.getShares());

        var acc3 = accountDao.buyOrSellShares(acc.getId(), share.getId(), -1);
        Assert.assertEquals(acc, acc3);
    }

    @Test
    public void testStats() {
        var share1 = addShare("TEST1", 1000);
        var share2 = addShare("TEST2", 400);
        updatePrices();
        share1 = getShare(share1.getId());
        share2 = getShare(share2.getId());

        var acc = accountDao.addAccount();
        Assert.assertNotNull(acc);

        var stats = accountDao.getStats(acc.getId());
        Assert.assertEquals(new UserAccountStats(acc.getId(), List.of(), 0, 0), stats);

        acc = accountDao.addAccountBalance(acc.getId(), 10000);
        Assert.assertNotNull(acc);

        stats = accountDao.getStats(acc.getId());
        Assert.assertEquals(new UserAccountStats(acc.getId(), List.of(), 10000, 0), stats);

        var acc2 = accountDao.buyOrSellShares(acc.getId(), share1.getId(), 2);
        acc2 = accountDao.buyOrSellShares(acc.getId(), share2.getId(), 20);
        Assert.assertNotNull(acc2);
        stats = accountDao.getStats(acc.getId());

        var stat1 = new UserAccountStats.ShareStat(
                share1.getName(),
                2,
                share1.getPrice() * 2
        );
        var stat2 = new UserAccountStats.ShareStat(
                share2.getName(),
                20,
                share2.getPrice() * 20
        );

        var priceSum = stat1.getOverallPrice() + stat2.getOverallPrice();
        var balance = 10000 - priceSum;

        Assert.assertTrue(
                new UserAccountStats(acc.getId(), List.of(stat1, stat2), balance, priceSum).equals(stats) ||
                new UserAccountStats(acc.getId(), List.of(stat2, stat1), balance, priceSum).equals(stats)
        );
    }

    @Test
    public void testInterruptedTransaction() {
        var share = addShareAndUpdatePrices("TEST", 1000);

        accountDao = Mockito.mock(AccountInMemoryDao.class, Mockito.withSettings()
                .useConstructor(getExchangeUrl()).defaultAnswer(CALLS_REAL_METHODS));

        var acc = accountDao.addAccount();
        Assert.assertNotNull(acc);
        accountDao.addAccountBalance(acc.getId(), 1000);
        acc = accountDao.buyOrSellShares(acc.getId(), share.getId(), 1);
        Assert.assertNotNull(acc);
        share = getShare(share.getId());
        Assert.assertEquals(Integer.valueOf(1), acc.getShares().get(share.getId()));
        Assert.assertEquals(share.getQuantity(), 999);

        doThrow(new RestClientException("mock")).when(accountDao).finishExchangeTransaction(anyInt());

        try {
            accountDao.buyOrSellShares(acc.getId(), share.getId(), 1);
            Assert.fail("Expected exception to be thrown");
        } catch (RestClientException e) {
        }

        var acc2 = accountDao.getAccount(acc.getId());
        Assert.assertEquals(acc, acc2);

        var share2 = getShare(share.getId());
        Assert.assertEquals(share.getQuantity(), share2.getQuantity());

        try {
            accountDao.buyOrSellShares(acc.getId(), share.getId(), -1);
            Assert.fail("Expected exception to be thrown");
        } catch (RestClientException e) {
        }

        acc2 = accountDao.getAccount(acc.getId());
        Assert.assertEquals(acc, acc2);

        share2 = getShare(share.getId());
        Assert.assertEquals(share.getQuantity(), share2.getQuantity());
    }
}
