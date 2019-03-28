package com.frotscher.demo.googleclient;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AddressComponent {

	public static final String TYPE_ROUTE = "route";								               // An der Welle
	public static final String TYPE_STREET_NUMBER = "street_number";				       // 22
	public static final String TYPE_POSTAL_CODE = "postal_code";					         // 60322
	public static final String TYPE_LOCALITY = "locality";							           // Frankfurt am Main
	public static final String TYPE_SUB_LOCALITY_LEVEL_1 = "sublocality_level_1";	 // Innenstadt
	public static final String TYPE_SUB_LOCALITY_LEVEL_2 = "sublocality_level_2";	 // Westend-Süd
	public static final String TYPE_ADMIN_LEVEL_1 = "administrative_area_level_1"; // Hessen
	public static final String TYPE_COUNTRY = "country";							             // Deutschland

	@JsonProperty("long_name")
	private String longName;

	@JsonProperty("short_name")
	private String shortName;

	private List<String> types;


	public String getLongName() {
		return longName;
	}

	public void setLongName(String longName) {
		this.longName = longName;
	}

	public String getShortName() {
		return shortName;
	}

	public void setShortName(String shortName) {
		this.shortName = shortName;
	}

	public List<String> getTypes() {
		return types;
	}

	public void setTypes(List<String> types) {
		this.types = types;
	}
}
