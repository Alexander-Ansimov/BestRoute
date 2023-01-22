package org.navigator.route.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.navigator.route.dto.RouteDto;
import org.navigator.route.service.RouteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/routing")
@RequiredArgsConstructor
public class RouteController {

  private final RouteService routeService;

  @GetMapping("/{origin}/{destination}")
  public RouteDto getRoute(@PathVariable String origin,
                           @PathVariable String destination) {
    log.debug("Got request with origin: %s, and destination: %s".formatted(origin, destination));
    RouteDto responseRoute = routeService.findBestRoute(origin, destination);
    log.debug("Response route: " + responseRoute);
    return responseRoute;
  }
}
