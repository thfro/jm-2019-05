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
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.concurrent.Future;
import java.util.logging.Logger;

import static com.frotscher.demo.googleclient.GoogleApiResponse.*;
import static org.junit.Assert.fail;

/**
 *
 * For demonstration purposes only.
 *
 */

@RunWith(CdiTestRunner.class)
public class JaxRs20Client {

    @Inject
    private Configuration config;

	private String address = "Opernplatz, Frankfurt";
	private boolean done;


	@Before
	public void checkConfiguration() {
		if (StringUtils.isBlank(config.getString(ConfigurationKeys.GOOGLE_APIKEY))) {
			fail("Missing configuration parameter " + ConfigurationKeys.GOOGLE_APIKEY + " in config.properties.");
		}
	}

	@Test
	public void testFindNearbyTrainStations() throws InterruptedException {

		Client googleApiClient = ClientBuilder.newClient();

			googleApiClient.target("https://maps.googleapis.com/maps/api/geocode/json")
				.queryParam("address", address)
				.queryParam("key", config.getString(ConfigurationKeys.GOOGLE_APIKEY))
				.queryParam("language", "de")
				.request()
				.accept(MediaType.APPLICATION_JSON)
				.async()
				.get(new InvocationCallback<Response>() {

					@Override
					public void failed(Throwable throwable) {
						fail("Failed to send request to Google Geocoding API");
					}

					@Override
					public void completed(Response response) {
						// extract Geo Location
						int httpStatus = response.getStatus();
						if (httpStatus != Response.Status.OK.getStatusCode()) {
							fail("Request to Google Geocoding API failed with status " + httpStatus);
						}

						GenericType<GoogleApiResponse<GeoLocation>> entityType = new GenericType<GoogleApiResponse<GeoLocation>>() {};
						GoogleApiResponse<GeoLocation> apiResponse = response.readEntity(entityType);

						failOnApiError(apiResponse, "Google Places API");

						GeoLocation geoLocation = apiResponse.getResults().get(0);
						String latLng = geoLocation.getGeometry().getLocation().toString();
                        System.out.println("JAX-RS thread retrieved coordinates: " + latLng);

						// find nearest train station asynchronously
						String placeType = "train_station";

                        googleApiClient.target("https://maps.googleapis.com/maps/api/place/nearbysearch/json")
                                .queryParam("key", config.getString(ConfigurationKeys.GOOGLE_APIKEY))
                                .queryParam("language", "de")
                                .queryParam("location", latLng)
                                .queryParam("rankby", "distance")
                                .queryParam("type", placeType)
                                .request()
                                .accept(MediaType.APPLICATION_JSON)
                                .async()
                                .get(new InvocationCallback<Response>() {

                                    @Override
                                    public void failed(Throwable throwable) {
										fail("Failed to send request to Google Places API");
                                    }

                                    @Override
                                    public void completed(Response response) {
										int httpStatus = response.getStatus();
										if (httpStatus != Response.Status.OK.getStatusCode()) {
											fail("Request to Google Places API failed with status " + httpStatus);
										}

                                        GenericType<GoogleApiResponse<Place>> gt = new GenericType<GoogleApiResponse<Place>>() {};
                                        GoogleApiResponse<Place> places = response.readEntity(gt);
                                        System.out.println("JAX-RS thread retrieved train stations: " + places.getResults().size());
                                        places.getResults().forEach(place -> System.out.println("- " + place.getName()));
                                        done = true;
                                    }
                                });
                    }
				});


		while(!done) {
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
