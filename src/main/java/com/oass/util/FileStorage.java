package com.oass.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

/**
 * Saves uploaded files on disk under a random UUID name (the original name is kept only in the DB).
 * Folder = upload.dir from config.properties, default ~/oass-data/uploads.
 * Syllabus: Section B (java.io / java.nio file handling).
 */
public final class FileStorage {

    private FileStorage() {}

    private static Path uploadDir() {
        return Path.of(Config.get("upload.dir", System.getProperty("user.home") + "/oass-data/uploads"))
                   .toAbsolutePath().normalize();
    }

    /** Writes the bytes to a new file named {uuid}.{extension} and returns that stored name. */
    public static String save(byte[] data, String extension) throws IOException {
        Path dir = uploadDir();
        Files.createDirectories(dir);
        String storedName = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
        Files.write(dir.resolve(storedName), data);
        return storedName;
    }

    /** Returns the path of a stored file (refuses names that try to leave the upload folder). */
    public static Path find(String storedName) {
        Path dir = uploadDir();
        Path file = dir.resolve(storedName).normalize();
        if (!file.startsWith(dir)) {
            throw new IllegalArgumentException("Invalid file name");
        }
        return file;
    }

    /** Deletes a stored file; used to clean up when the database insert fails. */
    public static void delete(String storedName) {
        try {
            Files.deleteIfExists(find(storedName));
        } catch (IOException | RuntimeException ignored) {
            // nothing more to do: the file is unreferenced anyway
        }
    }

    /** "My Report.PDF" -> "pdf". Returns "" when there is no safe extension. */
    public static String extensionOf(String filename) {
        if (filename == null) return "";
        int dot = filename.lastIndexOf('.');
        if (dot < 0) return "";
        String ext = filename.substring(dot + 1).toLowerCase();
        return ext.matches("[a-z0-9]{1,10}") ? ext : "";
    }

    /** Strips any folder part (some browsers send a full path) and limits the length. */
    public static String cleanName(String submittedName) {
        if (submittedName == null || submittedName.isBlank()) return "submission";
        String name = submittedName.substring(Math.max(submittedName.lastIndexOf('/'), submittedName.lastIndexOf('\\')) + 1);
        return name.length() > 200 ? name.substring(name.length() - 200) : name;
    }
}
