package gusl.launcher;

import gusl.launcher.model.WarDetails;
import io.undertow.server.handlers.PathHandler;
import io.undertow.servlet.api.DeploymentManager;
import javax.servlet.ServletException;

/**
 *
 * @author dhudson
 */
public class DeployerThread extends Thread {

    private final DeploymentManager theManager;
    private final PathHandler thePath;
    private final WarDetails theDetails;

    public DeployerThread(DeploymentManager manager, PathHandler path, WarDetails details) {
        theManager = manager;
        thePath = path;
        theDetails = details;
    }

    @Override
    public void run() {
        try {
            // Run the startup in the new class loader
            theManager.deploy();
            thePath.addPrefixPath("/" + theDetails.getName(), theManager.start());
        } catch (ServletException ex) {
            System.err.println("ERR002 Unable to deploy Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
}
