package house.greenhouse.greenhouseconfig.toml;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;

import house.greenhouse.greenhouseconfig.api.lang.ConfigLang;
import house.greenhouse.greenhouseconfig.nightconfig.NightConfigElement;
import house.greenhouse.greenhouseconfig.nightconfig.NightConfigObject;
import house.greenhouse.greenhouseconfig.nightconfig.NightConfigOps;

import com.mojang.serialization.DynamicOps;

public final class TomlLang implements ConfigLang<NightConfigElement> {
    public static final TomlLang INSTANCE = new TomlLang();

    private TomlLang() {
    }

    @Override
    public DynamicOps<NightConfigElement> getOps() {
        return NightConfigOps.INSTANCE;
    }

    @Override
    public String getFileExtension() {
        return "toml";
    }

    @Override
    public void write(Writer writer, NightConfigElement configObj) throws IOException {
        if (configObj instanceof NightConfigObject object) {
            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.write(object.getConfig(), writer);
            writer.flush();
        }
    }

    @Override
    public NightConfigElement read(Reader reader) {
        TomlParser parser = new TomlParser();
        CommentedConfig config = parser.parse(reader);
        return new NightConfigObject(config);
    }
}
