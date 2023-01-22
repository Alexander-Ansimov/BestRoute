package org.navigator.route.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.navigator.route.model.Country;
import org.navigator.route.service.CountryService;
import org.springframework.stereotype.Service;

@Service
public class CountryServiceImpl implements CountryService {

  private List<Country> countryList;

  @Override
  public void setCountryList(List<Country> countryList) {
    this.countryList = countryList;
  }

  @Override
  public Map<String, Country> createCountryMap() {
    List<Country> copyCountryList = countryList.stream()
        .map(Country::getNameBorderCopy)
        .toList();
    return initCountryMap(copyCountryList);
  }

  private Map<String, Country> initCountryMap(List<Country> countryList) {
    Map<String, Country> countryMap
        = countryList.stream()
        .collect(Collectors.toMap(Country::getName, country -> country));

    countryMap.values()
        .forEach(country -> {
          country.setIsReached(false);
          if (country.getBorders() != null) {
            country.getBorders()
                .forEach(border -> {
                  if (countryMap.get(border) != null) {
                    if (country.getCommonBorderCountries() == null) {
                      country.setCommonBorderCountries(new ArrayList<>());
                    }
                    country.getCommonBorderCountries().add(countryMap.get(border));
                  }
                });
          }
        });
    return countryMap;
  }

}
