package house.greenhouse.greenhouseconfig.api;

import house.greenhouse.greenhouseconfig.api.event.GreenhouseConfigCallback;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public class GreenhouseConfigEvents {
    public static final Event<GreenhouseConfigCallback> POST_LOAD = EventFactory.createArrayBacked(GreenhouseConfigCallback.class, callbacks -> (holder, config, side) -> {
        for (GreenhouseConfigCallback callback : callbacks) {
            callback.onConfig(holder, config, side);
        }
    });

    public static final Event<GreenhouseConfigCallback> POST_POPULATION = EventFactory.createArrayBacked(GreenhouseConfigCallback.class, callbacks -> (holder, config, side) -> {
        for (GreenhouseConfigCallback callback : callbacks) {
            callback.onConfig(holder, config, side);
        }
    });

    public static final Event<GreenhouseConfigCallback> POST_DEPOPULATION = EventFactory.createArrayBacked(GreenhouseConfigCallback.class, callbacks -> (holder, config, side) -> {
        for (GreenhouseConfigCallback callback : callbacks) {
            callback.onConfig(holder, config, side);
        }
    });
}
