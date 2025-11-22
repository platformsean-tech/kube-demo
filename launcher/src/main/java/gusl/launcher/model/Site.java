package gusl.launcher.model;

import java.util.List;
import java.util.Map;

/**
 *
 * @author dhudson
 */
public class Site {

    private String name;
    private Map<String, String> systemProperties;
    private List<WebApp> webApps;

    public Site() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Map<String, String> getSystemProperties() {
        return systemProperties;
    }

    public void setSystemProperties(Map<String, String> systemProperties) {
        this.systemProperties = systemProperties;
    }

    public List<WebApp> getWebApps() {
        return webApps;
    }

    public void setWebApps(List<WebApp> webApps) {
        this.webApps = webApps;
    }

}
