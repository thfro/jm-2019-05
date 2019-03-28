package com.frotscher.demo.javaee8;

import com.frotscher.demo.config.ConfigurationKeys;
import com.frotscher.demo.googleclient.GeoLocation;
import com.frotscher.demo.googleclient.GoogleApiResponse;
import com.frotscher.demo.googleclient.OverQueryLimitException;
import com.frotscher.demo.googleclient.Place;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.deltaspike.testcontrol.api.junit.CdiTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutorService;
import java.util.logging.Logger;

import static com.frotscher.demo.googleclient.GoogleApiResponse.STATUS_INVALID_REQUEST;
import static com.frotscher.demo.googleclient.GoogleApiResponse.STATUS_OVER_QUERY_LIMIT;
import static com.frotscher.demo.googleclient.GoogleApiResponse.STATUS_REQUEST_DENIED;
import static com.frotscher.demo.googleclient.GoogleApiResponse.STATUS_UNKNOWN_ERROR;
import static org.junit.Assert.fail;

/**
 *
 * For demonstration purposes only.
 *
 */
@RunWith(CdiTestRunner.class)
public class ReactiveClient2 {

	private final static Logger log = Logger.getLogger(ReactiveClient2.class.getName());

	@Inject
	private Configuration config;

	// custom thread pool for JAX-RS requests to Google API
	@Inject
	private ExecutorService myExecutorService;

	private String address = "Opernplatz, Frankfurt";


	@Before
	public void checkConfiguration() {
		if (StringUtils.isBlank(config.getString(ConfigurationKeys.GOOGLE_APIKEY))) {
			fail("Missing configuration parameter " + ConfigurationKeys.GOOGLE_APIKEY + " in config.properties.");
		}
	}

	/**
	 * Find the closest train stations asynchronously using multiple completion stages, while doing
	 * something else in the main thread.
	 *
	 * In this example the actual logic of each stage is implemented in separate methods, resulting in
	 * code that is better readable than the example in class ReactiveClient
	 *
	 */
	@Test
	public void testOneCompletionStage() {
		geolocateAddressViaGoogle(address)
			.thenApply(this::extractGeoLocation)
			.thenCompose(location -> findPlace(location, "train_station"))
			.thenApply(this::extractPlace)
			.thenAccept(this::printResult)
			.exceptionally( t -> {log.severe("An error occurred: " + t.getMessage()); return null;})
			.toCompletableFuture()
		  .join();

		System.out.println("Done");
	}

	/**
	 * Concurrently find the closest train station AND the closest subway station in two separate completion stages,
	 * while doing something else in the main thread
	 */
	@Test
	public void testTwoCompletionStages() throws InterruptedException {

		CompletionStage<GeoLocation> geoLocationCompletionStage =
			geolocateAddressViaGoogle(address)
				.thenApply(this::extractGeoLocation);

		CompletableFuture<Void> trainStationCompleted = geoLocationCompletionStage
			.thenCompose(location -> findPlace(location, "train_station"))
			.thenApply(this::extractPlace)
			.thenAccept(this::printResult)
			.exceptionally( t -> {log.severe("Failed to find train station: " + t.getMessage()); return null;})
			.toCompletableFuture();

		CompletableFuture<Void> subwayCompleted = geoLocationCompletionStage
			.thenCompose(location -> findPlace(location, "subway_station"))
			.thenApply(this::extractPlace)
			.thenAccept(this::printResult)
			.exceptionally( t -> {log.severe("Failed to find subway station: " + t.getMessage()); return null;})
			.toCompletableFuture();

		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(trainStationCompleted, subwayCompleted);

		while (!combinedFuture.isDone()) {
			System.out.println("Main thread: Doing something important while waiting...");
			Thread.sleep(500);
		}

		System.out.println("Main thread: Done.");
	}


	/* ========================= private helper methods ========================= */


	private CompletionStage<Response> geolocateAddressViaGoogle(String address) {
    log.info("Geo Location via Google for address: " + address);
		return ClientBuilder.newBuilder().executorService(myExecutorService).build()
			.target("https://maps.googleapis.com/maps/api/geocode/json")
			.queryParam("address", address)
			.queryParam("key", config.getString(ConfigurationKeys.GOOGLE_APIKEY))
			.queryParam("language", "de")
			.request()
			.accept(MediaType.APPLICATION_JSON)
			.rx()
			.get();
	}

	private GeoLocation extractGeoLocation(Response response) {
		int httpStatus = response.getStatus();
		if (httpStatus != Response.Status.OK.getStatusCode()) {
			throw new RuntimeException("Request to Google Geocoding API failed with status " + httpStatus);
		}

		GenericType<GoogleApiResponse<GeoLocation>> entityType = new GenericType<>() {};
		GoogleApiResponse<GeoLocation> apiResponse = response.readEntity(entityType);

        failOnApiError(apiResponse, "Google Geocoding API");

		if (apiResponse.getResults().isEmpty()) {
			throw new RuntimeException("Geo Location not found for address!");
		}

		return apiResponse.getResults().get(0);
	}

  private CompletionStage<Response> findPlace(GeoLocation geoLocation, String placeType) {
		String latLng = geoLocation.getGeometry().getLocation().toString();

		return ClientBuilder.newBuilder().executorService(myExecutorService).build()
			.target("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
			.queryParam("key", config.getString(ConfigurationKeys.GOOGLE_APIKEY))
			.queryParam("language", "de")
			.queryParam("location", latLng)
			.queryParam("rankby", "distance")
			.queryParam("type", placeType)
			.request()
			.accept(MediaType.APPLICATION_JSON)
			.rx()
			.get();
	}

	private Place extractPlace(Response response) {
		int httpStatus = response.getStatus();
		if (httpStatus != Response.Status.OK.getStatusCode()) {
			throw new RuntimeException("Request to Google Places API failed with status " + httpStatus);
		}

		GenericType<GoogleApiResponse<Place>> entityType = new GenericType<>() {};
		GoogleApiResponse<Place> apiResponse = response.readEntity(entityType);

		failOnApiError(apiResponse, "Google Places API");

		List<Place> places = apiResponse.getResults();
		return places.isEmpty() ? null : places.get(0);
	}

	private void printResult(Place place) {
		System.out.println("JAX-RS thread: Nearest station: " + place.getName());
	}


	private void failOnApiError(GoogleApiResponse<?> apiResponse, String apiName) {
		String apiStatus = apiResponse.getStatus();
		String apiError = apiResponse.getErrorMessage();

		String msg =
			String.format("Response received from %s, but with status %s. Error message: %s", apiName, apiStatus, apiError);

		switch (apiStatus) {
			case STATUS_OVER_QUERY_LIMIT:
				throw new OverQueryLimitException(msg);
			case STATUS_REQUEST_DENIED:
			case STATUS_INVALID_REQUEST:
			case STATUS_UNKNOWN_ERROR:
				throw new RuntimeException(msg);
			default:
				// nothing to do
		}
	}
}
