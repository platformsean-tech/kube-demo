package gusl.launcher.model;

import java.io.File;

/**
 * @author dhudson
 */
public class StaticDetails {

    private String name;
    private File location;
    private String context;

    public StaticDetails() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getLocation() {
        return location;
    }

    public void setLocation(File location) {
        this.location = location;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

}
