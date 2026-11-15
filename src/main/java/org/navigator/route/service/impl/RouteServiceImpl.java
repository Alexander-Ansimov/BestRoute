package org.navigator.route.service.impl;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

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

    Deque<String> bestRoute = findShortestRoute(countries, origin, destination);

    log.debug("Best route is found: {}", bestRoute);
    return new RouteDto(bestRoute);
  }

  /**
   * Breadth-first search: routes are expanded one border at a time, so the first route whose last
   * country borders the destination is the shortest one.
   */
  private Deque<String> findShortestRoute(Map<String, Country> countries,
                                          String origin,
                                          String destination) {
    Set<String> visited = new HashSet<>(Set.of(origin));
    Queue<Deque<String>> routes = new ArrayDeque<>();
    routes.add(new LinkedList<>(List.of(origin)));

    while (!routes.isEmpty()) {
      Deque<String> route = routes.poll();
      List<String> borders = countries.get(route.getLast()).getBorders();

      if (borders.contains(destination)) {
        route.addLast(destination);
        return route;
      }
      for (String border : borders) {
        // Borders missing from the data set are ignored.
        if (countries.containsKey(border) && visited.add(border)) {
          Deque<String> extendedRoute = new LinkedList<>(route);
          extendedRoute.addLast(border);
          routes.add(extendedRoute);
        }
      }
    }
    throw new NoLandCrossingException("Failed to find lend crossing to destination");
  }

  private void validate(Map<String, Country> countries, String origin, String destination) {
    if (!countries.containsKey(origin) || !countries.containsKey(destination)) {
      log.warn("Provided 'origin' and/or 'destination' are unknown");
      throw new InvalidParameterException("Provided 'origin' and/or 'destination' are unknown");
    }
    if (origin.equals(destination)) {
      log.warn("'origin' and 'destination' should not be the same");
      throw new InvalidParameterException("'origin' and 'destination' should not be the same");
    }
  }

}
