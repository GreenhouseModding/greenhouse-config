package house.greenhouse.greenhouseconfig.hocon;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.hocon.HoconParser;
import com.electronwill.nightconfig.hocon.HoconWriter;
import com.mojang.serialization.DynamicOps;
import house.greenhouse.greenhouseconfig.api.lang.ConfigLang;
import house.greenhouse.greenhouseconfig.nightconfig.NightConfigElement;
import house.greenhouse.greenhouseconfig.nightconfig.NightConfigObject;
import house.greenhouse.greenhouseconfig.nightconfig.NightConfigOps;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

public class HoconLang implements ConfigLang<NightConfigElement> {
	public static final HoconLang INSTANCE = new HoconLang();

	private HoconLang() {
	}

	@Override
	public DynamicOps<NightConfigElement> getOps() {
		return NightConfigOps.INSTANCE;
	}

	@Override
	public String getFileExtension() {
		return "hocon";
	}

	@Override
	public void write(Writer writer, NightConfigElement configObj) throws IOException {
		if (configObj instanceof NightConfigObject object) {
			HoconWriter hoconWriter = new HoconWriter();
			hoconWriter.write(object.getConfig(), writer);
			writer.flush();
		}
	}

	@Override
	public NightConfigElement read(Reader reader) {
		HoconParser parser = new HoconParser();
		CommentedConfig config = parser.parse(reader);
		return new NightConfigObject(config);
	}
}
