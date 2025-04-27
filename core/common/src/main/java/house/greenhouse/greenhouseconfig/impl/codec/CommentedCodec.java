package house.greenhouse.greenhouseconfig.impl.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;

import house.greenhouse.greenhouseconfig.api.lang.CommentedValue;

public class CommentedCodec<T> implements Codec<T> {
    protected final String[] comments;
    protected final Codec<T> baseCodec;

    public CommentedCodec(Codec<T> codec, String... comments) {
        this.comments = comments;
        this.baseCodec = codec;
    }

    @Override
    public <TOps> DataResult<Pair<T, TOps>> decode(DynamicOps<TOps> ops, TOps input) {
        return baseCodec.decode(ops, input);
    }

	@SuppressWarnings("unchecked")
	@Override
    public <TOps> DataResult<TOps> encode(T input, DynamicOps<TOps> ops, TOps prefix) {
        DataResult<TOps> result = baseCodec.encode(input, ops, prefix);
        if (result.hasResultOrPartial() && result.resultOrPartial().orElseThrow() instanceof CommentedValue commented) {
            commented = commented.withComment(comments);
            if (result.isSuccess())
                return DataResult.success((TOps) commented);
            DataResult<TOps> errorResult = DataResult.error(() -> result.error().orElseThrow().message());
            if (errorResult.hasResultOrPartial())
                errorResult.setPartial(result.getPartialOrThrow());
            return errorResult;
        }
        return result;
    }
}
