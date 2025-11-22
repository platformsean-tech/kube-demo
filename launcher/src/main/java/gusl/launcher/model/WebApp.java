package gusl.launcher.model;

/**
 * @author dhudson
 */
public class WebApp {

    private String context;
    private String war;
    private String site;
    private String config;
    private String path;
    private String name;
    private String zipRoot;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public WebApp() {
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public String getWar() {
        return war;
    }

    public void setWar(String war) {
        this.war = war;
    }

    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    public String getConfig() {
        return config;
    }

    public void setConfig(String config) {
        this.config = config;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getZipRoot() {
        return zipRoot;
    }

    public void setZipRoot(String zipRoot) {
        this.zipRoot = zipRoot;
    }
}
