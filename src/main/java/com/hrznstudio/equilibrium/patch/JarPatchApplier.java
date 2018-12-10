package com.hrznstudio.equilibrium.patch;

import com.hrznstudio.spark.patch.IBytePatcher;
import com.hrznstudio.spark.patch.JarPatcher;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;

public class JarPatchApplier {
    private final Collection<IBytePatcher> patchers;

    public JarPatchApplier(Collection<IBytePatcher> patchers) {
        this.patchers = patchers;
    }

    public void patchJar(Path input, Path output) throws IOException {
        try (JarPatcher patcher = new JarPatcher(input, new Path[0], this.patchers)) {
            patcher.patch(output);
        }
    }
}
