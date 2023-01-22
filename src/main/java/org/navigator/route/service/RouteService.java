package org.navigator.route.service;

import org.navigator.route.dto.RouteDto;

public interface RouteService {

  RouteDto findBestRoute(String origin, String destination);

}
