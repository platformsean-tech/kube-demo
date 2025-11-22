package gusl.launcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import gusl.launcher.model.WarDetails;
import org.apache.commons.text.StringSubstitutor;

import java.io.*;
import java.net.URL;
import java.nio.file.FileSystem;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

/**
 * Casanova Utils.
 *
 * @author dhudson
 */
public class LauncherUtils {

    private static Attributes theManifestAttrs;

    private static final Pattern PROPERTY_PATTERN = Pattern.compile("\\$\\{(.+?)\\}");

    private LauncherUtils() {
    }

    /**
     * Extract a war file at a give location.
     *
     * @param warLocation
     * @param baseLocation
     * @param path
     * @param expand
     * @return
     * @throws IOException
     */
    public static WarDetails extract(String warLocation, File baseLocation, String path, boolean expand) throws IOException {
        File warFile = new File(warLocation);
        if (!warFile.exists()) {
            throw new IOException("No file at location" + warLocation);
        }

        //String warName = getFileBaseName(warFile);
        // Current location, this may need to change
        File location = new File(baseLocation, "undertow" + File.separator + path);

        if (location.exists()) {
            if (expand) {
                removeAllDirectories(location.toPath());
            }
        } else {
            Files.createDirectories(location.toPath());
        }

        JarFile jarFile = new JarFile(warFile);
        WarDetails details = new WarDetails(path);
        details.setLocation(location);

        if (expand) {
            Enumeration<JarEntry> enums = jarFile.entries();
            while (enums.hasMoreElements()) {
                JarEntry entry = enums.nextElement();

                File toWrite = new File(location, entry.getName());

                if (entry.isDirectory()) {
                    toWrite.mkdirs();
                    continue;
                }

                copyStream(jarFile.getInputStream(entry), new FileOutputStream(toWrite));
            }
        }

        // Load the Manifest from disk
        File manifestFile = new File(location, LauncherConstants.JAR_MANIFEST_LOCATION);
        Properties properties = new Properties();
        properties.load(new FileInputStream(manifestFile));

        details.setManifest(properties);

        return details;
    }

    private static void loadAttrs() {
        try {
            Class<?> clazz = WarDetails.class;
            String className = clazz.getSimpleName() + ".class";
            String classPath = clazz.getResource(className).toString();
            if (!classPath.startsWith("jar")) {
                // Class not from JAR
                return;
            }
            String manifestPath = classPath.substring(0, classPath.lastIndexOf("!") + 1)
                    + "/META-INF/MANIFEST.MF";
            Manifest manifest = new Manifest(new URL(manifestPath).openStream());
            theManifestAttrs = manifest.getMainAttributes();
        } catch (IOException ex) {
        }
    }

    private static Attributes getManifestAttrs() {
        if (theManifestAttrs == null) {
            loadAttrs();
        }

        return theManifestAttrs;
    }

    public static String getVersion() {
        if (getManifestAttrs() != null) {
            return getManifestAttrs().getValue(LauncherConstants.BUILD_VERSION_KEY);
        }

        return "unknown";
    }

    public static String getTimestamp() {
        if (getManifestAttrs() != null) {
            return getManifestAttrs().getValue(LauncherConstants.BUILD_TIMESTAMP_KEY);
        }

        return "unknown";
    }

    /**
     * Returns the file name of the given file without path or extension
     *
     * @param file to process
     * @return The filename without path or extension
     * @since 1.0
     */
    public static String getFileBaseName(File file) {
        final String fullPath = file.getName();

        final int dot = fullPath.lastIndexOf('.');
        final int sep = fullPath.lastIndexOf(File.pathSeparatorChar);

        return fullPath.substring(sep + 1, dot);
    }

    /**
     * Copy the input stream to the output stream
     *
     * @param inputStream  inbound stream
     * @param outputStream outbound stream
     * @throws IOException if unable to copy streams
     * @since 1.0
     */
    public static void copyStream(InputStream inputStream,
                                  OutputStream outputStream) throws IOException {
        inputStream.transferTo(outputStream);
    }

    public static void removeAllDirectories(Path rootPath) throws IOException {
        Files.walk(rootPath, FileVisitOption.FOLLOW_LINKS)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }

    /**
     * Close a closeable resource, catching and discarding any IOExceptions that
     * may arise.
     *
     * @param resource to close
     * @since 1.0
     */
    public final static void closeQuietly(Closeable resource) {
        if (resource == null) {
            return;
        }
        try {
            resource.close();
        } catch (final IOException ignore) {
        }
    }

    public static <T> T loadConfig(Class<T> config, String fileName, ObjectMapper mapper) throws IOException {
        try (InputStream is = new FileInputStream(new File(fileName))) {
            return mapper.readValue(is, config);
        } catch (IOException ex) {
            throw ex;
        }
    }

    public static String resolve(String path, Map<String, String> properties) {
        StringSubstitutor sub = new StringSubstitutor(properties);
        return sub.replace(path);
    }

    public static void unzip(String zipFilePath, String destDir, String zipRoot) throws IOException {
        File file = new File(zipFilePath);
        FileSystem zipFs = FileSystems.newFileSystem(file.toPath(), LauncherUtils.class.getClassLoader());

        File location = new File(destDir);
        Path targetDir = location.toPath();

        if (location.exists()) {
            removeAllDirectories(targetDir);
        } else {
            Files.createDirectories(targetDir);
        }

        Path pathInZip;
        if (zipRoot == null) {
            pathInZip = zipFs.getPath("/");
        } else {
            pathInZip = zipFs.getPath("/", zipRoot);
        }

        Files.walkFileTree(pathInZip, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path filePath, BasicFileAttributes attrs) throws IOException {
                // Make sure that we conserve the hierarchy of files and folders inside the zip
                Path relativePathInZip = pathInZip.relativize(filePath);
                Path targetPath = targetDir.resolve(relativePathInZip.toString());
                Files.createDirectories(targetPath.getParent());

                // And extract the file
                Files.copy(filePath, targetPath);

                return FileVisitResult.CONTINUE;
            }
        });
    }
}
