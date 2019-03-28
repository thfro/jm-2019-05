package com.frotscher.demo.googleclient;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties({"formatted_address", "partial_match", "place_id", "types", "partial_match"})
public class GeoLocation {

    @JsonProperty("address_components")
    private List<AddressComponent> addressComponents;

    private Geometry geometry;


    public List<AddressComponent> getAddressComponents() {
        return addressComponents;
    }

    public void setAddressComponents(List<AddressComponent> addressComponents) {
        this.addressComponents = addressComponents;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
}