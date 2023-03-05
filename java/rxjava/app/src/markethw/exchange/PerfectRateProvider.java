package markethw.exchange;

import org.javamoney.moneta.convert.ExchangeRateBuilder;
import org.javamoney.moneta.spi.AbstractRateProvider;
import org.javamoney.moneta.spi.DefaultNumberValue;

import javax.money.convert.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public class PerfectRateProvider extends AbstractRateProvider {
    private static final Map<String, BigDecimal> BASE_FACTORS = Map.of(
            Currency.RUB.toString(), BigDecimal.ONE,
            Currency.USD.toString(), BigDecimal.valueOf(100),
            Currency.EUR.toString(), BigDecimal.valueOf(100)
    );
    private static final int SCALE = 2;

    private static final ProviderContext CONTEXT =
            ProviderContextBuilder.of("PERFECT", RateType.OTHER)
                    .set("providerDescription", "Perfect Provider").build();

    public PerfectRateProvider() {
        super(CONTEXT);
    }

    private static boolean isCurrencyCodeSupported(String code) {
        return BASE_FACTORS.containsKey(code);
    }

    @Override
    public boolean isAvailable(ConversionQuery conversionQuery) {
        return isCurrencyCodeSupported(conversionQuery.getBaseCurrency().getCurrencyCode()) &&
                isCurrencyCodeSupported(conversionQuery.getCurrency().getCurrencyCode());
    }

    @Override
    public ExchangeRate getExchangeRate(ConversionQuery conversionQuery) {
        if (isAvailable(conversionQuery)) {
            return new ExchangeRateBuilder(getContext().getProviderName(), RateType.OTHER)
                    .setBase(conversionQuery.getBaseCurrency())
                    .setTerm(conversionQuery.getCurrency())
                    .setFactor(DefaultNumberValue.of(
                            BASE_FACTORS.get(conversionQuery.getCurrency().getCurrencyCode())
                                    .setScale(SCALE, RoundingMode.HALF_EVEN)
                                    .divide(
                                            BASE_FACTORS.get(conversionQuery.getBaseCurrency().getCurrencyCode()),
                                            RoundingMode.HALF_EVEN
                                    )
                    )).build();
        }
        return null;
    }


    @Override
    public ExchangeRate getReversed(ExchangeRate rate) {
        if (rate.getContext().getProviderName().equals(CONTEXT.getProviderName())) {
            return new ExchangeRateBuilder(rate.getContext())
                    .setTerm(rate.getBaseCurrency()).setBase(rate.getCurrency())
                    .setFactor(new DefaultNumberValue(BigDecimal.ONE
                            .setScale(SCALE, RoundingMode.HALF_EVEN)
                            .divide(
                                    rate.getFactor().numberValue(BigDecimal.class),
                                    RoundingMode.HALF_EVEN
                            ))
                    ).build();
        }
        return null;
    }
}