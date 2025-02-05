package house.greenhouse.greenhouseconfig.test.dfu.fix;

import com.mojang.datafixers.DSL;
import com.mojang.datafixers.DataFix;
import com.mojang.datafixers.TypeRewriteRule;
import com.mojang.datafixers.schemas.Schema;
import com.mojang.serialization.Dynamic;
import house.greenhouse.greenhouseconfig.api.dfu.GreenhouseConfigDFUReferences;

public class V1ToV2FieldsFix extends DataFix {
    public V1ToV2FieldsFix(Schema outputSchema) {
        super(outputSchema, false);
    }

    @Override
    protected TypeRewriteRule makeRule() {
        return fixTypeEverywhereTyped("Fix v1 fields to v2 fields", getInputSchema().getType(GreenhouseConfigDFUReferences.CONFIG), typed -> typed.update(DSL.remainderFinder(), V1ToV2FieldsFix::fixDynamic));
    }

    private static Dynamic<?> fixDynamic(Dynamic<?> dynamic) {
        return dynamic.renameField("funny", "silly")
                .set("blue_blocks", dynamic.createString("minecraft:netherrack"))
                .set("liked_enchantment", dynamic.createString("minecraft:frost_walker"));
    }
}
