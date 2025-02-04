package house.greenhouse.greenhouseconfig.api.lang;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import com.mojang.serialization.DynamicOps;

/**
 * Config Language API.
 * <p>
 * This defines everything that GreenhouseConfig needs to use a specific config language.
 *
 * @param <T> the type of config object that will be encoded/decoded to/from the actual config value.
 */
public interface ConfigLang<T> {
    /**
     * {@return The config type's ops}
     */
    DynamicOps<T> getOps();

    /**
     * {@return The config type's file extension.}
     */
    String getFileExtension();

    /**
     * Writes a config object to a writer.
     *
     * @param writer    the writer to write to.
     * @param configObj the config object to write.
     * @throws IOException if an error ocurrs while writing the config object to the writer.
     */
    void write(Writer writer, T configObj) throws IOException;

    /**
     * Reads a config object from a reader.
     *
     * @param reader the reader to read from.
     * @return the read config object.
     * @throws IOException if an error ocurrs while reading the config object from the reader.
     */
    T read(Reader reader) throws IOException;
}
