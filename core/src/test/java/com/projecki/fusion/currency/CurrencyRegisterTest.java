package com.projecki.fusion.currency;

import com.projecki.fusion.currency.storage.CurrencyStorage;
import com.projecki.fusion.currency.storage.SqlCurrencyStorage;
import com.projecki.fusion.sql.SqlConnectionPool;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@ExtendWith(MockitoExtension.class)
public class CurrencyRegisterTest {

    @Mock
    CurrencyStorage currencyStorage;

    @BeforeEach
    void init (@Mock SqlConnectionPool connectionPool) {
        currencyStorage = new SqlCurrencyStorage("balances", connectionPool);
    }

    @Test
    void currencyRegisterTest () {

        Currency currency = new BasicCurrency("test", "coin", "coins", currencyStorage);
        Currency currency2 = new BasicCurrency("test2", "dollar", "dollars", currencyStorage);

        CurrencyRegister.registerCurrency(currency);
        CurrencyRegister.registerCurrency(currency2);

        Assertions.assertNotNull(CurrencyRegister.getCurrency("test").orElse(null));
        Assertions.assertNotNull(CurrencyRegister.getCurrency("coin").orElse(null));
        Assertions.assertNotNull(CurrencyRegister.getCurrency("coins").orElse(null));
        Assertions.assertNotNull(CurrencyRegister.getCurrency("test2").orElse(null));
        Assertions.assertNotNull(CurrencyRegister.getCurrency("dollar").orElse(null));
        Assertions.assertNotNull(CurrencyRegister.getCurrency("dollars").orElse(null));
    }


}
