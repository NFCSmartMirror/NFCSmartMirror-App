/*
 * Copyright (C) 2015 IOLITE, All rights reserved. Created: 02.01.2015 Created by: lehmann
 */

package de.iolite.drivers.example;

import static de.iolite.drivers.basic.DriverConstants.PROPERTY_blindDriveStatus_LITERAL_stopped;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iolite.drivers.basic.DriverConstants;
import de.iolite.drivers.framework.DataPoint;
import de.iolite.drivers.framework.DataPointConfiguration;
import de.iolite.drivers.framework.DataPointFactory;
import de.iolite.drivers.framework.DataPointValueCallback;
import de.iolite.drivers.framework.Driver;
import de.iolite.drivers.framework.DriverAPI;
import de.iolite.drivers.framework.device.Device;
import de.iolite.drivers.framework.device.DeviceConfigurationBuilder;
import de.iolite.drivers.framework.device.DeviceConfigurationObserver;
import de.iolite.drivers.framework.exception.DataPointConfigurationException;
import de.iolite.drivers.framework.exception.DataPointInstantiationException;
import de.iolite.drivers.framework.exception.DeviceConfigurationException;
import de.iolite.drivers.framework.exception.DeviceStartException;
import de.iolite.drivers.framework.exception.DriverStartFailedException;
import de.iolite.utilities.concurrency.scheduler.Scheduler;

/**
 * Demonstrates the implementation of an IOLITE driver.
 *
 * @author Grzegorz Lehmann
 * @since 16.11
 */
public final class ExampleDriver implements Driver {

	private enum DataPointTypes {
		POWER_USAGE("power_usage"), ON_OFF_STATUS("on_off_status"), BOOLEAN_SENSOR("boolean_sensor"), INTEGER_DATAPOINT("integer_datapoint"), STRING_DATAPOINT("string_datapoint"), BLIND_DRIVE_STATUS("blind_drive_status"), DOUBLE_DATAPOINT("double_datapoint");

		@Nonnull
		private final String name;

		private DataPointTypes(@Nonnull final String typeName) {
			this.name = typeName;
		}

		@Nonnull
		private String getName() {
			return this.name;
		}

		@Nonnull
		private static DataPointTypes get(@Nonnull final String dataPointTypeName)
				throws DataPointConfigurationException {
			for (final DataPointTypes value : values()) {
				if (value.getName().equalsIgnoreCase(dataPointTypeName)) {
					return value;
				}
			}
			throw new DataPointConfigurationException(String.format("Unknown data point type '%s'", dataPointTypeName));
		}
	}

	private static final class DeviceStarter implements DeviceConfigurationObserver {

		@Nonnull
		private final DataPointFactory factory;

		private DeviceStarter(@Nonnull final DataPointFactory dataPointFactory) {
			this.factory = dataPointFactory;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onConfigured(@Nonnull final Device device) {
			// start all configured devices immediately
			try {
				device.start(this.factory);
			}
			catch (final DeviceStartException e) {
				LOGGER.error("Failed to start device '{}'", device, e);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void onRemoved(@Nonnull final String deviceIdentifier) {
			// nothing to do
		}
	}

	private static final class ExampleDataPointFactory implements DataPointFactory {

		private final Map<DataPointTypes, DataPointFactory> strategies = new EnumMap<>(DataPointTypes.class);

		private ExampleDataPointFactory(@Nonnull final Scheduler scheduler) {
			this.strategies.put(DataPointTypes.POWER_USAGE, new PowerUsageDataPointFactory(scheduler));
			this.strategies.put(DataPointTypes.ON_OFF_STATUS, new OnOffStatusDataPointFactory());
			this.strategies.put(DataPointTypes.BOOLEAN_SENSOR,new BooleanSensorDataPointFactory(scheduler));
			this.strategies.put(DataPointTypes.INTEGER_DATAPOINT,new IntegerDataPointFactory(0));
			this.strategies.put(DataPointTypes.BLIND_DRIVE_STATUS,new StringDataPointFactory(PROPERTY_blindDriveStatus_LITERAL_stopped));
			this.strategies.put(DataPointTypes.DOUBLE_DATAPOINT,new DoubleDataPointFactory(0.0));


		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		@Nonnull
		public DataPoint create(@Nonnull final DataPointConfiguration configuration, @Nonnull final String propertyTypeIdentifier,
				@Nonnull final DataPointValueCallback callback)
				throws DataPointConfigurationException, DataPointInstantiationException {
			final DataPointTypes dataPointType = DataPointTypes.get(configuration.getDataPointType());
			final DataPointFactory strategy = this.strategies.get(dataPointType);

			if (strategy == null) {
				throw new DataPointInstantiationException(String.format("Unknown data point type '%s'", dataPointType));
			}
			return strategy.create(configuration, propertyTypeIdentifier, callback);
		}
	}

	@Nonnull
	private static final Logger LOGGER = LoggerFactory.getLogger(ExampleDriver.class);

	@Nonnull
	static final String CONFIGURATION_INITIAL_VALUE = "initial.value";

	@Nonnull
	static final String CONFIGURATION_RANDOMIZE_VALUE = "randomize.value";
	private static final String IOLITE_GMBH_NAME = "IOLITE GmbH";

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Nonnull
	public DeviceConfigurationObserver start(@Nonnull final DriverAPI driverAPI, @Nonnull final Set<Device> existingDevices)
			throws DriverStartFailedException {
		// report some example devices
		try {
			configureExampleDevices(driverAPI);
		}
		catch (final DeviceConfigurationException e) {
			throw new DriverStartFailedException("Failed to configure devices", e);
		}

		final ExampleDataPointFactory factory = new ExampleDataPointFactory(driverAPI.getScheduler());
		existingDevices.forEach(device -> {
			try {
				device.start(factory);
			}
			catch (final DeviceStartException e) {
				LOGGER.error("Failed to start existing device '{}' due to error: {}", device.getIdentifier(), e.getMessage());
			}
		});
		return new DeviceStarter(factory);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void stop() {
		LOGGER.debug("Stopped");
	}

	private void configureExampleDevices(@Nonnull final DriverAPI deviceManagement)
			throws DeviceConfigurationException {
		//Configure a lamp device
		final DeviceConfigurationBuilder lamp1 = deviceManagement.configure("lamp1", DriverConstants.PROFILE_Lamp_ID);
		lamp1.fromManufacturer(IOLITE_GMBH_NAME);
		lamp1.withDataPoint(DataPointTypes.ON_OFF_STATUS.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_Lamp_on_ID);
		lamp1.withConfiguration(CONFIGURATION_RANDOMIZE_VALUE, true).and(CONFIGURATION_INITIAL_VALUE, 120).forDataPoint(
				DataPointTypes.POWER_USAGE.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_Lamp_powerUsage_ID);
		lamp1.addIfAbsent();

		//Configure a contact sensor device
		final DeviceConfigurationBuilder contactSensor1 = deviceManagement.configure("contactSensor1",DriverConstants.PROFILE_ContactSensor_ID);
		contactSensor1.fromManufacturer(IOLITE_GMBH_NAME);
		contactSensor1.withDataPoint(DataPointTypes.BOOLEAN_SENSOR.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_ContactSensor_contactDetected_ID);
		contactSensor1.addIfAbsent();

		//Configure a movement sensor device
		final DeviceConfigurationBuilder movementSensor1 = deviceManagement.configure("movementSensor1",DriverConstants.PROFILE_MovementSensor_ID);
		movementSensor1.fromManufacturer(IOLITE_GMBH_NAME);
		movementSensor1.withDataPoint(DataPointTypes.BOOLEAN_SENSOR.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_MovementSensor_movementDetected_ID);
		movementSensor1.addIfAbsent();

		//Configure a smoke sensor device
		final DeviceConfigurationBuilder smokeSensor1 = deviceManagement.configure("smokeSensor1",DriverConstants.PROFILE_SmokeDetectionSensor_ID);
		smokeSensor1.fromManufacturer(IOLITE_GMBH_NAME);
		smokeSensor1.withDataPoint(DataPointTypes.BOOLEAN_SENSOR.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_SmokeDetectionSensor_smokeDetected_ID);
		smokeSensor1.addIfAbsent();

		//Configure a window device
		final DeviceConfigurationBuilder window1 = deviceManagement.configure("window1",DriverConstants.PROFILE_Window_ID);
		window1.fromManufacturer(IOLITE_GMBH_NAME);
		window1.withDataPoint(DataPointTypes.BOOLEAN_SENSOR.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_Window_open_ID);
		window1.addIfAbsent();

		//Configure a blind device
		final DeviceConfigurationBuilder blind1 = deviceManagement.configure("blind1", DriverConstants.PROFILE_Blind_ID);
		blind1.fromManufacturer(IOLITE_GMBH_NAME);
		blind1.withDataPoint(DataPointTypes.INTEGER_DATAPOINT.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_Blind_blindLevel_ID);
		blind1.withDataPoint(DataPointTypes.BLIND_DRIVE_STATUS.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_Blind_blindDriveStatus_ID);
		blind1.withConfiguration(CONFIGURATION_RANDOMIZE_VALUE, true).and(CONFIGURATION_INITIAL_VALUE, 120).forDataPoint(
				DataPointTypes.POWER_USAGE.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_Blind_powerUsage_ID);
		blind1.addIfAbsent();

		//Configure a door device
		final DeviceConfigurationBuilder door1 = deviceManagement.configure("door1",DriverConstants.PROFILE_Door_ID);
		door1.fromManufacturer(IOLITE_GMBH_NAME);
		door1.withDataPoint(DataPointTypes.BOOLEAN_SENSOR.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_Door_open_ID);
		door1.addIfAbsent();

		//Configure a socket device
		final DeviceConfigurationBuilder socket1 = deviceManagement.configure("socket1", DriverConstants.PROFILE_Socket_ID);
		socket1.fromManufacturer(IOLITE_GMBH_NAME);
		socket1.withDataPoint(DataPointTypes.ON_OFF_STATUS.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_Socket_on_ID);
		socket1.withConfiguration(CONFIGURATION_RANDOMIZE_VALUE, true).and(CONFIGURATION_INITIAL_VALUE, 120).forDataPoint(
				DataPointTypes.POWER_USAGE.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_Socket_powerUsage_ID);
		socket1.addIfAbsent();

		//Configure a cook top with four hobs device
		final DeviceConfigurationBuilder cookTop1 = deviceManagement.configure("cooktop1", DriverConstants.PROFILE_CookTopWithFourHobs_ID);
		cookTop1.fromManufacturer(IOLITE_GMBH_NAME);
//		cookTop1.withDataPoint(DataPointTypes.ON_OFF_STATUS.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_CookTopWithFourHobs_on_ID);
		cookTop1.withDataPoint(DataPointTypes.INTEGER_DATAPOINT.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_CookTopWithFourHobs_hob1HeatLevelSetting_ID);
		cookTop1.withDataPoint(DataPointTypes.INTEGER_DATAPOINT.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_CookTopWithFourHobs_hob2HeatLevelSetting_ID);
		cookTop1.withDataPoint(DataPointTypes.INTEGER_DATAPOINT.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_CookTopWithFourHobs_hob3HeatLevelSetting_ID);
		cookTop1.withDataPoint(DataPointTypes.INTEGER_DATAPOINT.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_CookTopWithFourHobs_hob4HeatLevelSetting_ID);
		cookTop1.withDataPoint(DataPointTypes.INTEGER_DATAPOINT.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_CookTopWithFourHobs_hob1HeatLevelRemaining_ID);
		cookTop1.withDataPoint(DataPointTypes.INTEGER_DATAPOINT.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_CookTopWithFourHobs_hob2HeatLevelRemaining_ID);
		cookTop1.withDataPoint(DataPointTypes.INTEGER_DATAPOINT.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_CookTopWithFourHobs_hob3HeatLevelRemaining_ID);
		cookTop1.withDataPoint(DataPointTypes.INTEGER_DATAPOINT.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_CookTopWithFourHobs_hob4HeatLevelRemaining_ID);
		cookTop1.withConfiguration(CONFIGURATION_RANDOMIZE_VALUE, true).and(CONFIGURATION_INITIAL_VALUE, 120).forDataPoint(
				DataPointTypes.POWER_USAGE.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_CookTopWithFourHobs_powerUsage_ID);
		cookTop1.addIfAbsent();

		//Configure a oven device
		final DeviceConfigurationBuilder oven1 = deviceManagement.configure("oven1", DriverConstants.PROFILE_Oven_ID);
		oven1.fromManufacturer(IOLITE_GMBH_NAME);
		oven1.withDataPoint(DataPointTypes.ON_OFF_STATUS.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_Socket_on_ID);
		oven1.withConfiguration(CONFIGURATION_RANDOMIZE_VALUE, true).and(CONFIGURATION_INITIAL_VALUE, 120).forDataPoint(
				DataPointTypes.POWER_USAGE.getName()).ofProperty(DriverConstants.PROFILE_PROPERTY_Oven_powerUsage_ID);
		oven1.addIfAbsent();
	}
}
