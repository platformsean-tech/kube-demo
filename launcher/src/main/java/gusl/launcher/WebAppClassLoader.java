package gusl.launcher;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Each War needs it own class loader, and this is it.
 * <p>
 * Implements the servlet spec model (v2.3 section 9.7.2) for classloading,
 * which is different to the standard JDK model in that it delegates *after*
 * checking local repositories. This has the effect of isolating copies of
 * classes that exist in 2 webapps from each other.
 * <p>
 * This explains it well.
 * <p>
 * https://tomcat.apache.org/tomcat-7.0-doc/class-loader-howto.html
 *
 * @author dhudson
 */
public class WebAppClassLoader extends URLClassLoader {

    private final String theWebAppName;

    protected final ClassLoader theSystemLoader;

    private ClassLoader theParentParent;

    public WebAppClassLoader(String name, ClassLoader parent) {
        super(new URL[0], parent);
        theSystemLoader = getSystemClassLoader();
        theWebAppName = name;
        if (parent != null) {
            // if we have a parent of the parent, and it's not the system
            // classloader
            theParentParent = parent.getParent() != theSystemLoader ? parent.getParent() : null;
        }
    }

    public String getWebAppName() {
        return theWebAppName;
    }

    public void addWarImageToClassLoader(File location) throws MalformedURLException {
        File classes = new File(location, "WEB-INF/classes");
        addURL(classes.toURI().toURL());

        File libs = new File(location, "WEB-INF/lib");
        for (File jar : libs.listFiles()) {
            if (jar.getName().endsWith(".jar") || jar.getName().endsWith(".zip")) {
                addURL(jar.toURI().toURL());
            }
        }

        // Add root
        addURL(location.toURI().toURL());
    }

    public void dumpClassLoaderNames() {
        System.out.println("Classloaders: System " + theSystemLoader);
        System.out.println("Parents Parent " + theParentParent);
        System.out.println("Parent " + getParent());
        System.out.println("This class " + WebAppClassLoader.class.getClassLoader());
        System.out.println("Thread Context Loader " + Thread.currentThread().getContextClassLoader());
    }

    //    @Override
//    protected void addURL(URL url) {
//        System.err.println("Adding " + url.getPath());
//        super.addURL(url);
//    }
    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {

//        if(name.startsWith("javax.servlet")) {
//            System.out.println("Pesky javax.servlet ..."+name);
//            return theSystemLoader.loadClass(name);
//        }
        // First, check if the class has already been loaded
        Class<?> c = findLoadedClass(name);

        // Try the system loader first, to ensure that system classes are not
        // overridden by webapps. Note that this includes any classes,
        // including the javax.servlet classes
        if (c == null) {
            try {
                c = theSystemLoader.loadClass(name);
            } catch (ClassNotFoundException e) {
            }
        }

        // If an allowed class, load it locally first
        if (c == null) {
            try {
                // If still not found, then invoke findClass in order to find the class.
                c = findClass(name);
            } catch (ClassNotFoundException e) {
            }
        }

        if (c == null) {
            ClassLoader parent = getParent();
            if (parent != null) {
                try {
                    c = parent.loadClass(name);
                } catch (ClassNotFoundException e) {
                }
            }
        }

        if (c == null && theParentParent != null) {
            try {
                c = theParentParent.loadClass(name);
            } catch (ClassNotFoundException e) {
            }
        }

        // otherwise, and only if we have a parent, delegate to our parent
        // Note that within winstone, the only difference between this and the system
        // class loader we've already tried is that our parent might include the common/shared lib.
        if (c == null) {
            // We have no other hope for loading the class, so throw the class not found exception
            throw new ClassNotFoundException(name);
        }

        if (resolve) {
            resolveClass(c);
        }
        return c;
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        if ((name != null) && name.startsWith("/")) {
            name = name.substring(1);
        }
        return super.getResourceAsStream(name);
    }

    @Override
    public String toString() {
        return "WebAppClassLoader{" + "theWebAppName=" + theWebAppName + ", theSystemLoader=" + theSystemLoader + ", theParentParent=" + theParentParent + '}';
    }

}
