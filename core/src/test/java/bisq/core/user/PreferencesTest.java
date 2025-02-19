/*
 * This file is part of Bisq.
 *
 * Bisq is free software: you can redistribute it and/or modify it
 * under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at
 * your option) any later version.
 *
 * Bisq is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public
 * License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Bisq. If not, see <http://www.gnu.org/licenses/>.
 */

package bisq.core.user;

import bisq.core.btc.nodes.LocalBitcoinNode;
import bisq.core.locale.CountryUtil;
import bisq.core.locale.CryptoCurrency;
import bisq.core.locale.CurrencyUtil;
import bisq.core.locale.FiatCurrency;
import bisq.core.locale.GlobalSettings;
import bisq.core.locale.Res;

import bisq.common.config.Config;
import bisq.common.persistence.PersistenceManager;

import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PreferencesTest {

    private Preferences preferences;
    private PersistenceManager persistenceManager;

    @BeforeEach
    public void setUp() {
        final Locale en_US = new Locale("en", "US");
        Locale.setDefault(en_US);
        GlobalSettings.setLocale(en_US);
        Res.setBaseCurrencyCode("BTC");
        Res.setBaseCurrencyName("Bitcoin");

        persistenceManager = mock(PersistenceManager.class);
        Config config = new Config();
        LocalBitcoinNode localBitcoinNode = new LocalBitcoinNode(config);
        preferences = new Preferences(
                persistenceManager, config, null, localBitcoinNode, null, null, Config.DEFAULT_FULL_DAO_NODE,
                null, null, Config.UNSPECIFIED_PORT);
    }

    @Test
    public void testAddFiatCurrency() {
        final FiatCurrency usd = new FiatCurrency("USD");
        final FiatCurrency usd2 = new FiatCurrency("USD");
        final ObservableList<FiatCurrency> fiatCurrencies = preferences.getFiatCurrenciesAsObservable();

        preferences.addFiatCurrency(usd);

        assertEquals(1, fiatCurrencies.size());

        preferences.addFiatCurrency(usd2);

        assertEquals(1, fiatCurrencies.size());
    }

    @Test
    public void testGetUniqueListOfFiatCurrencies() {
        PreferencesPayload payload = mock(PreferencesPayload.class);

        List<FiatCurrency> fiatCurrencies = CurrencyUtil.getMainFiatCurrencies();
        final FiatCurrency usd = new FiatCurrency("USD");
        fiatCurrencies.add(usd);

        when(persistenceManager.getPersisted(anyString())).thenReturn(payload);
        when(payload.getUserLanguage()).thenReturn("en");
        when(payload.getUserCountry()).thenReturn(CountryUtil.getDefaultCountry());
        when(payload.getPreferredTradeCurrency()).thenReturn(usd);
        when(payload.getFiatCurrencies()).thenReturn(fiatCurrencies);

        preferences.readPersisted(() -> {
            assertEquals(7, preferences.getFiatCurrenciesAsObservable().size());
            assertTrue(preferences.getFiatCurrenciesAsObservable().contains(usd));
        });
    }

    @Test
    public void testGetUniqueListOfCryptoCurrencies() {
        PreferencesPayload payload = mock(PreferencesPayload.class);

        List<CryptoCurrency> cryptoCurrencies = CurrencyUtil.getMainCryptoCurrencies();
        final CryptoCurrency dash = new CryptoCurrency("DASH", "Dash");
        cryptoCurrencies.add(dash);

        when(persistenceManager.getPersisted(anyString())).thenReturn(payload);
        when(payload.getUserLanguage()).thenReturn("en");
        when(payload.getUserCountry()).thenReturn(CountryUtil.getDefaultCountry());
        when(payload.getPreferredTradeCurrency()).thenReturn(new FiatCurrency("USD"));
        when(payload.getCryptoCurrencies()).thenReturn(cryptoCurrencies);

        preferences.readPersisted(() -> {
            assertTrue(preferences.getCryptoCurrenciesAsObservable().contains(dash));
        });
    }

    @Test
    public void testUpdateOfPersistedFiatCurrenciesAfterLocaleChanged() {
        PreferencesPayload payload = mock(PreferencesPayload.class);

        List<FiatCurrency> fiatCurrencies = new ArrayList<>();
        final FiatCurrency usd = new FiatCurrency(Currency.getInstance("USD"), new Locale("de", "AT"));
        fiatCurrencies.add(usd);

        assertEquals("US-Dollar (USD)", usd.getNameAndCode());

        when(persistenceManager.getPersisted(anyString())).thenReturn(payload);
        when(payload.getUserLanguage()).thenReturn("en");
        when(payload.getUserCountry()).thenReturn(CountryUtil.getDefaultCountry());
        when(payload.getPreferredTradeCurrency()).thenReturn(usd);
        when(payload.getFiatCurrencies()).thenReturn(fiatCurrencies);

        preferences.readPersisted(() -> {
            assertEquals("US Dollar (USD)", preferences.getFiatCurrenciesAsObservable().get(0).getNameAndCode());
        });
    }
}
