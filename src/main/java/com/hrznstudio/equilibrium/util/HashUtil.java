package com.hrznstudio.equilibrium.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;

public class HashUtil {
    public static boolean equivalent(Path left, Path right) {
        if (!Files.exists(left) || !Files.exists(right)) {
            return false;
        }
        try {
            byte[] leftHash = md5(left);
            byte[] rightHash = md5(right);
            return Arrays.equals(leftHash, rightHash);
        } catch (Throwable t) {
            return false;
        }
    }

    public static byte[] md5(Path path) throws IOException {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            return algorithm.digest(Files.readAllBytes(path));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static byte[] md5(Collection<Path> paths) throws IOException {
        try {
            MessageDigest algorithm = MessageDigest.getInstance("MD5");
            try (ByteArrayOutputStream output = new ByteArrayOutputStream()) {
                for (Path path : paths) {
                    Files.copy(path, output);
                }
                output.flush();
                return algorithm.digest(output.toByteArray());
            }
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
