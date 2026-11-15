package org.navigator.route.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.navigator.route.model.Country;

class CountryServiceImplTest {

  private CountryServiceImpl countryService;

  @BeforeEach
  void setUp() {
    countryService = new CountryServiceImpl();
  }

  private static Country country(String name, String... borders) {
    Country country = new Country();
    country.setName(name);
    country.setBorders(List.of(borders));
    return country;
  }

  @Test
  @DisplayName("Creates map keyed by country name")
  void createCountryMap_returnsAllCountriesByName() {
    countryService.setCountryList(List.of(country("AAA", "BBB"), country("BBB", "AAA")));

    Map<String, Country> result = countryService.createCountryMap();

    assertEquals(Set.of("AAA", "BBB"), result.keySet());
    assertEquals("AAA", result.get("AAA").getName());
    assertEquals(List.of("BBB"), result.get("AAA").getBorders());
  }

  @Test
  @DisplayName("Links neighbours that exist in the list and marks all countries as not reached")
  void createCountryMap_linksCommonBorderCountries() {
    countryService.setCountryList(List.of(
        country("AAA", "BBB", "CCC"),
        country("BBB", "AAA"),
        country("CCC", "AAA")));

    Map<String, Country> result = countryService.createCountryMap();

    Country a = result.get("AAA");
    assertEquals(2, a.getCommonBorderCountries().size());
    assertTrue(a.getCommonBorderCountries().contains(result.get("BBB")));
    assertTrue(a.getCommonBorderCountries().contains(result.get("CCC")));
    assertSame(a, result.get("BBB").getCommonBorderCountries().get(0));
    result.values().forEach(c -> assertFalse(c.getIsReached()));
  }

  @Test
  @DisplayName("Ignores borders pointing to unknown countries")
  void createCountryMap_ignoresUnknownBorders() {
    countryService.setCountryList(List.of(country("AAA", "ZZZ", "BBB"), country("BBB", "AAA")));

    Map<String, Country> result = countryService.createCountryMap();

    assertEquals(1, result.get("AAA").getCommonBorderCountries().size());
    assertEquals("BBB", result.get("AAA").getCommonBorderCountries().get(0).getName());
    assertEquals(List.of("ZZZ", "BBB"), result.get("AAA").getBorders());
  }

  @Test
  @DisplayName("Leaves common borders empty when all borders are unknown")
  void createCountryMap_allBordersUnknown() {
    countryService.setCountryList(List.of(country("AAA", "ZZZ")));

    Map<String, Country> result = countryService.createCountryMap();

    assertNull(result.get("AAA").getCommonBorderCountries());
    assertNull(result.get("AAA").getAllNonReachedCountry());
  }

  @Test
  @DisplayName("Island without borders has no common border countries")
  void createCountryMap_islandWithoutBorders() {
    countryService.setCountryList(List.of(country("ISL")));

    Map<String, Country> result = countryService.createCountryMap();

    assertTrue(result.get("ISL").getBorders().isEmpty());
    assertNull(result.get("ISL").getCommonBorderCountries());
    assertFalse(result.get("ISL").getIsReached());
  }

  @Test
  @DisplayName("Returns empty map for empty country list")
  void createCountryMap_emptyList() {
    countryService.setCountryList(List.of());

    assertTrue(countryService.createCountryMap().isEmpty());
  }

  @Test
  @DisplayName("Each call returns fresh copies so state changes do not leak between calls")
  void createCountryMap_returnsIndependentCopies() {
    Country source = country("AAA", "BBB");
    countryService.setCountryList(List.of(source, country("BBB", "AAA")));

    Map<String, Country> first = countryService.createCountryMap();
    first.get("AAA").setIsReached(true);
    Map<String, Country> second = countryService.createCountryMap();

    assertNotSame(first.get("AAA"), second.get("AAA"));
    assertFalse(second.get("AAA").getIsReached());
    assertNull(source.getCommonBorderCountries());
    assertNull(source.getIsReached());
  }

  @Test
  @DisplayName("Replaces previously set country list")
  void setCountryList_replacesPreviousList() {
    countryService.setCountryList(List.of(country("AAA")));
    countryService.setCountryList(List.of(country("BBB")));

    Map<String, Country> result = countryService.createCountryMap();

    assertEquals(Set.of("BBB"), result.keySet());
    assertNotNull(result.get("BBB"));
  }

  @Test
  @DisplayName("Fails when country list was never set")
  void createCountryMap_listNotSet_throws() {
    assertThrows(NullPointerException.class, () -> countryService.createCountryMap());
  }

  @Test
  @DisplayName("Fails when country has null borders")
  void createCountryMap_nullBorders_throws() {
    Country broken = new Country();
    broken.setName("AAA");
    countryService.setCountryList(List.of(broken));

    assertThrows(NullPointerException.class, () -> countryService.createCountryMap());
  }

  @Test
  @DisplayName("Fails on duplicate country names")
  void createCountryMap_duplicateNames_throws() {
    countryService.setCountryList(List.of(country("AAA"), country("AAA")));

    assertThrows(IllegalStateException.class, () -> countryService.createCountryMap());
  }

}
