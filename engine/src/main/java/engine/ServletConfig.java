package engine;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class ServletConfig implements ServletContextListener {
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try {
            sce.getServletContext().addServlet("ApiServlet", ApiServlet.class)
                .addMapping("/api");
            System.out.println("Servelet Registered...");
        } catch (Exception e) {
            System.err.println("Failed to register servlet: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
    }
}

