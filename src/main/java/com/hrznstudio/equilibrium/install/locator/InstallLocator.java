package com.hrznstudio.equilibrium.install.locator;

import com.hrznstudio.equilibrium.install.GameInstall;
import com.hrznstudio.equilibrium.install.locator.windows.WindowsSteamLocator;
import com.hrznstudio.equilibrium.metadata.InstallMetadata;
import com.hrznstudio.equilibrium.metadata.InstallMetadataExtractor;
import org.gradle.api.Project;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

public class InstallLocator {
    private static final Collection<PotentialInstallLocator> INSTALL_LOCATORS = new ArrayList<>();

    static {
        // TODO: Locate steam roots based on steamapps/libraryfolders.vdf
        // TODO: Locate and cache based on running processes
        registerLocator(new WindowsSteamLocator());
    }

    public static void registerLocator(PotentialInstallLocator installProvider) {
        INSTALL_LOCATORS.add(installProvider);
    }

    public static Collection<GameInstall> locateInstalls(Project project) {
        Collection<Path> installCandidates = collectInstallCandidates(project);

        Collection<GameInstall> locatedInstalls = new ArrayList<>();
        for (Path installCandidate : installCandidates) {
            if (Files.exists(installCandidate)) {
                try {
                    Optional<InstallMetadata> metadata = InstallMetadataExtractor.extractMetadata(project, installCandidate);
                    metadata.ifPresent(meta -> locatedInstalls.add(new GameInstall(installCandidate, meta)));
                } catch (IOException e) {
                    project.getLogger().error("Failed to extract metadata from install candidate '{}'", installCandidate.toAbsolutePath());
                }
            }
        }

        return locatedInstalls;
    }

    private static Collection<Path> collectInstallCandidates(Project project) {
        return getApplicableLocators().stream()
                .flatMap(p -> p.findInstallLocations(project).stream())
                .collect(Collectors.toList());
    }

    private static Collection<PotentialInstallLocator> getApplicableLocators() {
        return INSTALL_LOCATORS.stream().filter(PotentialInstallLocator::shouldUse).collect(Collectors.toList());
    }
}
