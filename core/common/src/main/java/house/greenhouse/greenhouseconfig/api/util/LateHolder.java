package house.greenhouse.greenhouseconfig.api.util;

import house.greenhouse.greenhouseconfig.impl.util.LateHolderImpl;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;

public interface LateHolder<T> extends Holder<T>, Late {
    static <T> LateHolder<T> create(ResourceKey<T> key) {
        return new LateHolderImpl(key.registryKey(), key);
    }

    ResourceKey<T> key();
}
