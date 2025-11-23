package engine;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet(urlPatterns = {"/api", "/api/*"})
public class ApiServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        
        String pathInfo = request.getPathInfo();
        String path = (pathInfo == null || pathInfo.equals("/")) ? "" : pathInfo.substring(1);
        
        String result;
        switch (path) {
            case "":
            case "api":
                result = "Hello from -> engine";
                break;
            case "hello":
                result = "Hello from -> engine";
                break;
            case "goodbye":
                result = "Goodbye from -> engine";
                break;
            default:
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                result = "Not found: /" + path;
        }
        
        response.getWriter().print(result);
    }
    
}

