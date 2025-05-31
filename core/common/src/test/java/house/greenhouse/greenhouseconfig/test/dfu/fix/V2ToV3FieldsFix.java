package house.greenhouse.greenhouseconfig.test.dfu.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import house.greenhouse.greenhouseconfig.api.dfu.GreenhouseConfigDFUReferences;
import house.greenhouse.greenhouseconfig.test.config.TestConfig;

import java.util.Map;

public class V2ToV3FieldsFix extends DataFix {
	private final boolean isClient;

	public V2ToV3FieldsFix(Schema outputSchema, boolean isClient) {
		super(outputSchema, false);
		this.isClient = isClient;
	}

	private static Dynamic<?> fixDynamic(Dynamic<?> dynamic, boolean isClient) {
		Dynamic<?> newDynamic = dynamic.renameAndFixField("liked_enchantment", "enchantment_opinion", dynamic1 ->
						dynamic1.createMap(
								Map.of(
										dynamic1.createString("enchantment"), dynamic1,
										dynamic1.createString("opinion"), dynamic1.createString("like")
								)
						)
				).renameField("blue_blocks", "red_blocks")
				.set("color", dynamic.createString(TestConfig.DEFAULT.color().serialize()));
		if (isClient) {
			return newDynamic
					.set("client_color", dynamic.createString(TestConfig.ClientConfigValues.DEFAULT.color().serialize()));
		}
		return newDynamic;
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped("Fix v2 fields to v3 fields", getInputSchema().getType(GreenhouseConfigDFUReferences.CONFIG), typed -> typed.update(DSL.remainderFinder(), dynamic -> fixDynamic(dynamic, isClient)));
	}
}
