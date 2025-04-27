package house.greenhouse.greenhouseconfig.api.lang;

import com.mojang.serialization.Codec;
import house.greenhouse.greenhouseconfig.impl.codec.CommentedCodec;

/**
 * A value that can have a comment attached to it.
 * <p>
 * This is designed to be implemented by language implementations.
 */
public interface CommentedValue {
	/**
	 * A {@link Codec} that encodes comments alongside its value.
	 *
	 * @param codec    A codec.
	 * @param comments The comments to encode. New values are a new line.
	 * @param <A>      The codec's type parameter.
	 * @return A commented codec.
	 */
	static <A> Codec<A> codec(Codec<A> codec, String... comments) {
		return new CommentedCodec<>(codec, comments);
	}

	/**
	 * Makes a copy of this commented value with the given comment.
	 *
	 * @param comments the comments the resulting value should have attached.
	 * @return the new value with comments attached.
	 */
	CommentedValue withComment(String[] comments);
}
