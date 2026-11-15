package org.navigator.route.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.navigator.route.dto.RouteDto;
import org.navigator.route.exception.InvalidParameterException;
import org.navigator.route.exception.NoLandCrossingException;
import org.navigator.route.model.Country;
import org.navigator.route.service.CountryService;

@ExtendWith(MockitoExtension.class)
class RouteServiceImplTest {

  @Mock
  private CountryService countryService;

  @InjectMocks
  private RouteServiceImpl routeService;

  private CountryServiceImpl mapBuilder;

  @BeforeEach
  void setUp() {
    mapBuilder = new CountryServiceImpl();
  }

  private static Country country(String name, String... borders) {
    Country country = new Country();
    country.setName(name);
    country.setBorders(List.of(borders));
    return country;
  }

  private void givenCountries(Country... countries) {
    mapBuilder.setCountryList(List.of(countries));
    Map<String, Country> countryMap = mapBuilder.createCountryMap();
    when(countryService.createCountryMap()).thenReturn(countryMap);
  }

  private static List<String> asList(RouteDto dto) {
    return List.copyOf(dto.getRoute());
  }

  @Test
  @DisplayName("Returns direct route when countries share a border")
  void findBestRoute_neighbours() {
    givenCountries(country("AAA", "BBB"), country("BBB", "AAA"));

    RouteDto result = routeService.findBestRoute("AAA", "BBB");

    assertEquals(List.of("AAA", "BBB"), asList(result));
    verify(countryService).createCountryMap();
  }

  @Test
  @DisplayName("Returns route through one intermediate country")
  void findBestRoute_oneIntermediateCountry() {
    givenCountries(
        country("AAA", "BBB"),
        country("BBB", "AAA", "CCC"),
        country("CCC", "BBB"));

    RouteDto result = routeService.findBestRoute("AAA", "CCC");

    assertEquals(List.of("AAA", "BBB", "CCC"), asList(result));
  }

  @Test
  @DisplayName("Route works in the reverse direction")
  void findBestRoute_reverseDirection() {
    givenCountries(
        country("AAA", "BBB"),
        country("BBB", "AAA", "CCC"),
        country("CCC", "BBB"));

    RouteDto result = routeService.findBestRoute("CCC", "AAA");

    assertEquals(List.of("CCC", "BBB", "AAA"), asList(result));
  }

  @Test
  @DisplayName("Chooses the shortest route among several alternatives")
  void findBestRoute_choosesShortestRoute() {
    givenCountries(
        country("AAA", "BBB", "EEE"),
        country("BBB", "AAA", "CCC"),
        country("CCC", "BBB", "DDD"),
        country("DDD", "CCC", "EEE"),
        country("EEE", "AAA", "DDD"));

    RouteDto result = routeService.findBestRoute("AAA", "DDD");

    assertEquals(List.of("AAA", "EEE", "DDD"), asList(result));
  }

  @Test
  @DisplayName("Finds a long route along a chain of countries")
  void findBestRoute_longChain() {
    givenCountries(
        country("C1", "C2"),
        country("C2", "C1", "C3"),
        country("C3", "C2", "C4"),
        country("C4", "C3", "C5"),
        country("C5", "C4"));

    RouteDto result = routeService.findBestRoute("C1", "C5");

    assertEquals(List.of("C1", "C2", "C3", "C4", "C5"), asList(result));
  }

  @Test
  @DisplayName("Handles cycles between countries without looping")
  void findBestRoute_cycle() {
    givenCountries(
        country("AAA", "BBB", "CCC"),
        country("BBB", "AAA", "CCC"),
        country("CCC", "AAA", "BBB", "DDD"),
        country("DDD", "CCC"));

    RouteDto result = routeService.findBestRoute("AAA", "DDD");

    assertEquals(List.of("AAA", "CCC", "DDD"), asList(result));
  }

  @Test
  @DisplayName("Returns the first found route when several routes have equal length")
  void findBestRoute_equalLengthRoutes() {
    givenCountries(
        country("AAA", "BBB", "CCC"),
        country("BBB", "AAA", "DDD"),
        country("CCC", "AAA", "DDD"),
        country("DDD", "BBB", "CCC"));

    RouteDto result = routeService.findBestRoute("AAA", "DDD");

    assertEquals(3, asList(result).size());
    assertEquals("AAA", asList(result).get(0));
    assertEquals("DDD", asList(result).get(2));
  }

  @Test
  @DisplayName("Ignores borders with countries missing in the data")
  void findBestRoute_unknownBorderIgnored() {
    givenCountries(
        country("AAA", "ZZZ", "BBB"),
        country("BBB", "AAA", "CCC"),
        country("CCC", "BBB"));

    RouteDto result = routeService.findBestRoute("AAA", "CCC");

    assertEquals(List.of("AAA", "BBB", "CCC"), asList(result));
  }

  @Test
  @DisplayName("Throws when origin is unknown")
  void findBestRoute_unknownOrigin() {
    givenCountries(country("AAA", "BBB"), country("BBB", "AAA"));

    InvalidParameterException ex = assertThrows(InvalidParameterException.class,
        () -> routeService.findBestRoute("XXX", "BBB"));

    assertEquals("Provided 'origin' and/or 'destination' are unknown", ex.getMessage());
  }

  @Test
  @DisplayName("Throws when destination is unknown")
  void findBestRoute_unknownDestination() {
    givenCountries(country("AAA", "BBB"), country("BBB", "AAA"));

    assertThrows(InvalidParameterException.class,
        () -> routeService.findBestRoute("AAA", "XXX"));
  }

  @Test
  @DisplayName("Throws when both origin and destination are unknown")
  void findBestRoute_bothUnknown() {
    givenCountries(country("AAA", "BBB"), country("BBB", "AAA"));

    assertThrows(InvalidParameterException.class,
        () -> routeService.findBestRoute("XXX", "YYY"));
  }

  @Test
  @DisplayName("Throws when origin or destination is null")
  void findBestRoute_nullParameters() {
    givenCountries(country("AAA", "BBB"), country("BBB", "AAA"));

    assertThrows(InvalidParameterException.class, () -> routeService.findBestRoute(null, "BBB"));
    assertThrows(InvalidParameterException.class, () -> routeService.findBestRoute("AAA", null));
  }

  @Test
  @DisplayName("Throws when origin equals destination")
  void findBestRoute_sameOriginAndDestination() {
    givenCountries(country("AAA", "BBB"), country("BBB", "AAA"));

    InvalidParameterException ex = assertThrows(InvalidParameterException.class,
        () -> routeService.findBestRoute("AAA", "AAA"));

    assertEquals("'origin' and 'destination' should not be the same", ex.getMessage());
  }

  @Test
  @DisplayName("Throws when no country data is available")
  void findBestRoute_emptyCountryMap() {
    givenCountries();

    assertThrows(InvalidParameterException.class, () -> routeService.findBestRoute("AAA", "BBB"));
  }

  @Test
  @DisplayName("Throws when origin is an island without any land borders")
  void findBestRoute_originIsland() {
    givenCountries(country("ISL"), country("AAA", "BBB"), country("BBB", "AAA"));

    NoLandCrossingException ex = assertThrows(NoLandCrossingException.class,
        () -> routeService.findBestRoute("ISL", "AAA"));

    assertEquals("Failed to find lend crossing to destination", ex.getMessage());
  }

  @Test
  @DisplayName("Throws when origin only borders countries missing in the data")
  void findBestRoute_originBordersOnlyUnknown() {
    givenCountries(country("AAA", "ZZZ"), country("BBB", "AAA"));

    assertThrows(NoLandCrossingException.class, () -> routeService.findBestRoute("AAA", "BBB"));
  }

  @Test
  @DisplayName("Throws when destination is an island unreachable by land")
  void findBestRoute_destinationIsland() {
    givenCountries(
        country("AAA", "BBB"),
        country("BBB", "AAA"),
        country("ISL"));

    assertThrows(NoLandCrossingException.class, () -> routeService.findBestRoute("AAA", "ISL"));
  }

  @Test
  @DisplayName("Throws when countries belong to disconnected land masses")
  void findBestRoute_disconnectedLandMasses() {
    givenCountries(
        country("AAA", "BBB"),
        country("BBB", "AAA"),
        country("CCC", "DDD"),
        country("DDD", "CCC"));

    assertThrows(NoLandCrossingException.class, () -> routeService.findBestRoute("AAA", "CCC"));
  }

  @Test
  @DisplayName("Throws when a reachable country is a dead end (asymmetric border data)")
  void findBestRoute_deadEnd() {
    givenCountries(
        country("AAA", "BBB"),
        country("BBB"),
        country("CCC"));

    assertThrows(NoLandCrossingException.class, () -> routeService.findBestRoute("AAA", "CCC"));
  }

  @Test
  @DisplayName("Propagates failure of the country service")
  void findBestRoute_countryServiceFails() {
    when(countryService.createCountryMap()).thenThrow(new IllegalStateException("no data"));

    assertThrows(IllegalStateException.class, () -> routeService.findBestRoute("AAA", "BBB"));
  }

  @Test
  @DisplayName("Repeated searches are independent of each other")
  void findBestRoute_repeatedCalls() {
    mapBuilder.setCountryList(List.of(
        country("AAA", "BBB"),
        country("BBB", "AAA", "CCC"),
        country("CCC", "BBB")));
    when(countryService.createCountryMap()).thenAnswer(inv -> mapBuilder.createCountryMap());

    RouteDto first = routeService.findBestRoute("AAA", "CCC");
    RouteDto second = routeService.findBestRoute("AAA", "CCC");

    assertEquals(asList(first), asList(second));
  }

}
