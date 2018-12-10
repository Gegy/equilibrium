package com.hrznstudio.equilibrium.install;

import com.hrznstudio.equilibrium.util.HashUtil;
import com.hrznstudio.equilibrium.EquilibriumCache;
import com.hrznstudio.equilibrium.metadata.InstallMetadata;
import org.gradle.api.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class GameInstallCache {
    private static final String INSTALL_CACHE = "raw-versions";

    private final Collection<GameInstall> installs;
    private final Map<String, Collection<GameInstall>> byVersion = new HashMap<>();

    private GameInstallCache(Collection<GameInstall> installs) {
        this.installs = installs;
        for (GameInstall install : installs) {
            String version = install.getMetadata().getVersion();
            this.byVersion.computeIfAbsent(version, v -> new ArrayList<>()).add(install);
        }
    }

    public static GameInstallCache extract(Project project, EquilibriumCache cache, Collection<GameInstall> locatedInstalls) {
        Path versions = cache.file(INSTALL_CACHE);
        try {
            Files.createDirectories(versions);
        } catch (IOException e) {
            project.getLogger().error("Failed to create version cache directory", e);
        }

        Collection<GameInstall> cachedInstalls = new ArrayList<>(locatedInstalls.size());
        for (GameInstall locatedInstall : locatedInstalls) {
            Path installPath = locatedInstall.getPath();
            Path cachePath = getCacheLocation(versions, locatedInstall);
            try {
                if (!HashUtil.equivalent(installPath, cachePath)) {
                    Files.deleteIfExists(cachePath);
                    Files.copy(installPath, cachePath);
                }
                cachedInstalls.add(new GameInstall(cachePath, locatedInstall.getMetadata()));
            } catch (IOException e) {
                project.getLogger().error("Failed to extract install jar into cache", e);
            }
        }

        return new GameInstallCache(cachedInstalls);
    }

    private static Path getCacheLocation(Path cacheRoot, GameInstall install) {
        InstallMetadata metadata = install.getMetadata();
        return cacheRoot.resolve(metadata.getVersion() + ".jar");
    }

    public Collection<GameInstall> byVersion(String version) {
        return this.byVersion.getOrDefault(version, Collections.emptyList());
    }
}
