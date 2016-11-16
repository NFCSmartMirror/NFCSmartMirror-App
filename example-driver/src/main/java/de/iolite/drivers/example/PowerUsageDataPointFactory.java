/* Copyright (C) 2016 IOLITE GmbH, All rights reserved.
 * Created:    16.11.2016
 * Created by: lehmann
 */

package de.iolite.drivers.example;

import java.util.Random;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import de.iolite.drivers.framework.DataPoint;
import de.iolite.drivers.framework.DataPointConfiguration;
import de.iolite.drivers.framework.DataPointFactory;
import de.iolite.drivers.framework.DataPointValueCallback;
import de.iolite.drivers.framework.exception.DataPointConfigurationException;
import de.iolite.drivers.framework.exception.DataPointInstantiationException;
import de.iolite.drivers.framework.exception.IllegalValueException;
import de.iolite.utilities.concurrency.scheduler.Scheduler;

final class PowerUsageDataPointFactory implements DataPointFactory {

	private static final class PowerUsageDataPoint implements DataPoint {

		private PowerUsageDataPoint(final double initialValue, @Nonnull final DataPointValueCallback callback)
				throws DataPointConfigurationException {
			try {
				callback.newDoubleValue(initialValue);
			}
			catch (final IllegalValueException e) {
				throw new DataPointConfigurationException("Initial value is illegal", e);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void destroy() {
			// nothing to do
		}
	}

	private static final class PowerUsageDataPointWithRandomization implements DataPoint {

		private static final int MAXIMUM_POWER_USAGE = 3600;

		@Nonnull
		private final Future<?> randomizationTask;

		@Nonnull
		private final Random random = new Random();

		@Nonnull
		private final DataPointValueCallback callback;

		@Nonnull
		private final AtomicInteger nextValue;

		private PowerUsageDataPointWithRandomization(final double initialValue, @Nonnull final DataPointValueCallback dataPointValueCallback,
				@Nonnull final Scheduler scheduler) {
			this.callback = dataPointValueCallback;
			this.nextValue = new AtomicInteger((int) initialValue);
			this.randomizationTask = scheduler.scheduleWithFixedDelay(this::reportRandomValue, 0, 10, TimeUnit.SECONDS);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void destroy() {
			this.randomizationTask.cancel(true);
		}

		private void reportRandomValue() {
			final int newValue = this.nextValue.getAndSet(this.random.nextInt(MAXIMUM_POWER_USAGE));
			try {
				this.callback.newDoubleValue(newValue);
			}
			catch (final IllegalValueException e) {
				LOGGER.error("Failed to report random value '{}'", newValue, e);
			}
		}
	}

	@Nonnull
	private static final Logger LOGGER = LoggerFactory.getLogger(PowerUsageDataPointFactory.class);

	@Nonnull
	private final Scheduler scheduler;

	PowerUsageDataPointFactory(@Nonnull final Scheduler dataPointScheduler) {
		this.scheduler = Validate.notNull(dataPointScheduler, "'dataPointScheduler' must not be null");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Nonnull
	public DataPoint create(@Nonnull final DataPointConfiguration configuration, @Nonnull final String propertyTypeIdentifier,
			@Nonnull final DataPointValueCallback callback)
			throws DataPointConfigurationException, DataPointInstantiationException {
		final double initialValue = configuration.getDouble(ExampleDriver.CONFIGURATION_INITIAL_VALUE);
		if (configuration.getBoolean(ExampleDriver.CONFIGURATION_RANDOMIZE_VALUE)) {
			return new PowerUsageDataPointWithRandomization(initialValue, callback, this.scheduler);
		}
		return new PowerUsageDataPoint(initialValue, callback);
	}
}
