package org.sandboxpowered.silica.loading;

import com.google.common.collect.Sets;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface AddonFinder {
    String SANDBOX_TOML = "sandbox.toml";

    Collection<URI> findAddons() throws IOException;

    class MergedFinder implements AddonFinder {
        private final Set<AddonFinder> finders;

        public MergedFinder(Set<AddonFinder> finders) {
            this.finders = finders;
        }

        public MergedFinder(AddonFinder... finders) {
            this(Sets.newHashSet(finders));
        }

        public MergedFinder(AddonFinder finder) {
            this(Collections.singleton(finder));
        }

        @Override
        public Collection<URI> findAddons() throws IOException {
            Set<URI> addons = new HashSet<>();
            for (AddonFinder finder : finders) {
                addons.addAll(finder.findAddons());
            }
            return addons;
        }
    }

    class DirectoryFinder implements AddonFinder {
        private final Path addonPath;

        public DirectoryFinder(Path path) {
            this.addonPath = path;
        }

        @Override
        public Collection<URI> findAddons() throws IOException {
            if (Files.notExists(addonPath)) Files.createDirectories(addonPath);
            try (Stream<Path> stream = Files.walk(addonPath, 1)) {
                return stream.filter(path -> path.toString().endsWith(".jar"))
                        .map(Path::toUri).collect(Collectors.toSet());
            }
        }
    }

    class ClasspathFinder implements AddonFinder {
        public static URI getSource(String filename, URL resourceURL) {
            try {
                URLConnection connection = resourceURL.openConnection();
                if (connection instanceof JarURLConnection) {
                    return ((JarURLConnection) connection).getJarFileURL().toURI();
                } else {
                    String path = resourceURL.getPath();
                    if (!path.endsWith(filename)) {
                        throw new RuntimeException(String.format("Could not find code source for file '%s' and URL '%s'!", filename, resourceURL));
                    }

                    return new URL(resourceURL.getProtocol(), resourceURL.getHost(), resourceURL.getPort(), path.substring(0, path.length() - filename.length())).toURI();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Collection<URI> findAddons() throws IOException {
            Set<URI> addons = new HashSet<>();
            Enumeration<URL> enumeration = getClass().getClassLoader().getResources(SANDBOX_TOML);
            while (enumeration.hasMoreElements()) {
                URI url = getSource(SANDBOX_TOML, enumeration.nextElement());
                addons.add(url);
            }
            return addons;
        }
    }
}