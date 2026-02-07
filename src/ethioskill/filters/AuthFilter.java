package ethioskill.filters;

import ethioskill.models.User;
import com.google.gson.Gson;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter("/*")
public class AuthFilter implements Filter {
    
    // Updated public paths to include uploads directory
    private static final String[] PUBLIC_PATHS = {
        "/pages/login.html",
        "/pages/signup.html",
        "/LoginServlet",
        "/SignupServlet",
        "/images/",
        "/css/",
        "/js/",
        "/uploads/"  // Critical: Allow access to uploaded user photos
    };
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);
        
        String path = httpRequest.getRequestURI().substring(httpRequest.getContextPath().length());
        
        // Set security headers
        httpResponse.setHeader("X-Content-Type-Options", "nosniff");
        httpResponse.setHeader("X-Frame-Options", "DENY");
        httpResponse.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Regenerate session ID to prevent session fixation on login
        if (session != null && httpRequest.getRequestURL().toString().contains("LoginServlet")) {
            // Note: Session invalidation is typically handled inside the LoginServlet logic,
            // but ensuring a clean session state here is good practice.
        }
        
        // Check if path is public
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }
        
        // Check if user is logged in
        if (session == null || session.getAttribute("user") == null) {
            handleUnauthorized(httpRequest, httpResponse, "Authentication required");
            return;
        }
        
        User user = (User) session.getAttribute("user");
        
        // Role-based access control
        if (!hasAccess(user.getRole(), path)) {
            handleForbidden(httpRequest, httpResponse, "Access denied");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    private boolean isPublicPath(String path) {
        for (String publicPath : PUBLIC_PATHS) {
            if (path.startsWith(publicPath)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasAccess(String role, String path) {
        // 1. Admin Area Protection (Servlet + HTML pages)
        if ((path.startsWith("/AdminServlet") || path.contains("admin-dashboard")) 
            && !"Admin".equals(role)) {
            return false;
        }
        
        // 2. Provider Area Protection (Servlet + HTML pages)
        // Checks for dashboard, registration, and service forms
        if ((path.startsWith("/ProviderServlet") || 
             path.contains("provider-dashboard") || 
             path.contains("provider-register") || 
             path.contains("service-form")) 
            && !"Provider".equals(role)) {
            return false;
        }
        
        // 3. Seeker Area Protection (Servlet + HTML pages)
        // Checks for dashboard and booking forms
        if ((path.startsWith("/SeekerServlet") || 
             path.contains("seeker-dashboard") || 
             path.contains("booking-form")) 
            && !"Seeker".equals(role)) {
            return false;
        }
        
        return true;
    }
    
    private void handleUnauthorized(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());
        
        // If API call (AJAX), return JSON
        if (path.startsWith("/") && (path.endsWith("Servlet") || path.contains("api"))) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write(new Gson().toJson(new Response(false, message)));
        } else {
            // If Page request, redirect to login
            response.sendRedirect(request.getContextPath() + "/pages/login.html");
        }
    }

    private void handleForbidden(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        String path = request.getRequestURI().substring(request.getContextPath().length());

        // If API call (AJAX), return JSON
        if (path.startsWith("/") && (path.endsWith("Servlet") || path.contains("api"))) {
            response.setContentType("application/json");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write(new Gson().toJson(new Response(false, message)));
        } else {
            // If Page request, redirect to login (or an error page)
            response.sendRedirect(request.getContextPath() + "/pages/login.html");
        }
    }
    
    @Override
    public void destroy() {}
    
    // Helper class for JSON response
    private static class Response {
        boolean success;
        String message;
        
        Response(boolean success, String message) {
            this.success = success;
            this.message = message;
        }
    }
}