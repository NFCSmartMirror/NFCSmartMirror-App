/* Copyright (C) 2016 IOLITE GmbH, All rights reserved.
 * Created:    16.11.2016
 * Created by: lehmann
 */

package de.iolite.drivers.example;

import javax.annotation.Nonnull;

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
 * Produces on/off data points.
 *
 * @author Grzegorz Lehmann
 * @since 16.11
 */
final class OnOffStatusDataPointFactory implements DataPointFactory {

	private static final class OnOffStatusDataPoint implements WritableDataPoint {

		@Nonnull
		private final DataPointValueCallback callback;

		private OnOffStatusDataPoint(@Nonnull final DataPointValueCallback dataPointValueCallback)
				throws DataPointConfigurationException {
			this.callback = dataPointValueCallback;
			//init with false value
			try {
				callback.newBooleanValue(false);
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
				this.callback.newBooleanValue(Boolean.parseBoolean(newValue));
			}
			catch (final IllegalValueException e) {
				throw new WriteFailedException(String.format("Failed to report written value '%s'", newValue), e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	@Nonnull
	public DataPoint create(@Nonnull final DataPointConfiguration configuration, @Nonnull final String propertyTypeIdentifier,
			@Nonnull final DataPointValueCallback callback)
			throws DataPointConfigurationException, DataPointInstantiationException {
		return new OnOffStatusDataPoint(callback);
	}
}
