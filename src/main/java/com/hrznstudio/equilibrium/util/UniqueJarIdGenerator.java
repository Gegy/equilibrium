package com.hrznstudio.equilibrium.util;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Collection;
import java.util.Random;

public class UniqueJarIdGenerator {
    public static String generateUniqueId(Collection<Path> patcherJars) throws IOException {
        byte[] hashBytes = HashUtil.md5(patcherJars);
        return Base64.getEncoder().encodeToString(hashBytes);
    }

    public static String generateRandomId() {
        byte[] bytes = new byte[16];
        new Random().nextBytes(bytes);
        return Base64.getEncoder().encodeToString(bytes);
    }
}
