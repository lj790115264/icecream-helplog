package com.yl;

import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;

/**
 * @author andre.lan
 */
public class DevUrlFilter extends OncePerRequestFilter {


    public static final String DEV_URL_TRACE_ID_HEADER = "x-dev-url-header";

    public static ThreadLocal<String> urlThreadLocal = new ThreadLocal<>();

    public DevUrlFilter() {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String uri = request.getHeader(DEV_URL_TRACE_ID_HEADER);
        if (StringUtils.isEmpty(uri)) {
            uri = request.getRequestURI();
        }

        urlThreadLocal.set(uri);

        try {
            filterChain.doFilter(request, response);
        } finally {

            urlThreadLocal.remove();
        }
    }
}
