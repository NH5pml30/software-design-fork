package ru.akirakozov.sd.app.exchange.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.akirakozov.sd.app.exchange.dao.ExchangeDao;
import ru.akirakozov.sd.app.shared.model.Share;
import ru.akirakozov.sd.app.shared.model.TransactionInfo;

@RestController
public class ExchangeController {
    private final ExchangeDao exchangeDao;

    public ExchangeController(ExchangeDao exchangeDao) {
        this.exchangeDao = exchangeDao;
    }

    @RequestMapping(value = "/add-share", method = RequestMethod.POST)
    public Share addShare(@RequestParam String shareName) {
        return exchangeDao.addShare(shareName);
    }

    @RequestMapping("/get-share")
    public Share getShare(@RequestParam int shareId) {
        return exchangeDao.getShare(shareId);
    }

    @RequestMapping(value = "/add-quantity", method = RequestMethod.POST)
    public Share addQuantity(@RequestParam int shareId, @RequestParam int howMuch) {
        return exchangeDao.addShareQuantity(shareId, howMuch);
    }

    @RequestMapping(value = "/update-prices", method = RequestMethod.PUT)
    public void updatePrices() {
        exchangeDao.updatePrices();
    }

    @RequestMapping(value = "/start-transaction", method = RequestMethod.POST)
    public TransactionInfo startTransaction(@RequestParam int shareId, @RequestParam int howMuch) {
        return exchangeDao.startTransaction(shareId, howMuch);
    }

    @RequestMapping(value = "/finish-transaction", method = RequestMethod.POST)
    public void finishTransaction(@RequestParam int transactionId) {
        exchangeDao.finishTransaction(transactionId);
    }

    @RequestMapping(value = "/cancel-transaction", method = RequestMethod.PUT)
    public void cancelTransaction(@RequestParam int transactionId) {
        exchangeDao.cancelTransaction(transactionId);
    }
}
