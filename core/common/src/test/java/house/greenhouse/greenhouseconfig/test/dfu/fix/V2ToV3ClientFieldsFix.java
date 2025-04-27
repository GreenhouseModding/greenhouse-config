package house.greenhouse.greenhouseconfig.test.dfu.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import house.greenhouse.greenhouseconfig.api.dfu.GreenhouseConfigDFUReferences;
import house.greenhouse.greenhouseconfig.test.config.TestConfig;

public class V2ToV3ClientFieldsFix extends DataFix {
	public V2ToV3ClientFieldsFix(Schema outputSchema) {
		super(outputSchema, false);
	}

	private static Dynamic<?> fixDynamic(Dynamic<?> dynamic) {
		return dynamic.set("client_color", dynamic.createString(TestConfig.CLIENT_DEFAULT.color().serialize()));
	}

	@Override
	protected TypeRewriteRule makeRule() {
		return fixTypeEverywhereTyped("Fix v2 fields to v3 fields", getInputSchema().getType(GreenhouseConfigDFUReferences.CONFIG), typed -> typed.update(DSL.remainderFinder(), V2ToV3ClientFieldsFix::fixDynamic));
	}
}
