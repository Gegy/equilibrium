package com.hrznstudio.equilibrium.decompiler;

import org.jetbrains.java.decompiler.main.decompiler.ConsoleDecompiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class JarDecompiler {
    private static final String[] DEFAULT_ARGUMENTS = new String[] {
            "-dgs=true",
            "-rbr=true",
            "-jvn=1",
            "-sef=1",
            "-log=warn",
            "-ind=    ",
            "-rsy=1"
    };

    private final Path input;

    public JarDecompiler(Path input) {
        this.input = input;
        if (!Files.exists(input)) {
            throw new IllegalArgumentException("Decompiler input did not exist");
        }
    }

    public void decompileTo(Path output) throws IOException {
        Files.deleteIfExists(output);

        Path dumpDirectory = Files.createTempDirectory("decompOut");
        Path dumpFile = dumpDirectory.resolve(this.input.getFileName());

        try {
            String[] arguments = this.buildArguments(dumpDirectory);
            ConsoleDecompiler.main(arguments);

            Files.copy(dumpFile, output);
        } finally {
            Files.delete(dumpDirectory);
            Files.deleteIfExists(dumpFile);
        }
    }

    private String[] buildArguments(Path dumpDirectory) {
        Collection<String> arguments = new ArrayList<>();
        Collections.addAll(arguments, DEFAULT_ARGUMENTS);
        arguments.add(this.input.toAbsolutePath().toString());
        arguments.add(dumpDirectory.toAbsolutePath().toString());

        return arguments.toArray(new String[0]);
    }
}
