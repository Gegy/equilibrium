package com.hrznstudio.equilibrium.install.locator.windows;

import org.gradle.api.Project;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

public class WindowsSteamLocator extends WindowsInstallLocator {
    private static final String STEAM_JAR_LOCATION = "steamapps/common/Equilinox/EquilinoxWindows.jar";

    @Override
    public Collection<Path> findInstallLocations(Project project) {
        return this.findSteamRoots(project).stream()
                .map(root -> root.resolve(STEAM_JAR_LOCATION))
                .collect(Collectors.toList());
    }

    private Collection<Path> findSteamRoots(Project project) {
        Collection<Path> possibleSteamRoots = new ArrayList<>();
        for (Path root : FileSystems.getDefault().getRootDirectories()) {
            this.addDefaultRoots(possibleSteamRoots, root);
            this.addLikelyRoots(project, possibleSteamRoots, root);
        }
        return possibleSteamRoots;
    }

    private void addDefaultRoots(Collection<Path> possibleSteamRoots, Path root) {
        possibleSteamRoots.add(root.resolve(Paths.get("Program Files", "Steam")));
        possibleSteamRoots.add(root.resolve(Paths.get("Program Files (x86)", "Steam")));
    }

    private void addLikelyRoots(Project project, Collection<Path> possibleSteamRoots, Path root) {
        try (DirectoryStream<Path> directoryStream = Files.newDirectoryStream(root)) {
            for (Path child : directoryStream) {
                if (this.isLikelySteamRoot(child)) {
                    possibleSteamRoots.add(child);
                }
            }
        } catch (IOException e) {
            project.getLogger().error("Failed to list steam directories in drive root", e);
        }
    }

    private boolean isLikelySteamRoot(Path path) {
        Path fileName = path.getFileName();
        return fileName.startsWith("Steam") || fileName.endsWith("Steam");
    }
}
