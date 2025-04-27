package house.greenhouse.greenhouseconfig.api.dfu;

import com.mojang.datafixers.DSL;

public class GreenhouseConfigDFUReferences {
	/**
	 * A type reference for a Greenhouse Config.
	 * For use within your data fixers.
	 */
	public static final DSL.TypeReference CONFIG = () -> "Greenhouse Config";
}
