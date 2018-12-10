package com.hrznstudio.equilibrium.install.locator;

import org.gradle.api.Project;

import java.nio.file.Path;
import java.util.Collection;

public interface PotentialInstallLocator {
    boolean shouldUse();

    Collection<Path> findInstallLocations(Project project);
}
