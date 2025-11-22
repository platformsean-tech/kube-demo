package wallet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class ApiServlet extends HttpServlet {
    
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");
        
        String path = extractPath(request);
        String result = route(path);
        
        if (result == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            result = "Not found: /" + path;
        }
        
        PrintWriter out = response.getWriter();
        out.print(result);
        out.flush();
    }
    
    private String extractPath(HttpServletRequest request) {
        String pathInfo = request.getPathInfo();
        if (pathInfo == null || pathInfo.equals("/")) {
            return "";
        }
        // Remove leading slash
        return pathInfo.startsWith("/") ? pathInfo.substring(1) : pathInfo;
    }
    
    private String route(String path) {
        switch (path) {
            case "":
            case "api":
                return "Hello from -> wallet";
            case "hello":
                return "Hello from -> wallet";
            case "goodbye":
                return "Goodbye from -> wallet";
            default:
                return null;
        }
    }
}

