package uk.gov.digital.ho.hocs.audit.export;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class HeaderConverterTest {

    private HeaderConverter converter;

    @BeforeEach
    public void before() {
        converter = new HeaderConverter();
    }

    @Test
    public void convertDataHandlesEmptyArray() {
        List<String> substitutedHeaders = converter.substitute(new LinkedList<>());
        assertThat(substitutedHeaders).isNotNull();
        assertThat(substitutedHeaders.size()).isEqualTo(0);
    }

    @Test
    public void convertDataHandlesNull() {
        assertThatThrownBy(() -> {
            converter.substitute(null);
        }).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void substituteExistingHeadersWithOrder() {
        List<String> headers = Stream.of("title1", "title2", "title3", "title4").collect(Collectors.toList());
        String[] expectedHeaders = new String[]{"New Title 1", "New Title 2", "New Title 3", "New Title 4"};
        List<String> substitutedHeaders = converter.substitute(headers);
        assertThat(substitutedHeaders).containsExactly(expectedHeaders);
    }

    @Test
    public void substituteNonExistingHeadersWithOrder() {
        List<String> headers = Stream.of("NonTitle1", "NonTitle2", "NonTitle3", "NonTitle4").collect(Collectors.toList());
        String[] expectedHeaders = new String[]{"NonTitle1", "NonTitle2", "NonTitle3", "NonTitle4"};
        List<String> substitutedHeaders = converter.substitute(headers);
        assertThat(substitutedHeaders).containsExactly(expectedHeaders);
    }

    @Test
    public void substituteExistingAndNonExistingMixedHeadersWithOrder() {
        List<String> headers = Stream.of("NonTitle1", "title1", "title2", "NonTitle2", "NonTitle3", "title3", "NonTitle4", "title4")
                                     .collect(Collectors.toList());
        String[] expectedHeaders = new String[]{"NonTitle1", "New Title 1", "New Title 2", "NonTitle2", "NonTitle3",
                                                "New Title 3", "NonTitle4", "New Title 4"};
        List<String> substitutedHeaders = converter.substitute(headers);
        assertThat(substitutedHeaders).containsExactly(expectedHeaders);
    }

}
