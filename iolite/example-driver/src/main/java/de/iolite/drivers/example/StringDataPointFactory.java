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
 * Produces string data points.
 *
 * @author Steven Tunack
 * @since 17.06
 */
final class StringDataPointFactory implements DataPointFactory {

	private static final class StringDataPoint implements WritableDataPoint {

		@Nonnull
		private final DataPointValueCallback callback;

		private StringDataPoint(@Nonnull final String initialValue, @Nonnull final DataPointValueCallback dataPointValueCallback)
				throws DataPointConfigurationException {
			this.callback = dataPointValueCallback;
			try {
				this.callback.newStringValue(initialValue);
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
			try {
				this.callback.newStringValue(newValue);
			}
			catch (final IllegalValueException e) {
				throw new WriteFailedException(String.format("Failed to report written value '%s'", newValue), e);
			}
		}
	}

	@Nonnull
	private final String initialDataPointValue;

	/**
	 * Constructor of StringDataPointFactory.
	 *
	 * @param initialValue initial value that will be set
	 */
	StringDataPointFactory(@Nonnull final String initialValue) {
		this.initialDataPointValue = Validate.notNull(initialValue, "'initialValue' must not be null");
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
		return new StringDataPoint(this.initialDataPointValue, callback);
	}
}
