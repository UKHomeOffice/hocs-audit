package uk.gov.digital.ho.hocs.audit;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.application.RequestData;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RequestDataTest
{
    @Mock
    private HttpServletRequest mockHttpServletRequest;
    @Mock private HttpServletResponse mockHttpServletResponse;
    @Mock private Object mockHandler;

    private RequestData requestData;

    @Before
    public void setup() {
        requestData = new RequestData();
    }

    @Test
    public void shouldDefaultRequestData() {
        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.correlationId()).isNotNull();
        assertThat(requestData.userId()).isEqualTo("anonymous");
        assertThat(requestData.username()).isEqualTo("anonymous");
    }

    @Test
    public void shouldUseCorrelationIdFromRequest() {
        when(mockHttpServletRequest.getHeader("X-Correlation-Id")).thenReturn("some correlation id");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.correlationId()).isEqualTo("some correlation id");
    }

    @Test
    public void shouldUseUserIdFromRequest() {
        when(mockHttpServletRequest.getHeader("X-Auth-Userid")).thenReturn("some user id");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.userId()).isEqualTo("some user id");
    }

    @Test
    public void shouldUseUsernameFromRequest() {
        when(mockHttpServletRequest.getHeader("X-Auth-Username")).thenReturn("some username");

        requestData.preHandle(mockHttpServletRequest, mockHttpServletResponse, mockHandler);

        assertThat(requestData.username()).isEqualTo("some username");
    }
}
