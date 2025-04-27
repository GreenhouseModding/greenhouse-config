package house.greenhouse.greenhouseconfig.api.event;

import house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder;
import house.greenhouse.greenhouseconfig.api.GreenhouseConfigSide;

@FunctionalInterface
public interface GreenhouseConfigCallback {
	/**
	 * A callback for all Greenhouse Config related events.
	 *
	 * @param holder A config holder.
	 * @param config The config object associated with the holder.
	 * @param side   The side that this operation is running on. Either {@link GreenhouseConfigSide#CLIENT} or {@link GreenhouseConfigSide#DEDICATED_SERVER}.
	 */
	void onConfig(GreenhouseConfigHolder<?> holder, Object config, GreenhouseConfigSide side);
}
