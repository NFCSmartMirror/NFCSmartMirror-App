/**
 * Copyright (C) 2016 IOLITE GmbH, All rights reserved.
 */

package de.iolite.apps.example;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.text.DateFormat;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nonnull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iolite.api.IOLITEAPINotResolvableException;
import de.iolite.api.IOLITEAPIProvider;
import de.iolite.api.IOLITEPermissionDeniedException;
import de.iolite.api.heating.access.HeatingAPI;
import de.iolite.api.heating.access.PlaceSchedule;
import de.iolite.app.AbstractIOLITEApp;
import de.iolite.app.api.device.DeviceAPIException;
import de.iolite.app.api.device.access.Device;
import de.iolite.app.api.device.access.DeviceAPI;
import de.iolite.app.api.device.access.DeviceAPI.DeviceAPIObserver;
import de.iolite.app.api.device.access.DeviceBooleanProperty;
import de.iolite.app.api.device.access.DeviceBooleanProperty.DeviceBooleanPropertyObserver;
import de.iolite.app.api.device.access.DeviceDoubleProperty;
import de.iolite.app.api.environment.EnvironmentAPI;
import de.iolite.app.api.environment.Location;
import de.iolite.app.api.frontend.FrontendAPI;
import de.iolite.app.api.frontend.FrontendAPIException;
import de.iolite.app.api.frontend.util.FrontendAPIRequestHandler;
import de.iolite.app.api.frontend.util.FrontendAPIUtility;
import de.iolite.app.api.network.NetworkAPI;
import de.iolite.app.api.storage.StorageAPI;
import de.iolite.app.api.storage.StorageAPIException;
import de.iolite.app.api.user.access.UserAPI;
import de.iolite.apps.example.internals.PageWithEmbeddedSessionTokenRequestHandler;
import de.iolite.common.lifecycle.exception.CleanUpFailedException;
import de.iolite.common.lifecycle.exception.InitializeFailedException;
import de.iolite.common.lifecycle.exception.StartFailedException;
import de.iolite.common.lifecycle.exception.StopFailedException;
import de.iolite.common.requesthandler.HTTPStatus;
import de.iolite.common.requesthandler.IOLITEHTTPRequest;
import de.iolite.common.requesthandler.IOLITEHTTPRequestHandler;
import de.iolite.common.requesthandler.IOLITEHTTPResponse;
import de.iolite.common.requesthandler.IOLITEHTTPStaticResponse;
import de.iolite.common.requesthandler.StaticResources;
import de.iolite.common.requesthandler.StaticResources.PathHandlerPair;
import de.iolite.drivers.basic.DriverConstants;
import de.iolite.utilities.disposeable.Disposeable;
import de.iolite.utilities.time.series.DataEntries.AggregatedEntry;
import de.iolite.utilities.time.series.DataEntries.BooleanEntry;
import de.iolite.utilities.time.series.Function;
import de.iolite.utilities.time.series.TimeInterval;

/**
 * <code>ExampleApp</code> is an example IOLITE App.
 *
 * @author Grzegorz Lehmann
 * @author Erdene-Ochir Tuguldur
 * @author Felix Rodemund
 * @since 1.0
 */
public final class ExampleApp extends AbstractIOLITEApp {

	/**
	 * A response handler returning devices filtered by the property type.
	 */
	class DevicesResponseHandler extends FrontendAPIRequestHandler {

		@Override
		protected IOLITEHTTPResponse handleRequest(final IOLITEHTTPRequest request, final String subPath) {
			String propertyType;
			try {
				propertyType = new JSONObject(readPassedData(request)).getString("propertyType");
			}
			catch (final JSONException e) {
				LOGGER.error("Could not handle devices request due to a JSON error: {}", e.getMessage(), e);
				return new IOLITEHTTPStaticResponse(e.getMessage(), HTTPStatus.BadRequest, "text/plain");
			}
			catch (final IOException e) {
				LOGGER.error("Could not handle devices request due to an I/O error: {}", e.getMessage(), e);
				return new IOLITEHTTPStaticResponse(e.getMessage(), HTTPStatus.BadRequest, "text/plain");
			}

			final JSONArray jsonDeviceArray = new JSONArray();
			for (final Device device : ExampleApp.this.deviceAPI.getDevices()) {
				if (device.getProperty(propertyType) != null) {
					// device has the correct property type
					final JSONObject jsonDeviceObject = new JSONObject();
					jsonDeviceObject.put("name", device.getName());
					jsonDeviceObject.put("identifier", device.getIdentifier());
					jsonDeviceArray.put(jsonDeviceObject);
				}
			}

			final JSONObject response = new JSONObject();
			response.put("devices", jsonDeviceArray);
			return new IOLITEHTTPStaticResponse(response.toString(), IOLITEHTTPResponse.JSON_CONTENT_TYPE);
		}

		private String getCharset(final IOLITEHTTPRequest request) {
			final String charset = request.getCharset();
			return charset == null || charset.length() == 0 ? IOLITEHTTPStaticResponse.ENCODING_UTF8 : charset;
		}

		private String readPassedData(final IOLITEHTTPRequest request)
				throws IOException {
			final String charset = getCharset(request);
			try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(request.getContent(), charset))) {
				final StringBuilder stringBuilder = new StringBuilder();
				String line;
				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line);
				}
				return stringBuilder.toString();
			}
		}
	}

	/**
	 * A response handler for returning the "not found" response.
	 */
	static class NotFoundResponseHandler extends FrontendAPIRequestHandler {

		@Override
		protected IOLITEHTTPResponse handleRequest(final IOLITEHTTPRequest request, final String subPath) {
			return new IOLITEHTTPStaticResponse(HTTPStatus.NotFound, IOLITEHTTPResponse.HTML_CONTENT_TYPE);
		}
	}

	/**
	 * A response handler returning all rooms as JSON array.
	 */
	class RoomsResponseHandler extends FrontendAPIRequestHandler {

		@Override
		protected IOLITEHTTPResponse handleRequest(final IOLITEHTTPRequest request, final String subPath) {
			final JSONArray locationNames = new JSONArray();
			for (final Location location : ExampleApp.this.environmentAPI.getLocations()) {
				locationNames.put(location.getName());
			}
			final JSONObject response = new JSONObject();
			response.put("rooms", locationNames);
			return new IOLITEHTTPStaticResponse(response.toString(), IOLITEHTTPResponse.JSON_CONTENT_TYPE);
		}
	}

	/** Logger */
	static final Logger LOGGER = LoggerFactory.getLogger(ExampleApp.class);
	/* App APIs */
	private FrontendAPI frontendAPI;
	private StorageAPI storageAPI;
	private DeviceAPI deviceAPI;
	private EnvironmentAPI environmentAPI;
	@SuppressWarnings("unused")
	private NetworkAPI networkAPI;
	private UserAPI userAPI;

	private HeatingAPI heatingAPI;

	/** front end assets */
	private Disposeable disposeableAssets;

	/**
	 * <code>ExampleApp</code> constructor. An IOLITE App must have a public, parameter-less constructor.
	 */
	public ExampleApp() {
		// empty
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void cleanUpHook()
			throws CleanUpFailedException {
		LOGGER.debug("Cleaning");
		LOGGER.debug("Cleaned");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void initializeHook()
			throws InitializeFailedException {
		LOGGER.debug("Initializing");
		LOGGER.debug("Initialized");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected final void startHook(@Nonnull final IOLITEAPIProvider context)
			throws StartFailedException {
		// here the IOLITE App is started
		// the context gives access to IOLITE App APIs
		LOGGER.debug("Starting");

		try {
			// use User API
			this.userAPI = context.getAPI(UserAPI.class);
			LOGGER.debug("Running for user '{}' with locale '{}'", this.userAPI.getUser().getIdentifier(), this.userAPI.getUser().getLocale());

			// Storage API enables the App to store data persistently
			// whatever is stored via the storage API will also be available if the App is restarted
			this.storageAPI = context.getAPI(StorageAPI.class);
			initializeStorage();

			// Frontend API enables the App to expose a user interface
			this.frontendAPI = context.getAPI(FrontendAPI.class);
			initializeWebResources();

			// Device API gives access to devices connected to IOLITE
			this.deviceAPI = context.getAPI(DeviceAPI.class);
			initializeDeviceManager();

			// Environment API gives a access for rooms, current situation etc.
			this.environmentAPI = context.getAPI(EnvironmentAPI.class);
			LOGGER.debug("Current Situation: {}", this.environmentAPI.getCurrentSituationIdentifier());
			LOGGER.debug("Locations:");
			for (final Location location : this.environmentAPI.getLocations()) {
				LOGGER.debug("\t{}", location.getName());
			}

			// Heating API
			this.heatingAPI = context.getAPI(HeatingAPI.class);
			for (final PlaceSchedule placeSchedule : this.heatingAPI.getHeatingSchedulesOfPlaces()) {
				LOGGER.debug("Heating schedule found for place '{}'", placeSchedule.getPlaceIdentifier());
			}

			// Network API
			this.networkAPI = context.getAPI(NetworkAPI.class);
			// TODO:
			// final URLConnection connection = this.networkAPI.openURLConnection(new URL("http://www.spiegel.de"));
		}
		catch (final IOLITEAPINotResolvableException e) {
			throw new StartFailedException(MessageFormat.format("Start failed due to required but not resolvable AppAPI: {0}", e.getMessage()), e);
		}
		catch (final IOLITEPermissionDeniedException e) {
			throw new StartFailedException(MessageFormat.format("Start failed due to permission denied problems in the examples: {0}", e.getMessage()), e);
		}
		catch (final StorageAPIException | FrontendAPIException e) {
			throw new StartFailedException(MessageFormat.format("Start failed due to an error in the App API examples: {0}", e.getMessage()), e);
		}

		LOGGER.debug("Started");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void stopHook()
			throws StopFailedException {
		LOGGER.debug("Stopping");

		// deregister the static assets
		if (this.disposeableAssets != null) {
			this.disposeableAssets.dispose();
		}

		LOGGER.debug("Stopped");
	}

	/**
	 * Example method showing how to use the Device API.
	 */
	private void initializeDeviceManager() {
		// register a device observer
		this.deviceAPI.setObserver(new DeviceAPIObserver() {

			@Override
			public void addedToDevices(final Device device) {
				LOGGER.debug("a new device added '{}'", device.getIdentifier());
			}

			@Override
			public void removedFromDevices(final Device device) {
				LOGGER.debug("a device removed '{}'", device.getIdentifier());
			}
		});

		// go through all devices, and register a property observer for ON/OFF properties
		for (final Device device : this.deviceAPI.getDevices()) {
			// each device has some properties (accessible under device.getProperties())
			// let's get the 'on/off' status property
			final DeviceBooleanProperty onProperty = device.getBooleanProperty(DriverConstants.PROPERTY_on_ID);
			if (onProperty != null) {
				LOGGER.debug("device '{}' has ON/OFF property, current value: '{}'", device.getIdentifier(), onProperty.getValue());

				onProperty.setObserver(new DeviceBooleanPropertyObserver() {

					@Override
					public void deviceChanged(final Device element) {
						// nothing here
					}

					@Override
					public void keyChanged(final String key) {
						// nothing here
					}

					@Override
					public void valueChanged(final Boolean value) {
						if (value) {
							LOGGER.debug("device '{}' turned on", device.getIdentifier());
						}
						else {
							LOGGER.debug("device '{}' turned off", device.getIdentifier());
						}
					}
				});
			}
		}

		// go through all devices, and toggle ON/OFF properties
		for (final Device device : this.deviceAPI.getDevices()) {
			// let's get the 'on/off' status property
			final DeviceBooleanProperty onProperty = device.getBooleanProperty(DriverConstants.PROPERTY_on_ID);
			final Boolean onValue;
			if (onProperty != null && (onValue = onProperty.getValue()) != null) {
				LOGGER.debug("toggling device '{}'", device.getIdentifier());
				try {
					onProperty.requestValueUpdate(!onValue);
				}
				catch (final DeviceAPIException e) {
					LOGGER.error("Failed to control device", e);
				}
			}
		}

		// go through all devices, and print ON/OFF and POWER USAGE property history datas
		for (final Device device : this.deviceAPI.getDevices()) {
			// ON/OFF history data
			final DeviceBooleanProperty onProperty = device.getBooleanProperty(DriverConstants.PROPERTY_on_ID);
			if (onProperty != null) {
				// retrieve the on/off history of last hour
				final long hourMillis = TimeUnit.SECONDS.toMillis(60 * 60);
				final List<BooleanEntry> onHistory;
				try {
					onHistory = onProperty.getValuesSince(System.currentTimeMillis() - hourMillis);
				}
				catch (final DeviceAPIException e) {
					LOGGER.error("Failed to retrieve the history of property '{}'", onProperty.getKey(), e);
					continue;
				}
				LOGGER.debug("Got '{}' historical values for property '{}'", onHistory.size(), onProperty.getKey());
				// log history values
				final DateFormat dateFormat = DateFormat.getTimeInstance();
				for (final BooleanEntry historyEntry : onHistory) {
					LOGGER.debug("At '{}' the value was '{}'", dateFormat.format(new Date(historyEntry.time)), historyEntry.value);
				}
			}

			// POWER USAGE history data
			final DeviceDoubleProperty powerUsage = device.getDoubleProperty(DriverConstants.PROPERTY_powerUsage_ID);
			if (powerUsage != null) {
				LOGGER.debug("Reading today's hourly power usage data from device '{}':", device.getIdentifier());
				List<AggregatedEntry> history;
				try {
					history = powerUsage.getAggregatedValuesOf(System.currentTimeMillis(), TimeInterval.DAY, TimeInterval.HOUR, Function.AVERAGE);
					for (final AggregatedEntry entry : history) {
						LOGGER.debug("The device used an average of {} Watt at '{}'.", entry.getAggregatedValue(),
								DateFormat.getTimeInstance().format(new Date(entry.getEndTime())));
					}
				}
				catch (final DeviceAPIException e) {
					LOGGER.error("Failed to retrieve history data of device", e);
				}
			}
		}
	}

	/**
	 * Example method showing how to use the Storate API.
	 *
	 * @throws StorageAPIException
	 */
	private void initializeStorage()
			throws StorageAPIException {
		// basically the Storage API provides a key/value storage for different data types
		// save an integer under the key 'test'
		this.storageAPI.saveInt("test", 10);
		// now let's store a string
		this.storageAPI.saveString("some key", "some value");

		// log the value of an entry, just to demonstrate
		LOGGER.debug("loading 'test' from storage: {}", Integer.valueOf(this.storageAPI.loadInt("test")));
	}

	/**
	 * Registering web resources.
	 *
	 * @throws FrontendAPIException if some resources are not found.
	 */
	private final void initializeWebResources()
			throws FrontendAPIException {

		// go through static assets and register them
		final Map<URI, PathHandlerPair> assets = StaticResources.scanClasspath("assets", getClass().getClassLoader());
		this.disposeableAssets = FrontendAPIUtility.registerPublicHandlers(this.frontendAPI, assets);

		// index page
		final IOLITEHTTPRequestHandler indexPageRequestHandler = new PageWithEmbeddedSessionTokenRequestHandler(loadTemplate("assets/index.html"));
		this.frontendAPI.registerRequestHandler("", indexPageRequestHandler);
		this.frontendAPI.registerRequestHandler("index.html", indexPageRequestHandler);

		// default handler returning a not found status
		this.frontendAPI.registerDefaultRequestHandler(new NotFoundResponseHandler());

		// example JSON request handlers
		this.frontendAPI.registerRequestHandler("rooms", new RoomsResponseHandler());
		this.frontendAPI.registerRequestHandler("devices", new DevicesResponseHandler());

		// TODO
		this.frontendAPI.registerRequestHandler("get_devices.json", new FrontendAPIRequestHandler() {

			@Override
			protected IOLITEHTTPResponse handleRequest(final IOLITEHTTPRequest request, final String subPath) {
				final JSONArray deviceIdentifiers = new JSONArray();

				final JSONObject response = new JSONObject();
				response.put("identifiers", deviceIdentifiers);
				return new IOLITEHTTPStaticResponse(response.toString(), HTTPStatus.OK, IOLITEHTTPResponse.JSON_CONTENT_TYPE);
			}
		});
	}

	/**
	 * Load a HTML template as string.
	 */
	private String loadTemplate(final String templateResource) {
		try {
			return StaticResources.loadResource(templateResource, getClass().getClassLoader());
		}
		catch (final IOException e) {
			throw new InitializeFailedException("Loading templates for the dummy app failed", e);
		}
	}
}