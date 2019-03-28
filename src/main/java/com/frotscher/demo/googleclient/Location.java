package com.frotscher.demo.googleclient;

public class Location {

	private float lat;
	private float lng;
	
	public float getLat() {
		return lat;
	}
	
	public void setLat(float lat) {
		this.lat = lat;
	}
	
	public float getLng() {
		return lng;
	}
	
	public void setLng(float lng) {
		this.lng = lng;
	}


	public static Location of(float lat, float lng) {
		Location location = new Location();
		location.setLat(lat);
		location.setLng(lng);
		return location;
	}

	public String toString() {
		return lat + "," + lng;
	}
}