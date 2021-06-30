package org.sandboxpowered.silica.loading;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.ConfigSpec;
import com.github.zafarkhaja.semver.Version;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.sandboxpowered.api.util.Identifier;

import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class AddonDefinition {

    private static final ConfigSpec CONFIG_SPEC = new ConfigSpec();
    private static final Pattern MODID_PATTERN = Pattern.compile("[a-z0-9-_]{4,15}");
    private static final Predicate<String> MODID_PREDICATE = MODID_PATTERN.asPredicate();

    static {
        CONFIG_SPEC.define("id", "");
        CONFIG_SPEC.define("version", "");
        CONFIG_SPEC.define("title", "");
        CONFIG_SPEC.define("description", "");
        CONFIG_SPEC.define("entrypoint", "");
        CONFIG_SPEC.define("authors", Collections.emptyList());
        CONFIG_SPEC.define("url", "");
        CONFIG_SPEC.define("dependencies", Collections.emptyMap());
        CONFIG_SPEC.define("custom", Config.inMemoryUniversal());
        CONFIG_SPEC.defineOfClass("side", LoadingSide.COMMON, LoadingSide.class);
        CONFIG_SPEC.define("platforms", Collections.emptyMap());
    }

    //metadata
    private final String id;
    private final Version version;
    private final String title;
    private final String description;
    private final List<String> authors;
    private final String url;
    private final Map<String, String> dependencies;
    private final Config customProperties;
    private final LoadingSide side;
    private final Map<String, Boolean> platforms;
    //internal info
    private final String mainClass;
    private final URL path;

    private AddonDefinition(String id, Version version, @Nullable String title, String description, List<String> authors, String url, Map<String, String> dependencies, Config customProperties, LoadingSide side, Map<String, Boolean> platforms, String mainClass, URL path) {
        if (!MODID_PREDICATE.test(id))
            throw new IllegalArgumentException(String.format("Addon ID '%s' does not match regex requirement '%s'", id, MODID_PATTERN.pattern()));
        this.id = id;
        this.version = version;
        if (title == null || title.isEmpty())
            title = id;
        this.title = title;
        this.description = description;
        this.authors = authors;
        this.url = url;
        this.dependencies = dependencies;
        this.customProperties = customProperties;
        this.side = side;
        this.platforms = platforms;
        this.mainClass = mainClass;
        this.path = path;
    }

    public static AddonDefinition from(Config config, URL path) {
        CONFIG_SPEC.correct(config);
        String id = config.get("id");
        if (StringUtils.isEmpty(id))
            throw new IllegalArgumentException(String.format("Addon at path %s does not define an ID!", path.toString()));
        String verString = config.get("version");
        if (StringUtils.isEmpty(verString))
            throw new IllegalArgumentException(String.format("Addon %s does not define a version!", id));
        Version version = Version.valueOf(verString);
        String title = config.get("title");
        String description = config.get("description");
        String mainClass = config.get("entrypoint");
        if (StringUtils.isEmpty(mainClass))
            throw new IllegalArgumentException(String.format("Addon %s does not define an entrypoint!", id));
        List<String> authors = config.get("authors");
        String url = config.get("url");
        Map<String, String> dependencies = config.get("dependencies");
        Config customProperties = config.get("custom");
        LoadingSide side = config.getEnum("side", LoadingSide.class);
        Map<String, Boolean> platforms = config.get("platforms");
        return new AddonDefinition(id, version, title, description, authors, url, dependencies, customProperties, side, platforms, mainClass, path);
    }

    public String getId() {
        return id;
    }

    public Version getVersion() {
        return version;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getUrl() {
        return url;
    }

    public LoadingSide getSide() {
        return side;
    }

    public Map<String, String> getDependencies() {
        return dependencies;
    }

    public Config getCustomProperties() {
        return customProperties;
    }

    public Map<String, Boolean> getPlatforms() {
        return platforms;
    }

    public PlatformSupport getPlatformSupport(Identifier platform) {
        if (platforms.containsKey(platform.toString())) {
            if (platforms.get(platform.toString())) return PlatformSupport.YES;
            return PlatformSupport.NO;
        } else {
            return PlatformSupport.MAYBE;
        }
    }

    public String getMainClass() {
        return mainClass;
    }

    public URL getPath() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddonDefinition addonSpec = (AddonDefinition) o;
        return Objects.equals(id, addonSpec.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    public enum PlatformSupport {
        YES,
        NO,
        MAYBE;

        public boolean canRun() {
            return this != NO;
        }
    }

    public enum LoadingSide {
        CLIENT,
        SERVER,
        COMMON
    }
}
