package org.navigator.route.service;

import java.util.List;
import java.util.Map;

import org.navigator.route.model.Country;

public interface CountryService {

  void setCountryList(List<Country> countryList);

  Map<String, Country> createCountryMap();

}
