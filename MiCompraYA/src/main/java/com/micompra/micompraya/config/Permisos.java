package com.micompra.micompraya.config;


import org.springframework.stereotype.Component;
import com.micompra.micompraya.models.Usuario;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;


@Component
@WebFilter("/*")
public class Permisos implements  Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        SpringBeanAutowiringSupport.processInjectionBasedOnServletContext(this, filterConfig.getServletContext());
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        HttpSession session = httpRequest.getSession(false);

        String path = httpRequest.getRequestURI();

        // Rutas públicas (sin necesidad de login)
        if (isPublicPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        if (session != null && session.getAttribute("usuario") != null) {
            // Si está logueado, verificar si tiene acceso a la ruta
            if (hasAccess(path, session)) {
                chain.doFilter(request, response);
            } else {
                // No tiene permiso para la ruta
                httpResponse.sendRedirect(httpRequest.getContextPath() + "/accesoNegado");
            }
        } else {
            // No está logueado, redirigir al login
            httpResponse.sendRedirect(httpRequest.getContextPath() + "/login");
        }
    }

    private boolean isPublicPath(String path) {
        // Agrega aquí las rutas que deben ser públicas
        return path.startsWith("/resources")
                || path.startsWith("/static")
                || path.startsWith("/css")
                || path.startsWith("/js")
                || path.startsWith("/images/productos")
                || path.equals("/")
                || path.endsWith("/login")
                || path.endsWith("/logout")
                || path.endsWith("/registrar")
                || path.endsWith("/acercaDe")
                || path.startsWith("/icons")

                || path.endsWith("/contactenos")
                || path.endsWith("/contacto/enviar")
                || path.endsWith("/mensajes_view")
                || path.endsWith("/mensajes/responder")
                || path.endsWith("/recuperar-contrasena")
                || path.endsWith("/recuperar-contrasena/enviar")
                || path.endsWith("/verificar-correo")
                || path.endsWith("/producto")
                || path.startsWith("/webhook")
                || path.endsWith("/login/firebase")
                || path.endsWith("/completar-registro")
                || path.endsWith("/registrar-google")
                || path.startsWith("/carrito")
                ;

    }
    private boolean hasAccess(String path, HttpSession session) {
        Object usuarioObj = session.getAttribute("usuario");
        if (usuarioObj instanceof Usuario) {
            Usuario usuario = (Usuario) usuarioObj;
            String rol = usuario.getRol().getRol();

            // Rutas solo para ADMIN
            if (path.startsWith("/usuarios")) {
                return "Administrador".equals(rol) || "Supervisor".equals(rol);
            }
            // 1. CAJA: Admin y Cajero
            if (path.startsWith("/caja")) {
                return "Administrador".equals(rol) || "Cajero".equals(rol);
            }

            // 2. REPORTES: Solo Admin
            if (path.startsWith("/reportes")) {
                return "Administrador".equals(rol);
            }

        }
        return true; // El resto de rutas protegidas solo requieren login
    }

    @Override
    public void destroy() {}
}
