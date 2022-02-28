package uk.gov.digital.ho.hocs.audit.export.infoclient;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.EntityDataDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.EntityDto;
import uk.gov.digital.ho.hocs.audit.export.infoclient.dto.UserDto;

import java.util.Set;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@RunWith(MockitoJUnitRunner.class)
public class InfoClientSupplierTest {

    @Mock
    private InfoClient infoClient;

    private final static UUID USER_UUID = UUID.randomUUID();

    private InfoClientSupplier infoClientSupplier;

    @Before
    public void before() {
        Mockito.when(infoClient.getEntitiesForList(any()))
                .thenReturn(
                        Set.of(new EntityDto(1L,
                                UUID.randomUUID(),
                                "TEST",
                                new EntityDataDto("TEST_TITLE"),
                                UUID.randomUUID(),
                                true)));

        Mockito.when(infoClient.getUsers())
                .thenReturn(Set.of(new UserDto(USER_UUID.toString(), "TEST", "TEST_FIRST", "TEST_LAST", "TEST_EMAIL")));

        infoClientSupplier = new InfoClientSupplier(infoClient);
    }

    @Test
    public void shouldReturnEntitiesAsSupplier() {
        var result = infoClientSupplier.getEntityList("TEST");
        var resultMap = result.get();

        Assert.assertEquals(1, resultMap.size());
        Assert.assertTrue(resultMap.containsKey("TEST"));
    }

    @Test
    public void shouldReturnUsers() {
        var result = infoClientSupplier.getUsers();
        var resultMap = result.get();

        Assert.assertEquals(1, resultMap.size());
        Assert.assertTrue(resultMap.containsKey(USER_UUID.toString()));
    }


}
