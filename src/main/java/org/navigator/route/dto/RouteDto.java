package org.navigator.route.dto;

import java.util.Queue;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@ToString
@Setter
@Getter
@AllArgsConstructor
public class RouteDto {

  private Queue<String> route;
}
