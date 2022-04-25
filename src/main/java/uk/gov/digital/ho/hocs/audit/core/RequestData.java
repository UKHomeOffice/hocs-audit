package uk.gov.digital.ho.hocs.audit.core;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class RequestData implements HandlerInterceptor {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    public static final String USER_ID_HEADER = "X-Auth-UserId";
    public static final String USERNAME_HEADER = "X-Auth-Username";
    public static final String USER_ROLES_HEADER = "X-Auth-Roles";

    private static final String ANONYMOUS = "anonymous";
    private static final String BLANK = "";


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MDC.clear();
        MDC.put(CORRELATION_ID_HEADER, initialiseCorrelationId(request));
        MDC.put(USER_ID_HEADER, initialiseUserId(request));
        MDC.put(USERNAME_HEADER, initialiseUserName(request));
        MDC.put(USER_ROLES_HEADER, initialiseUserRoles(request));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        MDC.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        response.setHeader(USER_ID_HEADER, userId());
        response.setHeader(USERNAME_HEADER, userId());
        response.setHeader(CORRELATION_ID_HEADER, correlationId());
        MDC.clear();
    }

    private String initialiseCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);
        return !isNullOrEmpty(correlationId) ? correlationId : UUID.randomUUID().toString();
    }

    private String initialiseUserId(HttpServletRequest request) {
        String userId = request.getHeader(USER_ID_HEADER);
        return !isNullOrEmpty(userId) ? userId : ANONYMOUS;
    }

    private String initialiseUserName(HttpServletRequest request) {
        String username = request.getHeader(USERNAME_HEADER);
        return !isNullOrEmpty(username) ? username : ANONYMOUS;
    }

    private String initialiseUserRoles(HttpServletRequest request) {
        String userRoles = request.getHeader(USER_ROLES_HEADER);
        return !isNullOrEmpty(userRoles) ? userRoles : BLANK;
    }


    public String correlationId() {
        return MDC.get(CORRELATION_ID_HEADER);
    }

    public String userId() {
        return MDC.get(USER_ID_HEADER);
    }

    public String username() {
        return MDC.get(USERNAME_HEADER);
    }

    public List<String> roles() {
        return Arrays.asList(MDC.get(USER_ROLES_HEADER).split(","));
    }

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.equals("");
    }

}
