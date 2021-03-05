package org.sandboxpowered.silica.loading

import java.io.IOException
import java.net.JarURLConnection
import java.net.URI
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.stream.Collectors

interface AddonFinder {
    @Throws(IOException::class)
    fun findAddons(): Collection<URI>
    class MergedFinder(private val finders: Set<AddonFinder>) : AddonFinder {
        constructor(vararg finders: AddonFinder) : this(setOf(*finders))
        constructor(finder: AddonFinder) : this(setOf(finder))

        @Throws(IOException::class)
        override fun findAddons(): Collection<URI> {
            val addons: MutableSet<URI> = HashSet()
            for (finder in finders) {
                addons.addAll(finder.findAddons())
            }
            return addons
        }
    }

    class DirectoryFinder(private val addonPath: Path) : AddonFinder {
        @Throws(IOException::class)
        override fun findAddons(): Collection<URI> {
            if (Files.notExists(addonPath)) Files.createDirectories(addonPath)
            Files.walk(addonPath, 1).use { stream ->
                return stream.filter { path: Path -> path.toString().endsWith(".jar") }
                    .map { obj: Path -> obj.toUri() }.collect(Collectors.toSet())
            }
        }
    }

    class ClasspathFinder : AddonFinder {
        @Throws(IOException::class)
        override fun findAddons(): Collection<URI> {
            val addons: MutableSet<URI> = HashSet()
            val enumeration = javaClass.classLoader.getResources(SANDBOX_TOML)
            while (enumeration.hasMoreElements()) {
                val url = getSource(SANDBOX_TOML, enumeration.nextElement())
                addons.add(url)
            }
            return addons
        }

        companion object {
            fun getSource(filename: String, resourceURL: URL): URI {
                return try {
                    val connection = resourceURL.openConnection()
                    if (connection is JarURLConnection) {
                        connection.jarFileURL.toURI()
                    } else {
                        val path = resourceURL.path
                        if (!path.endsWith(filename)) {
                            throw RuntimeException(
                                String.format(
                                    "Could not find code source for file '%s' and URL '%s'!",
                                    filename,
                                    resourceURL
                                )
                            )
                        }
                        URL(
                            resourceURL.protocol,
                            resourceURL.host,
                            resourceURL.port,
                            path.substring(0, path.length - filename.length)
                        ).toURI()
                    }
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            }
        }
    }

    companion object {
        const val SANDBOX_TOML = "sandbox.toml"
    }
}