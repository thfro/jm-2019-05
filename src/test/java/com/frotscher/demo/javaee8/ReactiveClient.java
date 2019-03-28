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
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

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
public class ReactiveClient {

    @Inject
    private Configuration config;

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
	 * In this example the actual logic of each stage is implemented right where the completion stage is defined
	 * (see also ReactiveClient2)
	 *
	 */
	@Test
	public void testFindClosestTrainStations() throws InterruptedException {

		Client googleApiClient = ClientBuilder.newClient();

		CompletionStage<Response> googleAddressValidationStage =
			googleApiClient.target("https://maps.googleapis.com/maps/api/geocode/json")
				.queryParam("address", address)
				.queryParam("key", config.getString(ConfigurationKeys.GOOGLE_APIKEY))
				.queryParam("language", "de")
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.rx()
				.get();

		CompletionStage<GeoLocation> extractGeoLocationStage =
			googleAddressValidationStage.thenApply(
				response -> {
					int httpStatus = response.getStatus();
					if (httpStatus != Response.Status.OK.getStatusCode()) {
						throw new RuntimeException("Request to Google Geocoding API failed with status " + httpStatus);
					}

					GenericType<GoogleApiResponse<GeoLocation>> entityType = new GenericType<>() {};
					GoogleApiResponse<GeoLocation> apiResponse = response.readEntity(entityType);

					failOnApiError(apiResponse, "Google Places API");

					return apiResponse.getResults().get(0);
				}
		);

		CompletionStage<Response> googleFindTrainStationStage =
			extractGeoLocationStage.thenCompose(
				geoLocation -> {
					String latLng = geoLocation.getGeometry().getLocation().toString();
					String placeType = "train_station";

					return googleApiClient.target("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
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
			);

		CompletionStage<Place> extractPlaceStage = googleFindTrainStationStage.thenApply(
			response -> {
				int httpStatus = response.getStatus();
				if (httpStatus != Response.Status.OK.getStatusCode()) {
					throw new RuntimeException("Request to Google Places API failed with status " + httpStatus);
				}

				GenericType<GoogleApiResponse<Place>> entityType = new GenericType<>() {};
				GoogleApiResponse<Place> apiResponse = response.readEntity(entityType);

				failOnApiError(apiResponse, "Google Places API");

				List<Place> places = apiResponse.getResults();
				System.out.println("JAX-RS thread: Number of results in nearby search: " + places.size());

				return places.isEmpty() ? null : places.get(0);
			}
		);

		extractPlaceStage.whenComplete(
			(place, throwable) -> {
				if (throwable!=null) {
					fail("An exception occurred: " + throwable.getMessage());
				} else {
					System.out.println("JAX-RS thread: Nearest train station: " + place.getName());
				}
			}
		);

		CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(extractPlaceStage.toCompletableFuture());

		while (!combinedFuture.isDone()) {
			System.out.println("Main thread: Doing something important while waiting...");
			Thread.sleep(250);
		}

		System.out.println("Main thread: Done.");
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
