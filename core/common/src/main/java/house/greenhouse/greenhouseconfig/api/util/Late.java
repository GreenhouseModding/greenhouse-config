package house.greenhouse.greenhouseconfig.api.util;

import net.minecraft.core.HolderLookup;

import java.util.function.Consumer;

/**
 * An interface representing a late value, which gets bound to a registry object at the right time.
 */
public interface Late {
	void bind(HolderLookup.Provider registries, Consumer<String> onException);

	void unbind();
}
