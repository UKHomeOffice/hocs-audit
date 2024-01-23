package uk.gov.digital.ho.hocs.audit.core;

import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.Objects;
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

    public void parseMessageHeaders(Map<String, String> headers) {
        MDC.clear();
        MDC.put(CORRELATION_ID_HEADER, initialiseCorrelationId(headers.get(CORRELATION_ID_HEADER)));
        MDC.put(USER_ID_HEADER, initialiseCorrelationId(headers.get(USER_ID_HEADER)));
        MDC.put(USERNAME_HEADER, initialiseCorrelationId(headers.get(USERNAME_HEADER)));
        MDC.put(GROUP_HEADER, initialiseCorrelationId(headers.get(GROUP_HEADER)));
        MDC.put(USER_ROLES_HEADER, initialiseCorrelationId(headers.get(USER_ROLES_HEADER)));
    }

    public void clear() {
        MDC.clear();
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        MDC.clear();
        MDC.put(CORRELATION_ID_HEADER, initialiseCorrelationId(request.getHeader(CORRELATION_ID_HEADER)));
        MDC.put(USER_ID_HEADER, initialiseUserId(request.getHeader(USER_ID_HEADER)));
        MDC.put(USERNAME_HEADER, initialiseUserName(request.getHeader(USERNAME_HEADER)));
        MDC.put(GROUP_HEADER, initialiseGroups(request.getHeader(GROUP_HEADER)));
        MDC.put(USER_ROLES_HEADER, initialiseUserRoles(request.getHeader(USER_ROLES_HEADER)));
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request,
                           HttpServletResponse response,
                           Object handler,
                           ModelAndView modelAndView) {
        MDC.clear();
    }

    @Override
    public void afterCompletion(HttpServletRequest request,
                                HttpServletResponse response,
                                Object handler,
                                Exception ex) {
        response.setHeader(CORRELATION_ID_HEADER, getCorrelationId());
        response.setHeader(USER_ID_HEADER, getUserId());
        response.setHeader(USERNAME_HEADER, getUsername());
        response.setHeader(GROUP_HEADER, getGroups());
        response.setHeader(USER_ROLES_HEADER, getRoles());
        MDC.clear();
    }

    private String initialiseCorrelationId(String value) {
        return Objects.requireNonNullElse(value, UUID.randomUUID().toString());
    }

    private String initialiseUserId(String value) {
        return Objects.requireNonNullElse(value, ANONYMOUS);
    }

    private String initialiseUserName(String value) {
        return Objects.requireNonNullElse(value, ANONYMOUS);
    }

    private String initialiseGroups(String value) {
        return Objects.requireNonNullElse(value, BLANK);
    }

    private String initialiseUserRoles(String value) {
        return Objects.requireNonNullElse(value, BLANK);
    }

    public String getCorrelationId() {
        return MDC.get(CORRELATION_ID_HEADER);
    }

    public String getUserId() {
        return MDC.get(USER_ID_HEADER);
    }

    public String getUsername() {
        return MDC.get(USERNAME_HEADER);
    }

    public String getGroups() {
        return MDC.get(GROUP_HEADER);
    }

    public String getRoles() {
        return MDC.get(USER_ROLES_HEADER);
    }

}
