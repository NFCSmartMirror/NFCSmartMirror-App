package de.iolite.drivers.example;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

/**
 * Created by Jonathan Gruber on 17.05.2017.
 *
 * @author Jonathan Gruber
 * @author Steven Tunack
 * @since 17.05
 *
 */
public class BooleanSensorDataPointFactory implements DataPointFactory {

	private static final class MovementStatusDataPointWithSimulation implements DataPoint {

		@Nonnull
		private final Future<?> switchValueTask;

		private boolean value;

		@Nonnull
		private final DataPointValueCallback callback;

		private MovementStatusDataPointWithSimulation(@Nonnull final DataPointValueCallback dataPointValueCallback,
				@Nonnull final Scheduler scheduler) {
			this.callback = dataPointValueCallback;
			this.value = true;
			this.switchValueTask = scheduler.scheduleWithFixedDelay(this::reportChangeValue, 0, 10, TimeUnit.SECONDS);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void destroy() {
			this.switchValueTask.cancel(true);
		}

		private void reportChangeValue() {

			this.value = !this.value;
			try {
				this.callback.newBooleanValue(this.value);
			}
			catch (final IllegalValueException e) {
				LOGGER.error("Failed to report new value '{}'", this.value, e.getMessage());
			}
		}
	}

	@Nonnull
	private static final Logger LOGGER = LoggerFactory.getLogger(BooleanSensorDataPointFactory.class);

	@Nonnull
	private final Scheduler scheduler;

	BooleanSensorDataPointFactory(@Nonnull final Scheduler dataPointScheduler) {
		this.scheduler = Validate.notNull(dataPointScheduler, "'dataPointScheduler' must not be null");
	}

	@Override
	@Nonnull
	public DataPoint create(@Nonnull final DataPointConfiguration configuration, @Nonnull final String propertyTypeIdentifier,
			@Nonnull final DataPointValueCallback callback)
			throws DataPointConfigurationException, DataPointInstantiationException {
		return new  BooleanSensorDataPointFactory.MovementStatusDataPointWithSimulation(callback, this.scheduler);
	}
}
