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
	 * @param <A>          The object's type.
	 * @return A default field codec.
	 */
	public static <A> MapCodec<A> codec(final Codec<A> codec, final String name, final A defaultValue) {
		return new DefaultedCodec<>(name, codec, defaultValue);
	}

	/**
	 * A {@link MapCodec} that will encode as a specified default field if not specified with additional comments surrounding the field.
	 * <br>
	 * This is functionally a shortcut to calling a {@link CommentedValue#codec(Codec, String...)} nested inside a {@link DefaultFieldUtil#codec(Codec, String, Object)}.
	 *
	 * @param codec        The codec to use for this object.
	 * @param name         The key of the field.
	 * @param defaultValue The default value to encode.
	 * @param comments	   Any comments to encode with this value.
	 * @param <A>          The object's type.
	 * @return A default field codec with comments.
	 *
	 * @see CommentedValue#codec(Codec, String...)
	 */
	public static <A> MapCodec<A> codecWithComments(final Codec<A> codec, final String name, final A defaultValue, final String... comments) {
		return codec(CommentedValue.codec(codec, comments), name, defaultValue);
	}
}
