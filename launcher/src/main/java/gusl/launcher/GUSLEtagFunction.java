package gusl.launcher;

import io.undertow.server.handlers.resource.PathResourceManager.ETagFunction;
import io.undertow.util.ETag;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

/**
 *
 * @author dhudson
 */
public class GUSLEtagFunction implements ETagFunction {

    private final ThreadLocal<MessageDigest> HTTP_MD5
            = new ThreadLocal<MessageDigest>() {
        @Override
        protected MessageDigest initialValue() {
            MessageDigest digest = null;
            try {
                digest = MessageDigest.getInstance("MD5");
                // Tidy up first..
                digest.reset();
            } catch (NoSuchAlgorithmException ignore) {
                // This will not happen, but just in case
                System.err.println("MD5 algorithm not found, unable to respond to HTTP request");
            }
            return digest;
        }
    };

    private final HashMap<Path, ETag> theEtagMap;

    public GUSLEtagFunction() {
        theEtagMap = new HashMap<>();
    }

    @Override
    public ETag generate(Path path) {
        ETag etag = theEtagMap.get(path);
        if (etag != null) {
            return etag;
        }

        ByteBuffer buf = ByteBuffer.allocate(16);
        MessageDigest algorithm = HTTP_MD5.get();
        File file = path.toFile();

        algorithm.update(file.getAbsolutePath().getBytes());
        // Remove the millis from the date
        long lastModified = file.lastModified() / 1000 * 1000;
        buf.putLong(lastModified);
        buf.flip();
        algorithm.update(buf);

        byte[] digest = algorithm.digest();
        buf.position(0);
        buf.limit(buf.capacity());
        buf.put(digest);
        buf.flip();

        StringBuilder builder = new StringBuilder(10);
        builder.append(Long.toHexString(buf.getLong()));

        etag = new ETag(false, builder.toString());
        theEtagMap.put(path, etag);
        return etag;
    }

}
