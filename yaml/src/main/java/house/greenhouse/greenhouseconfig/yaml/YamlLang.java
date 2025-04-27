package house.greenhouse.greenhouseconfig.yaml;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.yaml.YamlParser;
import com.electronwill.nightconfig.yaml.YamlWriter;

import house.greenhouse.greenhouseconfig.api.lang.ConfigLang;
import house.greenhouse.greenhouseconfig.nightconfig.NightConfigElement;
import house.greenhouse.greenhouseconfig.nightconfig.NightConfigObject;
import house.greenhouse.greenhouseconfig.nightconfig.NightConfigOps;

import com.mojang.serialization.DynamicOps;

public class YamlLang implements ConfigLang<NightConfigElement> {
    public static final YamlLang INSTANCE = new YamlLang();

    private YamlLang() {}

    @Override
    public DynamicOps<NightConfigElement> getOps() {
        return NightConfigOps.INSTANCE;
    }

    @Override
    public String getFileExtension() {
        return "yml";
    }

    @Override
    public void write(Writer writer, NightConfigElement configObj) throws IOException {
        if (configObj instanceof NightConfigObject object) {
            YamlWriter yamlWriter = new YamlWriter();
            yamlWriter.write(object.getConfig(), writer);
            writer.flush();
        }
    }

    @Override
    public NightConfigElement read(Reader reader) {
        YamlParser parser = new YamlParser();
        Config config = parser.parse(reader);
        // night-config-yaml doesn't actually support comments yet :(
        CommentedConfig commented = CommentedConfig.copy(config);
        return new NightConfigObject(commented);
    }
}
