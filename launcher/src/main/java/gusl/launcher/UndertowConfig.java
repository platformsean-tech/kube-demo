package gusl.launcher;

/**
 * Separate Config for Undertow.
 * <p>
 * Stops conflicts with CLI Options and Undertow Options.
 * <p>
 * NB: Should look at buffer sizes, can configure them to something sensible.
 * <p>
 * The other socket options look sensible, so leave them alone
 *
 * @author dhudson
 */
public class UndertowConfig {

    // Defaults
    private int thePort = 8080;
    private String theHost = "localhost";
    private long entityMaxSize = 4096000L;
    private String corsHeaders = "";

    public UndertowConfig() {
    }

    public int getPort() {
        return thePort;
    }

    public void setPort(int port) {
        thePort = port;
    }

    public String getHost() {
        return theHost;
    }

    public void setHost(String host) {
        theHost = host;
    }

    public long getEntityMaxSize() {
        return entityMaxSize;
    }

    public void setEntityMaxSize(long entityMaxSize) {
        this.entityMaxSize = entityMaxSize;
    }

    public void setCorsHeaders(String headers) {
        corsHeaders = headers;
    }

    public String getCorsHeaders() {
        return corsHeaders;
    }

    @Override
    public String toString() {
        return theHost + ":" + thePort + " : Max Entity " + entityMaxSize + " : Cors Headers " + corsHeaders;
    }

}
