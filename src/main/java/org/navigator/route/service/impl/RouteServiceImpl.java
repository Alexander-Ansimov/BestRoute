package org.navigator.route.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.navigator.route.dto.RouteDto;
import org.navigator.route.exception.InvalidParameterException;
import org.navigator.route.exception.NoLandCrossingException;
import org.navigator.route.model.Country;
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
    Map<String, Country> countries = countryService.createCountryMap();
    validate(countries, origin, destination);

    Queue<String> route = findShortestRoute(countries, origin, destination);
    log.debug("Best route is found: {}", route);
    return new RouteDto(route);
  }

  /**
   * Breadth-first search over land borders. Routes are extended one level
   * (one country longer) at a time, so the first route that reaches the
   * destination is a shortest one.
   */
  private Queue<String> findShortestRoute(Map<String, Country> countries,
                                          String origin,
                                          String destination) {
    Country originCountry = countries.get(origin);
    if (originCountry.getBorders().contains(destination)) {
      return pathOf(origin, destination);
    }
    originCountry.setIsReached(true);

    List<LinkedList<String>> level = new ArrayList<>();
    for (Country neighbour : unvisitedNeighbours(originCountry)) {
      level.add(pathOf(origin, neighbour.getName()));
    }

    while (!level.isEmpty()) {
      List<LinkedList<String>> nextLevel = new ArrayList<>();
      for (LinkedList<String> route : level) {
        Country last = countries.get(route.getLast());
        if (last.getBorders() == null) {
          continue;
        }
        if (last.getBorders().contains(destination)) {
          route.addLast(destination);
          return route;
        }
        for (Country neighbour : unvisitedNeighbours(last)) {
          neighbour.setIsReached(true);
          LinkedList<String> extended = new LinkedList<>(route);
          extended.addLast(neighbour.getName());
          nextLevel.add(extended);
        }
      }
      level = nextLevel;
    }

    throw new NoLandCrossingException("Failed to find lend crossing to destination");
  }

  private static List<Country> unvisitedNeighbours(Country country) {
    List<Country> neighbours = country.getAllNonReachedCountry();
    return neighbours == null ? List.of() : neighbours;
  }

  private static LinkedList<String> pathOf(String... countryNames) {
    return new LinkedList<>(Arrays.asList(countryNames));
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
