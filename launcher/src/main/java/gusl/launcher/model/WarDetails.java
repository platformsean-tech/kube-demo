package gusl.launcher.model;

import java.io.File;
import java.net.MalformedURLException;
import java.util.Properties;
import gusl.launcher.WebAppClassLoader;

/**
 * Simple Wrapper class around the war details.
 *
 * @author dhudson
 */
public class WarDetails {

    private final String theName;
    private File theLocation;
    private WebAppClassLoader theClassLoader;
    private File theConfigLocation;
    private Properties theManifest;

    public WarDetails(String name) {
        theName = name;
    }

    public String getName() {
        return theName;
    }

    public File getLocation() {
        return theLocation;
    }

    public void setLocation(File location) {
        this.theLocation = location;
    }

    public WebAppClassLoader createClassLoader() throws MalformedURLException {
        theClassLoader = new WebAppClassLoader(theName, WarDetails.class.getClassLoader());
        theClassLoader.addWarImageToClassLoader(theLocation);
        return theClassLoader;
    }

    public WebAppClassLoader getClassLoader() {
        return theClassLoader;
    }

    public void setConfigLocation(File configLocation) {
        theConfigLocation = configLocation;
    }

    public File getConfigLocation() {
        return theConfigLocation;
    }

    public Properties getManifest() {
        return theManifest;
    }

    public void setManifest(Properties theManifest) {
        this.theManifest = theManifest;
    }

    @Override
    public String toString() {
        return "WarDetails{" + "theName=" + theName + ", theLocation=" + theLocation + '}';
    }

}
