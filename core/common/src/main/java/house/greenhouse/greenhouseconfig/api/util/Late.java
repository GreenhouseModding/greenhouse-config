package house.greenhouse.greenhouseconfig.api.util;

import net.minecraft.core.HolderLookup;

import java.util.function.Consumer;

public interface Late {
    void bind(HolderLookup.Provider registries, Consumer<String> onException);

    void unbind();
}
