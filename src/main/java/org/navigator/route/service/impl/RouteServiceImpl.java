package org.navigator.route.service.impl;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.navigator.route.dto.RouteDto;
import org.navigator.route.exception.InvalidParameterException;
import org.navigator.route.exception.NoLandCrossingException;
import org.navigator.route.model.Country;
import org.navigator.route.model.Route;
import org.navigator.route.service.CountryService;
import org.navigator.route.service.RouteService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RouteServiceImpl implements RouteService {

  private final CountryService countryService;

  @Override
  public RouteDto findBestRoute(String origin, String destination) {
    Map<String, Country> countryMap = countryService.createCountryMap();
    validate(countryMap, origin, destination);

    Route bestRoute = searchForBestRoute(countryMap, origin, destination);

    log.debug("Best route is found: " + bestRoute);
    return new RouteDto(bestRoute.getRoute());
  }

  private Route searchForBestRoute(Map<String, Country> countryMap,
                                   String origin,
                                   String destination) {
    Country originCountry = countryMap.get(origin);
    if (originCountry.getBorders().contains(destination)) {
      return new Route(origin, destination);
    }

    originCountry.setIsReached(true);
    List<Country> nonReachedCountries = originCountry.getAllNonReachedCountry();
    if (nonReachedCountries == null) {
      throw new NoLandCrossingException("Failed to find lend crossing to destination");
    }
    List<Route> initialRouteList = nonReachedCountries.stream()
        .map(nonReachedCountry -> new Route(originCountry.getName(), nonReachedCountry.getName()))
        .toList();
    return handleNextLevel(initialRouteList, countryMap, destination);
  }

  private Route handleNextLevel(List<Route> currentRouteList,
                               Map<String, Country> countryMap,
                               String destination) {
    Route resultRoute = new Route();

    List<Route> newRoutes = currentRouteList.stream()
        .map(route -> {
          Country currentCountry = countryMap.get(route.getRoute().getLast());
          List<Country> nonReachedCountries = currentCountry.getAllNonReachedCountry();

          if (currentCountry.getBorders() == null || nonReachedCountries == null
              || resultRoute.getRoute() != null) {
            return null;
          }
          if (currentCountry.getBorders().contains(destination)) {
            route.addNextCountry(destination);
            resultRoute.setRoute(new LinkedList<>(route.getRoute()));
            return null;
          }

          return nonReachedCountries.stream().map(country -> {
            country.setIsReached(true);
            return new Route(route, country.getName());
          }).toList();
        })
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .toList();

    if (resultRoute.getRoute() != null) {
      return resultRoute;
    }
    if (newRoutes.isEmpty()) {
      throw new NoLandCrossingException("Failed to find lend crossing to destination");
    }

    return handleNextLevel(newRoutes, countryMap, destination);
  }

  private void validate(Map<String, Country> countryMap, String origin, String destination) {
    if (!countryMap.containsKey(origin) || !countryMap.containsKey(destination)) {
      log.warn("Provided 'origin' and/or 'destination' are unknown");
      throw new InvalidParameterException("Provided 'origin' and/or 'destination' are unknown");
    }
    if (origin.equals(destination)) {
      log.warn("'origin' and 'destination' should not be the same");
      throw new InvalidParameterException("'origin' and 'destination' should not be the same");
    }
  }

}
