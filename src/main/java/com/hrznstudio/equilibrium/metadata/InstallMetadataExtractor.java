package com.hrznstudio.equilibrium.metadata;

import org.gradle.api.Project;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class InstallMetadataExtractor {
    private static final String MAIN_APP_CLASS = "main/MainApp.class";
    private static final String VERSION_CONSTANT = "VERSION_STRING";

    public static Optional<InstallMetadata> extractMetadata(Project project, Path jarPath) throws IOException {
        JarFile jar = new JarFile(jarPath.toFile());
        JarEntry mainAppEntry = jar.getJarEntry(MAIN_APP_CLASS);
        if (mainAppEntry == null) {
            project.getLogger().debug("Missing {} in {}", MAIN_APP_CLASS, jarPath);
            return Optional.empty();
        }

        try (InputStream input = jar.getInputStream(mainAppEntry)) {
            ClassNode node = new ClassNode();
            ClassReader reader = new ClassReader(input);
            reader.accept(node, 0);

            Optional<String> version = extractVersion(project, node).flatMap(v -> parseVersion(project, v));
            if (version.isPresent()) {
                return Optional.of(new InstallMetadata(version.get()));
            }
        }

        return Optional.empty();
    }

    private static Optional<String> extractVersion(Project project, ClassNode node) {
        List<FieldNode> fields = node.fields;
        for (FieldNode field : fields) {
            if (field.name.equals(VERSION_CONSTANT)) {
                if (!(field.value instanceof String)) {
                    project.getLogger().debug("Version constant was not of expected type String!");
                    continue;
                }
                return Optional.of((String) field.value);
            }
        }

        return Optional.empty();
    }

    private static Optional<String> parseVersion(Project project,String version) {
        if (!version.startsWith("Version ")) {
            project.getLogger().debug("Version string did not match expected pattern of 'Version X.X.X': {}", version);
            return Optional.empty();
        }
        return Optional.of(version.split(" ")[1]);
    }
}
