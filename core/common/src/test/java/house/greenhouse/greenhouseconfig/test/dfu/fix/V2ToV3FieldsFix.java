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
	public V2ToV3FieldsFix(Schema outputSchema) {
		super(outputSchema, false);
	}

	private static Dynamic<?> fixDynamic(Dynamic<?> dynamic) {
		return dynamic.renameAndFixField("liked_enchantment", "enchantment_opinion", dynamic1 ->
						dynamic1.createMap(
								Map.of(
										dynamic1.createString("enchantment"), dynamic1,
										dynamic1.createString("opinion"), dynamic1.createString("like")
								)
						)
				).renameField("blue_blocks", "red_blocks")
				.set("color", dynamic.createString(TestConfig.DEFAULT.color().serialize()))
				.set("client_color", dynamic.createString(TestConfig.DEFAULT.clientValues().color().serialize()));
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped("Fix v2 fields to v3 fields", getInputSchema().getType(GreenhouseConfigDFUReferences.CONFIG), typed -> typed.update(DSL.remainderFinder(), V2ToV3FieldsFix::fixDynamic));
	}
}
