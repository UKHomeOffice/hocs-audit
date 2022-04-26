package uk.gov.digital.ho.hocs.audit.core;

import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestDataTest {
    @Mock
    private HttpServletRequest mockHttpServletRequest;

    private final RequestData requestData = new RequestData();
    @Mock
    private HttpServletResponse mockHttpServletResponse;
    @Mock
    private Object mockHandler;

    @AfterEach
    public void after() {
        requestData.clear();
    }

    @Test
    public void shouldDefaultRequestData() {
        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.correlationId()).isNotNull();
        assertThat(requestData.userId()).isEqualTo("anonymous");
        assertThat(requestData.username()).isEqualTo("anonymous");
        assertThat(requestData.groups()).isEmpty();
        assertThat(requestData.roles()).isEmpty();

    }

    @Test
    public void shouldUseCorrelationIdFromRequest() {
        when(mockHttpServletRequest.getHeader("X-Correlation-Id")).thenReturn("some correlation id");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.correlationId()).isEqualTo("some correlation id");
    }

    @Test
    public void shouldUseUserIdFromRequest() {
        when(mockHttpServletRequest.getHeader("X-Auth-UserId")).thenReturn("some user id");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.userId()).isEqualTo("some user id");
    }

    @Test
    public void shouldUseUsernameFromRequest() {
        when(mockHttpServletRequest.getHeader("X-Auth-Username")).thenReturn("some username");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.username()).isEqualTo("some username");
    }

    @Test
    public void shouldUseGroupsFromRequest() {
        when(mockHttpServletRequest.getHeader("X-Auth-Groups")).thenReturn("some groups");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.groups()).isEqualTo("some groups");
    }

    @Test
    public void shouldUseRolesFromRequest() {
        when(mockHttpServletRequest.getHeader("X-Auth-Roles")).thenReturn("some roles");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.username()).isEqualTo("some roles");
    }

    @Test
    public void shouldParseMessageHeadersFromMapCorrelation() {

        Map<String, String> headers = new HashMap<>();
        UUID correlationId = UUID.randomUUID();
        headers.put("X-Correlation-Id", correlationId.toString());

        requestData.parseMessageHeaders(headers);

        assertThat(requestData.correlationId()).isEqualTo(correlationId.toString());
    }

    @Test
    public void shouldParseMessageHeadersFromMapUserId() {

        Map<String, String> headers = new HashMap<>();
        UUID userId = UUID.randomUUID();
        headers.put("X-Auth-UserId", userId.toString());

        requestData.parseMessageHeaders(headers);

        assertThat(requestData.userId()).isEqualTo(userId.toString());
    }

    @Test
    public void shouldParseMessageHeadersFromMapUserName() {

        Map<String, String> headers = new HashMap<>();
        String userName = "Some userName";
        headers.put("X-Auth-Username", userName);

        requestData.parseMessageHeaders(headers);

        assertThat(requestData.username()).isEqualTo(userName);
    }

    @Test
    public void shouldParseMessageHeadersFromMapGroups() {

        Map<String, String> headers = new HashMap<>();
        String groups = "Some Groups";
        headers.put("X-Auth-Groups", groups);

        requestData.parseMessageHeaders(headers);

        assertThat(requestData.groups()).isEqualTo(groups);
    }

    @Test
    public void shouldParseMessageHeadersFromMapRoles() {

        Map<String, String> headers = new HashMap<>();
        String roles = "Some Roles";
        headers.put("X-Auth-Roles", roles);

        requestData.parseMessageHeaders(headers);

        assertThat(requestData.roles()).isEqualTo(roles);
    }

    @Test
    public void shouldGetUserUUIDNotNull() {
        assertThat(requestData.userId()).isNotNull();
    }
}
