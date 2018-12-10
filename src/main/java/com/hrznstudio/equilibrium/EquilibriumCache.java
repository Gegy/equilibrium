package com.hrznstudio.equilibrium;

import org.gradle.api.Project;

import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class EquilibriumCache {
    private final Path cacheRoot;

    public EquilibriumCache(Path cacheRoot) {
        this.cacheRoot = cacheRoot;
        try {
            Files.createDirectories(cacheRoot);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create cache root");
        }
    }

    public static EquilibriumCache from(Project project) {
        File homeDir = project.getGradle().getGradleUserHomeDir();
        Path cacheRoot = homeDir.toPath().resolve("caches/equilibrium");
        return new EquilibriumCache(cacheRoot);
    }

    public Path file(String path) {
        return this.cacheRoot.resolve(path);
    }

    public Reader reader(String path) throws IOException {
        return Files.newBufferedReader(this.cacheRoot.resolve(path));
    }

    public Writer writer(String path) throws IOException {
        return Files.newBufferedWriter(this.cacheRoot.resolve(path));
    }
}
