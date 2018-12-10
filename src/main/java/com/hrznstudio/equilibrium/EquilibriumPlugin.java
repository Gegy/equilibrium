package com.hrznstudio.equilibrium;

import com.amadornes.artifactural.gradle.GradleRepositoryAdapter;
import com.hrznstudio.equilibrium.patch.JarPatchApplier;
import com.hrznstudio.equilibrium.patch.PatcherLoader;
import com.hrznstudio.equilibrium.util.UniqueJarIdGenerator;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

public class EquilibriumPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        EquilibriumCache cache = EquilibriumCache.from(project);

        ConfigurationContainer configurations = project.getConfigurations();
        Configuration patchConfig = configurations.create("patch", c -> c.setTransitive(false));
        Configuration loaderConfig = configurations.create("loader", c -> {});

        project.getRepositories().maven(m -> m.setUrl("https://maven.gegy1000.net/"));

        project.afterEvaluate(p -> {
            Collection<Path> patcherJars = patchConfig.getFiles().stream()
                    .map(File::toPath)
                    .filter(path -> path.getFileName().toString().endsWith(".jar"))
                    .collect(Collectors.toList());

            String uniqueId;
            try {
                uniqueId = UniqueJarIdGenerator.generateUniqueId(patcherJars);
            } catch (IOException e) {
                uniqueId = UniqueJarIdGenerator.generateRandomId();
                p.getLogger().error("Failed to generate unique jar id {}", uniqueId);
            }

            PatcherLoader patcherLoader = new PatcherLoader(patcherJars);
            JarPatchApplier patchApplier = new JarPatchApplier(patcherLoader.loadPatchers());

            File repoCache = cache.file("repo").toFile();
            EquilibriumRepository repository = new EquilibriumRepository(project, cache, patchApplier, uniqueId);
            GradleRepositoryAdapter.add(p.getRepositories(), "repo", repoCache, repository.create());
        });
    }
}
