package house.greenhouse.greenhouseconfig.api.util;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import house.greenhouse.greenhouseconfig.api.lang.CommentedValue;
import house.greenhouse.greenhouseconfig.impl.codec.DefaultedCodec;

public class DefaultFieldUtil {

	/**
	 * A {@link MapCodec} that will encode as a specified default field if not specified.
	 *
	 * @param codec        The codec to use for this object.
	 * @param name         The key of the field.
	 * @param defaultValue The default value to encode.
	 * @param <A>          The codec's type parameter.
	 * @return A default field codec.
	 */
	public static <A> MapCodec<A> codec(final Codec<A> codec, final String name, final A defaultValue) {
		return new DefaultedCodec<>(name, codec, defaultValue);
	}

	public static <A> MapCodec<A> codecWithComments(final Codec<A> codec, final String name, final A defaultValue, final String... comments) {
		return codec(CommentedValue.codec(codec, comments), name, defaultValue);
	}
}
