package uk.gov.digital.ho.hocs.audit.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@SpringBootTest
public class RequestDataTest {

    @Mock
    private HttpServletRequest mockHttpServletRequest;

    private final RequestData requestData = new RequestData();

    @Mock
    private HttpServletResponse mockHttpServletResponse;

    @Mock
    private Object mockHandler;

    @BeforeEach
    public void before() {
        requestData.clear();
    }

    @Test
    public void shouldDefaultRequestData() {
        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertNotNull(requestData.getCorrelationId());
        assertEquals("anonymous", requestData.getUserId());
        assertEquals("anonymous", requestData.getUsername());
        assertTrue(requestData.getGroups().isEmpty());
        assertTrue(requestData.getRoles().isEmpty());
    }

    @Test
    public void shouldUseCorrelationIdFromRequest() {
        when(mockHttpServletRequest.getHeader("X-Correlation-Id")).thenReturn("some correlation id");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertEquals("some correlation id", requestData.getCorrelationId());
    }

    @Test
    public void shouldUseUserIdFromRequest() {
        when(mockHttpServletRequest.getHeader("X-Auth-UserId")).thenReturn("some user id");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertEquals("some user id", requestData.getUserId());
    }

    @Test
    public void shouldUseUsernameFromRequest() {
        when(mockHttpServletRequest.getHeader("X-Auth-Username")).thenReturn("some username");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertEquals("some username", requestData.getUsername());
    }

    @Test
    public void shouldUseGroupsFromRequest() {
        when(mockHttpServletRequest.getHeader("X-Auth-Groups")).thenReturn("some groups");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertEquals("some groups", requestData.getGroups());
    }

    @Test
    public void shouldUseRolesFromRequest() {
        when(mockHttpServletRequest.getHeader("X-Auth-Roles")).thenReturn("some roles");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertEquals("some roles", requestData.getRoles());
    }

    @Test
    public void shouldParseMessageHeadersFromMapCorrelation() {

        Map<String, String> headers = new HashMap<>();
        UUID correlationId = UUID.randomUUID();
        headers.put("X-Correlation-Id", correlationId.toString());

        requestData.parseMessageHeaders(headers);

        assertEquals(correlationId.toString(), requestData.getCorrelationId());
    }

    @Test
    public void shouldParseMessageHeadersFromMapUserId() {

        Map<String, String> headers = new HashMap<>();
        UUID userId = UUID.randomUUID();
        headers.put("X-Auth-UserId", userId.toString());

        requestData.parseMessageHeaders(headers);

        assertEquals(userId.toString(), requestData.getUserId());
    }

    @Test
    public void shouldParseMessageHeadersFromMapUserName() {

        Map<String, String> headers = new HashMap<>();
        String userName = "Some userName";
        headers.put("X-Auth-Username", userName);

        requestData.parseMessageHeaders(headers);

        assertEquals(userName, requestData.getUsername());
    }

    @Test
    public void shouldParseMessageHeadersFromMapGroups() {

        Map<String, String> headers = new HashMap<>();
        String groups = "Some Groups";
        headers.put("X-Auth-Groups", groups);

        requestData.parseMessageHeaders(headers);

        assertEquals(groups, requestData.getGroups());
    }

    @Test
    public void shouldParseMessageHeadersFromMapRoles() {

        Map<String, String> headers = new HashMap<>();
        String roles = "Some Roles";
        headers.put("X-Auth-Roles", roles);

        requestData.parseMessageHeaders(headers);

        assertEquals(roles, requestData.getRoles());
    }

    @Test
    public void shouldGetUserUUIDNotNull() {
        requestData.parseMessageHeaders(Map.of());
        assertNotNull(requestData.getUserId());
    }

}
