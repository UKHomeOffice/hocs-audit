package uk.gov.digital.ho.hocs.audit.core;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class RequestData implements HandlerInterceptor {

    static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    static final String USER_ID_HEADER = "X-Auth-UserId";
    static final String USERNAME_HEADER = "X-Auth-Username";
    static final String GROUP_HEADER = "X-Auth-Groups";
    static final String USER_ROLES_HEADER = "X-Auth-Roles";

    private static final String ANONYMOUS = "anonymous";
    private static final String BLANK = "";

    private static boolean isNullOrEmpty(String value) {
        return value == null || value.equals("");
    }

    public void parseMessageHeaders(Map<String, String> headers) {
        if (headers.containsKey(CORRELATION_ID_HEADER)) {
            MDC.put(CORRELATION_ID_HEADER, headers.get(CORRELATION_ID_HEADER));
        }

        if (headers.containsKey(USER_ID_HEADER)) {
            MDC.put(USER_ID_HEADER, headers.get(USER_ID_HEADER));
        }

        if (headers.containsKey(USERNAME_HEADER)) {
            MDC.put(USERNAME_HEADER, headers.get(USERNAME_HEADER));
        }

        if (headers.containsKey(GROUP_HEADER)) {
            MDC.put(GROUP_HEADER, headers.get(GROUP_HEADER));
        }

        if (headers.containsKey(USER_ROLES_HEADER)) {
            MDC.put(USER_ROLES_HEADER, headers.get(USER_ROLES_HEADER));
        }
    }

    public void clear() {
        MDC.clear();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MDC.clear();
        MDC.put(CORRELATION_ID_HEADER, initialiseCorrelationId(request));
        MDC.put(USER_ID_HEADER, initialiseUserId(request));
        MDC.put(USERNAME_HEADER, initialiseUserName(request));
        MDC.put(GROUP_HEADER, initialiseGroups(request));
        MDC.put(USER_ROLES_HEADER, initialiseUserRoles(request));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) {
        MDC.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        response.setHeader(CORRELATION_ID_HEADER, correlationId());
        response.setHeader(USER_ID_HEADER, userId());
        response.setHeader(USERNAME_HEADER, username());
        response.setHeader(GROUP_HEADER, groups());
        response.setHeader(USER_ROLES_HEADER, roles());
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

    private String initialiseGroups(HttpServletRequest request) {
        String groups = request.getHeader(GROUP_HEADER);
        return !isNullOrEmpty(groups) ? groups : BLANK;
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

    public String groups() {
        return MDC.get(GROUP_HEADER);
    }

    public String roles() {
        return MDC.get(USER_ROLES_HEADER);
    }

    public List<String> rolesList() {
        return Arrays.asList(MDC.get(USER_ROLES_HEADER).split(","));
    }

}
