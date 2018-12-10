package com.hrznstudio.equilibrium.install;

import com.hrznstudio.equilibrium.metadata.InstallMetadata;

import java.io.File;
import java.nio.file.Path;

public class GameInstall {
    private final Path path;
    private final InstallMetadata metadata;

    public GameInstall(Path path, InstallMetadata metadata) {
        this.path = path;
        this.metadata = metadata;
    }

    public Path getPath() {
        return this.path;
    }

    public File getFile() {
        return this.path.toFile();
    }

    public InstallMetadata getMetadata() {
        return this.metadata;
    }
}
