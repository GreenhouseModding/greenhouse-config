package house.greenhouse.greenhouseconfig.toml;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.toml.TomlParser;
import com.electronwill.nightconfig.toml.TomlWriter;

import house.greenhouse.greenhouseconfig.api.lang.ConfigLang;
import house.greenhouse.greenhouseconfig.toml.internal.TomlElement;
import house.greenhouse.greenhouseconfig.toml.internal.TomlObject;

import com.mojang.serialization.DynamicOps;

public final class TomlLang implements ConfigLang<TomlElement> {
    public static final TomlLang INSTANCE = new TomlLang();

    private TomlLang() {
    }

    @Override
    public DynamicOps<TomlElement> getOps() {
        return TomlOps.INSTANCE;
    }

    @Override
    public String getFileExtension() {
        return "toml";
    }

    @Override
    public void write(Writer writer, TomlElement configObj) throws IOException {
        if (configObj instanceof TomlObject object) {
            TomlWriter tomlWriter = new TomlWriter();
            tomlWriter.write(object.getConfig(), writer);
            writer.flush();
        }
    }

    @Override
    public TomlElement read(Reader reader) throws IOException {
        TomlParser parser = new TomlParser();
        CommentedConfig config = parser.parse(reader);
        return new TomlObject(config);
    }
}
