package ru.akirakozov.sd.app.exchange.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.akirakozov.sd.app.exchange.dao.ExchangeDao;
import ru.akirakozov.sd.app.exchange.dao.ExchangeInMemoryDao;
import ru.akirakozov.sd.app.exchange.model.RandomPriceGetter;

@Configuration
public class InMemoryDaoContextConfiguration {
    @Bean
    public ExchangeDao exchangeDao() {
        return new ExchangeInMemoryDao(new RandomPriceGetter(10, 20));
    }
}
