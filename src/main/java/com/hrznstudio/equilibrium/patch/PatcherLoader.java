package com.hrznstudio.equilibrium.patch;

import com.hrznstudio.spark.patch.IBytePatcher;
import com.hrznstudio.spark.patch.IPatchPlugin;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class PatcherLoader {
    private final Collection<Path> patcherJars;

    public PatcherLoader(Collection<Path> patcherJars) {
        this.patcherJars = patcherJars;
    }

    public Collection<IBytePatcher> loadPatchers() {
        Collection<IPatchPlugin> plugins = this.loadPlugins();
        plugins.forEach(IPatchPlugin::initialize);

        return plugins.stream()
                .flatMap(p -> p.getPatchers().stream())
                .collect(Collectors.toList());
    }

    private Collection<IPatchPlugin> loadPlugins() {
        URL[] urls = this.patcherJars.stream()
                .map(PatcherLoader::toUrl)
                .toArray(URL[]::new);

        ClassLoader classLoader = new URLClassLoader(urls, this.getClass().getClassLoader());
        ServiceLoader<IPatchPlugin> loader = ServiceLoader.load(IPatchPlugin.class, classLoader);

        Collection<IPatchPlugin> plugins = new ArrayList<>();
        loader.forEach(plugins::add);

        return plugins;
    }

    private static URL toUrl(Path path) {
        try {
            return path.toUri().toURL();
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
    }
}
