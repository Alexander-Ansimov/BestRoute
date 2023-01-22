package org.navigator.route.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.navigator.route.model.Country;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CountryLoader {

  @Value("${data.countries}")
  private String sourceFileName;

  private final CountryService countryService;

  @PostConstruct
  public void loadCountries() {
    List<Country> countryList = loadCountriesFromJson();
    log.debug("Country data was loaded from source file: %s; Country count: %s"
        .formatted(sourceFileName, countryList.size()));
    countryService.setCountryList(countryList);
  }

  private List<Country> loadCountriesFromJson() {
    ObjectMapper mapper = new ObjectMapper();
    List<Country> countryList;

    try (InputStream inputStream
             = Thread.currentThread().getContextClassLoader().getResourceAsStream(sourceFileName)) {
      countryList = mapper.readValue(inputStream, new TypeReference<List<Country>>() {
      });
    } catch (IOException e) {
      log.error("Failed to read initial Country data");
      throw new RuntimeException("Failed to read initial Country data");
    }
    return countryList;
  }

}
