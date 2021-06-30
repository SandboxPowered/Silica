package org.sandboxpowered.silica.loading;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.toml.TomlParser;
import com.github.zafarkhaja.semver.Parser;
import com.github.zafarkhaja.semver.expr.Expression;
import com.github.zafarkhaja.semver.expr.ExpressionParser;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sandboxpowered.api.addon.Addon;
import org.sandboxpowered.api.util.Identifier;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Consumer;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import static org.sandboxpowered.silica.loading.AddonFinder.SANDBOX_TOML;

public class SandboxLoader {
    private static final Parser<Expression> PARSER = ExpressionParser.newInstance();
    private final Map<String, AddonDefinition> loadedAddons = new HashMap<>();
    private final Map<AddonDefinition, Addon> addonMap = new HashMap<>();
    private final Map<AddonDefinition, AddonSpecificAPIReference> addonAPIs = new HashMap<>();
    private final AddonFinder scanner = new AddonFinder.MergedFinder(
            new AddonFinder.DirectoryFinder(Paths.get("addons")),
            new AddonFinder.ClasspathFinder()
    );
    private final Map<String, AddonClassLoader> addonToClassLoader = new LinkedHashMap<>();
    public Logger log = LogManager.getLogger(SandboxLoader.class);
    private boolean loaded;

    private AddonSpecificAPIReference getAPIForAddon(AddonDefinition spec) {
        return addonAPIs.computeIfAbsent(spec, s -> new AddonSpecificAPIReference(s, this));
    }

    public Optional<AddonDefinition> getAddon(String addonId) {
        return Optional.ofNullable(loadedAddons.get(addonId));
    }

    public void loadAddon(AddonDefinition info, Addon addon) {
        loadedAddons.put(info.getId(), info);
        addonMap.put(info, addon);
    }

    private List<AddonDefinition> getLoadOrder() {
        Set<AddonDefinition> visited = new HashSet<>();
        List<AddonDefinition> loadOrder = new ArrayList<>();
        for (AddonDefinition info : getAllAddons().keySet()) {
            handleDependencies(visited, loadOrder, info);
        }
        return loadOrder;
    }

    private void loopInOrder(Consumer<AddonDefinition> consumer) {
        getLoadOrder().forEach(spec -> {
            if (!spec.getPlatformSupport(getPlatform()).canRun()) {
                throw new IllegalStateException(String.format("Addon %s cannot run on platform %s!", spec.getId(), getPlatform().toString()));
            }
            try {
                consumer.accept(spec);
            } catch (Exception e) {
                throw new RuntimeException(String.format("Loading for addon %s failed: %s", spec.getId(), e.getMessage()), e);
            }
        });
    }

    public Identifier getPlatform() {
        return Identifier.of("sandbox", "test");
    }

    private void handleDependencies(Set<AddonDefinition> visited, List<AddonDefinition> order, AddonDefinition spec) {
        if (visited.contains(spec)) return;
        visited.add(spec);
        Map<String, String> dependencies = spec.getDependencies();
        for (String dep : dependencies.keySet()) {
            Optional<AddonDefinition> optionalDep = getAddon(dep);
            if (!optionalDep.isPresent())
                throw new IllegalStateException(String.format("Addon %s depends on other addon %s that isn't loaded!", spec.getId(), dep));
            AddonDefinition dependency = optionalDep.get();
            String versionString = dependencies.get(dep);
            Expression version = PARSER.parse(versionString);
            if (!version.interpret(dependency.getVersion()))
                throw new IllegalStateException(String.format("Addon %s depends on %s version %s but found version %s instead!", spec.getId(), dep, versionString, dependency.getVersion().toString()));
            handleDependencies(visited, order, dependency);
        }
        order.add(spec);
    }

    public Map<AddonDefinition, Addon> getAllAddons() {
        return ImmutableMap.copyOf(addonMap);
    }

    public void unload() {
        log.info("Unloading Sandbox");
        loadedAddons.clear();
        addonMap.clear();
        addonAPIs.clear();
        addonToClassLoader.clear();
        loaded = false;
    }

    public AddonClassLoader getClassLoader(AddonDefinition spec, URL url) {
        return addonToClassLoader.computeIfAbsent(spec.getId(), addonId -> new AddonClassLoader(this, Addon.class.getClassLoader(), url, spec));
    }

    private void loadFromURLs(Collection<URI> urls) {
        if (urls.isEmpty()) {
            log.info("Loaded 0 addons");
        } else {
            log.info("Loading {} addons", urls.size());
            TomlParser parser = new TomlParser();
            for (URI uri : urls) {
                InputStream configStream = null;
                JarFile jarFile = null;
                try {
                    if (uri.toString().endsWith(".jar")) {
                        jarFile = new JarFile(new File(uri));
                        ZipEntry ze = jarFile.getEntry(SANDBOX_TOML);
                        if (ze != null)
                            configStream = jarFile.getInputStream(ze);
                    } else {
                        configStream = uri.resolve(SANDBOX_TOML).toURL().openStream();
                    }
                    if (configStream == null)
                        continue;
                    Config config = parser.parse(configStream);
                    URL url = uri.toURL();
                    AddonDefinition spec = AddonDefinition.from(config, url);
                    AddonClassLoader loader = getClassLoader(spec, url);
                    Class<?> mainClass = loader.loadClass(spec.getMainClass());
                    Object obj = mainClass.getConstructor().newInstance();
                    if (obj instanceof Addon) {
                        loadAddon(spec, (Addon) obj);
                    } else {
                        log.error("Unable to load addon '{}', main class not instance of Addon", spec.getId());
                    }
                } catch (IOException | NoSuchMethodException | ClassNotFoundException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    log.error("Unknown Error", e);
                    //TODO: Split these up to provide unique messages per error.
                } finally {
                    IOUtils.closeQuietly(configStream);
                    IOUtils.closeQuietly(jarFile);
                }
            }
        }
    }

    public void load() {
        try {
            loadFromURLs(scanner.findAddons());
        } catch (IOException e) {
            log.error("Failed to load classpath addons", e);
        }

        loopInOrder(spec -> addonMap.get(spec).setup(getAPIForAddon(spec)));
        loaded = true;
    }

    public boolean isAddonLoaded(String addonId) {
        return getAddon(addonId).isPresent();
    }

    public boolean isLoaded() {
        return loaded;
    }
}