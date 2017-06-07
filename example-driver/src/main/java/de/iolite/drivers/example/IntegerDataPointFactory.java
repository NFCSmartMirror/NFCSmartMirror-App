/* Copyright (C) 2016 IOLITE GmbH, All rights reserved.
 * Created:    16.11.2016
 * Created by: Steven Tunack
 */

package de.iolite.drivers.example;

import javax.annotation.Nonnull;

import org.apache.commons.lang3.Validate;

import de.iolite.drivers.api.WriteFailedException;
import de.iolite.drivers.framework.DataPoint;
import de.iolite.drivers.framework.DataPointConfiguration;
import de.iolite.drivers.framework.DataPointFactory;
import de.iolite.drivers.framework.DataPointValueCallback;
import de.iolite.drivers.framework.WritableDataPoint;
import de.iolite.drivers.framework.exception.DataPointConfigurationException;
import de.iolite.drivers.framework.exception.DataPointInstantiationException;
import de.iolite.drivers.framework.exception.IllegalValueException;

/**
 * Produces integer data points.
 *
 * @author Steven Tunack
 * @since 17.06
 */
final class IntegerDataPointFactory implements DataPointFactory {

	private static final class IntegerDataPoint implements WritableDataPoint {

		@Nonnull
		private final DataPointValueCallback callback;

		private IntegerDataPoint(final int initialValue, @Nonnull final DataPointValueCallback dataPointValueCallback)
				throws DataPointConfigurationException {
			this.callback = dataPointValueCallback;
			try {
				this.callback.newIntValue(initialValue);
			}
			catch (final IllegalValueException e) {
				throw new DataPointConfigurationException("Initial value is illegal", e);
			}
		}

		@Override
		public void destroy() {
			// nothing to do
		}

		@Override
		public void write(@Nonnull final String newValue)
				throws WriteFailedException {
			Validate.notNull(newValue, "'newValue' must not be null");
			try {
				this.callback.newIntValue(Integer.parseInt(newValue));
			}
			catch (final NumberFormatException e) {
				throw new WriteFailedException(String.format("Failed to parse '%s' to int", newValue), e);
			}
			catch (final IllegalValueException e) {
				throw new WriteFailedException(String.format("Failed to report written value '%s'", newValue), e);
			}
		}
	}

	private final int initialDataPointValue;

	/**
	 * Constructor of IntegerDataPointFactory.
	 *
	 * @param initialValue initial value that will be set.
	 */
	IntegerDataPointFactory(final int initialValue) {
		this.initialDataPointValue = initialValue;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Nonnull
	public DataPoint create(@Nonnull final DataPointConfiguration configuration, @Nonnull final String propertyTypeIdentifier,
			@Nonnull final DataPointValueCallback callback)
			throws DataPointConfigurationException, DataPointInstantiationException {
		Validate.notNull(configuration, "'configuration' must not be null");
		Validate.notNull(propertyTypeIdentifier, "'propertyTypeIdentifier' must not be null");
		Validate.notNull(callback, "'callback' must not be null");
		return new IntegerDataPoint(this.initialDataPointValue, callback);
	}
}
