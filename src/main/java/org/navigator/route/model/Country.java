package org.navigator.route.model;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class Country {

  @JsonProperty("cca3")
  private String name;
  @JsonProperty("borders")
  private List<String> borders;

  private List<Country> commonBorderCountries;
  private Boolean isReached;

  public List<Country> getAllNonReachedCountry() {
    if (commonBorderCountries != null) {
      return commonBorderCountries.stream()
          .filter(c -> !c.getIsReached())
          .toList();
    }
    return null;
  }

  public Country getNameBorderCopy() {
    Country country = new Country();
    country.setName(this.name);
    country.setBorders(new ArrayList<>(borders));
    return country;
  }

}
