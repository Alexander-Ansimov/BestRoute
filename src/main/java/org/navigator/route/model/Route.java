package org.navigator.route.model;

import java.util.Deque;
import java.util.LinkedList;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
public class Route {

  private Deque<String> route;

  public Route() {
  }

  public Route(String originCountryName, String nextCountryName) {
    this.route = new LinkedList<>();
    this.route.add(originCountryName);
    this.route.addLast(nextCountryName);
  }

  public Route(Route route, String nextCountry) {
    this.route = new LinkedList<>(route.getRoute());
    this.route.addLast(nextCountry);
  }

  public void addNextCountry(String nextCountryName) {
    route.addLast(nextCountryName);
  }

}
