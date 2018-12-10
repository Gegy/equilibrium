package com.hrznstudio.equilibrium;

import com.amadornes.artifactural.api.artifact.Artifact;
import com.amadornes.artifactural.api.artifact.ArtifactIdentifier;
import com.amadornes.artifactural.api.artifact.ArtifactType;
import com.amadornes.artifactural.api.repository.ArtifactProvider;
import com.amadornes.artifactural.api.repository.Repository;
import com.amadornes.artifactural.base.artifact.StreamableArtifact;
import com.amadornes.artifactural.base.repository.ArtifactProviderBuilder;
import com.amadornes.artifactural.base.repository.SimpleRepository;
import com.hrznstudio.equilibrium.decompiler.JarDecompiler;
import com.hrznstudio.equilibrium.install.GameInstall;
import com.hrznstudio.equilibrium.install.GameInstallCache;
import com.hrznstudio.equilibrium.install.locator.InstallLocator;
import com.hrznstudio.equilibrium.patch.JarPatchApplier;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Writer;
import org.gradle.api.Project;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Optional;

// TODO: how can we clean this up?
public class EquilibriumRepository implements ArtifactProvider<ArtifactIdentifier> {
    private static final String EQUILINOX_GROUP = "com.equilinox";
    private static final String EQUILINOX_NAME = "equilinox";

    private final Project project;
    private final EquilibriumCache cache;
    private final JarPatchApplier patchApplier;
    private final String uniqueId;

    private final GameInstallCache installCache;

    public EquilibriumRepository(Project project, EquilibriumCache cache, JarPatchApplier patchApplier, String uniqueId) {
        this.project = project;
        this.cache = cache;
        this.patchApplier = patchApplier;
        this.uniqueId = uniqueId;

        Collection<GameInstall> installs = InstallLocator.locateInstalls(project);
        this.installCache = GameInstallCache.extract(project, cache, installs);
    }

    public Repository create() {
        return SimpleRepository.of(ArtifactProviderBuilder.begin(ArtifactIdentifier.class).provide(this));
    }

    @Override
    public Artifact getArtifact(ArtifactIdentifier info) {
        if (info.getGroup().equals(EQUILINOX_GROUP) && info.getName().equals(EQUILINOX_NAME)) {
            return this.getEquilinoxArtifact(info);
        }
        return Artifact.none();
    }

    private Artifact getEquilinoxArtifact(ArtifactIdentifier identifier) {
        String extension = identifier.getExtension();
        String classifier = identifier.getClassifier();
        switch (extension) {
            case "jar":
                if ("sources".equals(classifier)) {
                    Path equilinoxSources = this.getEquilinoxSources(identifier);
                    if (equilinoxSources != null) {
                        return StreamableArtifact.ofFile(identifier, ArtifactType.SOURCE, equilinoxSources.toFile());
                    }
                } else {
                    Path equilinoxJar = this.getEquilinoxPatchedJar(identifier);
                    if (equilinoxJar != null) {
                        return StreamableArtifact.ofFile(identifier, ArtifactType.BINARY, equilinoxJar.toFile());
                    }
                }
            case "pom":
                return this.getEquilinoxPom(identifier);
        }
        return Artifact.none();
    }

    @Nullable
    private Path getEquilinoxRawJar(ArtifactIdentifier identifier) {
        // TODO: how to deal with more than one install located? (some sort of priority system?)
        Collection<GameInstall> installs = this.installCache.byVersion(identifier.getVersion());
        Optional<GameInstall> install = installs.stream().findFirst();

        return install.map(GameInstall::getPath).orElse(null);
    }

    @Nullable
    private Path getEquilinoxPatchedJar(ArtifactIdentifier identifier) {
        Path cacheRoot = this.getUniqueCacheRoot();
        Path patchJar = cacheRoot.resolve(identifier.getName() + "-" + identifier.getVersion() + "-patched.jar");
        if (!Files.exists(patchJar)) {
            Path rawJar = this.getEquilinoxRawJar(identifier);
            if (rawJar == null) {
                return null;
            }

            this.project.getLogger().lifecycle("Patching jar for {}", identifier.getVersion());
            try {
                this.patchApplier.patchJar(rawJar, patchJar);
            } catch (IOException e) {
                this.project.getLogger().error("Failed to patch jar for {}", identifier, e);
            }
        }

        if (Files.exists(patchJar)) {
            return patchJar;
        }

        return null;
    }

    @Nullable
    private Path getEquilinoxSources(ArtifactIdentifier identifier) {
        Path cacheRoot = this.getUniqueCacheRoot();
        Path sourceJar = cacheRoot.resolve(identifier.getName() + "-" + identifier.getVersion() + "-sources.jar");
        if (!Files.exists(sourceJar)) {
            Path patchedJar = this.getEquilinoxPatchedJar(identifier);
            if (patchedJar == null) {
                return null;
            }

            this.project.getLogger().lifecycle("Decompiling jar for {}", identifier.getVersion());
            try {
                JarDecompiler decompiler = new JarDecompiler(patchedJar);
                decompiler.decompileTo(sourceJar);
            } catch (IOException e) {
                this.project.getLogger().error("Failed to decompile jar for {}", identifier, e);
            }
        }

        if (Files.exists(sourceJar)) {
            return sourceJar;
        }

        return null;
    }

    private Path getUniqueCacheRoot() {
        Path cacheRoot = this.cache.file("jars-" + this.uniqueId);
        if (!Files.exists(cacheRoot)) {
            try {
                Files.createDirectories(cacheRoot);
            } catch (IOException e) {
                this.project.getLogger().error("Failed to create cache root for {}", this.uniqueId, e);
            }
        }
        return cacheRoot;
    }

    private Artifact getEquilinoxPom(ArtifactIdentifier identifier) {
        String cacheName = identifier.getName() + "-" + identifier.getVersion() + ".pom";
        Path cacheFile = this.cache.file(cacheName);

        if (!Files.exists(cacheFile)) {
            Model model = new Model();
            model.setModelVersion("4.0.0");

            model.setGroupId(identifier.getGroup());
            model.setArtifactId(identifier.getName());
            model.setVersion(identifier.getVersion());

            try (Writer writer = Files.newBufferedWriter(cacheFile)) {
                new MavenXpp3Writer().write(writer, model);
            } catch (IOException e) {
                this.project.getLogger().error("Failed to write pom for {}", identifier, e);
            }
        }

        if (Files.exists(cacheFile)) {
            return StreamableArtifact.ofFile(identifier, ArtifactType.OTHER, cacheFile.toFile());
        } else {
            return Artifact.none();
        }
    }
}
