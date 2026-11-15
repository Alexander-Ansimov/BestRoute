package org.navigator.route.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.navigator.route.model.Country;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class CountryLoaderTest {

  @Mock
  private CountryService countryService;

  @InjectMocks
  private CountryLoader countryLoader;

  @SuppressWarnings("unchecked")
  private List<Country> loadedCountries() {
    ArgumentCaptor<List<Country>> captor = ArgumentCaptor.forClass(List.class);
    verify(countryService).setCountryList(captor.capture());
    return captor.getValue();
  }

  @Test
  @DisplayName("Loads countries from a valid JSON file and passes them to the service")
  void loadCountries_validFile() {
    ReflectionTestUtils.setField(countryLoader, "sourceFileName", "countries/valid.json");

    countryLoader.loadCountries();

    List<Country> countries = loadedCountries();
    assertEquals(2, countries.size());
    assertEquals("AAA", countries.get(0).getName());
    assertEquals(List.of("BBB"), countries.get(0).getBorders());
    assertEquals("BBB", countries.get(1).getName());
  }

  @Test
  @DisplayName("Loads the real application country data")
  void loadCountries_applicationDataFile() {
    ReflectionTestUtils.setField(countryLoader, "sourceFileName", "country-data.json");

    countryLoader.loadCountries();

    assertTrue(loadedCountries().size() > 100);
  }

  @Test
  @DisplayName("Fails when the JSON file is malformed")
  void loadCountries_malformedFile() {
    ReflectionTestUtils.setField(countryLoader, "sourceFileName", "countries/malformed.json");

    RuntimeException ex = assertThrows(RuntimeException.class, () -> countryLoader.loadCountries());

    assertEquals("Failed to read initial Country data", ex.getMessage());
    verifyNoInteractions(countryService);
  }

  @Test
  @DisplayName("Fails when the JSON file does not exist")
  void loadCountries_missingFile() {
    ReflectionTestUtils.setField(countryLoader, "sourceFileName", "countries/missing.json");

    assertThrows(RuntimeException.class, () -> countryLoader.loadCountries());
    verifyNoInteractions(countryService);
  }

  @Test
  @DisplayName("Propagates failure of the country service")
  void loadCountries_serviceFails() {
    ReflectionTestUtils.setField(countryLoader, "sourceFileName", "countries/valid.json");
    doThrow(new IllegalStateException("boom")).when(countryService).setCountryList(any());

    assertThrows(IllegalStateException.class, () -> countryLoader.loadCountries());
  }

}
