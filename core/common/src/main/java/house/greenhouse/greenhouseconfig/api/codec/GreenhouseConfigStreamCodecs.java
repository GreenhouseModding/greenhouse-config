package house.greenhouse.greenhouseconfig.api.codec;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import house.greenhouse.greenhouseconfig.api.util.LateHolder;
import house.greenhouse.greenhouseconfig.api.util.LateHolderSet;
import house.greenhouse.greenhouseconfig.impl.util.LateHolderImpl;
import house.greenhouse.greenhouseconfig.impl.util.LateHolderSetImpl;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.network.VarInt;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

public class GreenhouseConfigStreamCodecs {
    /**
     * A {@link StreamCodec} for a LateHolder, a {@link net.minecraft.core.Holder} that gets bound when registries are loaded.
     *
     * @param registry The registry key to use for this codec.
     * @return A {@link LateHolder} codec.
     * @param <E> The type that the registry holds.
     * @see house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder.Builder#lateValues(Function, Consumer)
     */
    public static <E> StreamCodec<ByteBuf, LateHolder<E>> lateHolderStreamCodec(final ResourceKey<Registry<E>> registry) {
        StreamCodec<ByteBuf, Holder<E>> holder = new StreamCodec<>() {
            private final StreamCodec<ByteBuf, ResourceKey<E>> holderCodec = ResourceKey.streamCodec(registry);

            public Holder<E> decode(ByteBuf buf) {
                ResourceKey<E> resourceKey = holderCodec.decode(buf);
                return new LateHolderImpl<>(resourceKey.registryKey(), resourceKey);
            }

            public void encode(ByteBuf buf, Holder<E> holder) {
                if (!(holder instanceof LateHolder<E> lateHolder))
                    holderCodec.encode(buf, holder.unwrapKey().orElseThrow());
                else
                    holderCodec.encode(buf, lateHolder.key());
            }
        };
        return holder.map(holders -> (LateHolder<E>) holders, holders -> holders);
    }


    /**
     * A {@link StreamCodec} for a LateHolderSet, a {@link net.minecraft.core.HolderSet} that gets bound when registries are loaded.
     *
     * @param registry The registry key to use for this stream/packet codec.
     * @return A {@link LateHolderSet} stream/packet codec.
     * @param <E> The type that the registry holds.
     * @see house.greenhouse.greenhouseconfig.api.GreenhouseConfigHolder.Builder#lateValues(Function, Consumer)
     */
    public static <E> StreamCodec<ByteBuf, LateHolderSet<E>> lateHolderSetStreamCodec(final ResourceKey<? extends Registry<E>> registry) {
        StreamCodec<ByteBuf, HolderSet<E>> holder = new StreamCodec<>() {
            private final StreamCodec<ByteBuf, ResourceKey<E>> holderCodec = ResourceKey.streamCodec(registry);

            public HolderSet<E> decode(ByteBuf buf) {
                int i = VarInt.read(buf) - 1;
                if (i == -1) {
                    return LateHolderSetImpl.createFromTags((ResourceKey<Registry<E>>) registry, List.of(TagKey.create(registry, ResourceLocation.STREAM_CODEC.decode(buf))));
                } else {
                    List<TagKey<E>> tags = new ArrayList<>(Math.min(i, 65536));
                    List<ResourceKey<E>> entries = new ArrayList<>(Math.min(i, 65536));

                    for (int j = 0; j < i; ++j) {
                        boolean isTag = buf.readBoolean();
                        if (isTag)
                            tags.add(TagKey.create(registry, ResourceLocation.STREAM_CODEC.decode(buf)));
                        else
                            entries.add(holderCodec.decode(buf));
                    }

                    return LateHolderSetImpl.createMixed((ResourceKey<Registry<E>>) registry, tags, entries);
                }
            }

            public void encode(ByteBuf buf, HolderSet<E> holderSet) {
                Optional<TagKey<E>> optional = holderSet.unwrapKey();
                if (optional.isPresent()) {
                    VarInt.write(buf, -1);
                    ResourceLocation.STREAM_CODEC.encode(buf, optional.get().location());
                } else {
                    VarInt.write(buf, holderSet.size() + 1);
                    if (holderSet instanceof LateHolderSetImpl<E> late) {

                        for (Either<TagKey<E>, ResourceKey<E>> value : late.keys()) {
                            if (value.left().isPresent()) {
                                buf.writeBoolean(true);
                                ResourceLocation.STREAM_CODEC.encode(buf, value.left().orElseThrow().location());
                            } else {
                                buf.writeBoolean(false);
                                holderCodec.encode(buf, value.right().orElseThrow());
                            }
                        }
                    } else {
                        for (Holder<E> value : holderSet) {
                            buf.writeBoolean(false);
                            holderCodec.encode(buf, value.unwrapKey().orElseThrow());
                        }
                    }
                }

            }
        };
        return holder.map(holders -> (LateHolderSet<E>) holders, holders -> holders);
    }
}
