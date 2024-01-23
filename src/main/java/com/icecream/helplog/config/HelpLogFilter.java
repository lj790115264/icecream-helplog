package com.icecream.helplog.config;

import cn.hutool.core.util.ObjectUtil;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import com.icecream.helplog.util.HelpLog;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author andre.lan
 */
@Component
public class HelpLogFilter extends OncePerRequestFilter {

    private static final String TRACE_ID_HEADER = "x-traceId-header";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        HelpLog.remove();

        try {
            String traceId = request.getHeader(TRACE_ID_HEADER);
            if (ObjectUtil.isNotEmpty(traceId)) {
                // header有traceId
                HelpLog.add(traceId);
            } else {
                // 没有拿到header
                HelpLog.info("接口开始");
            }
            filterChain.doFilter(request, response);
        } finally {
            HelpLog.remove();
        }
    }
}
