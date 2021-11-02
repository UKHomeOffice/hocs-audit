package uk.gov.digital.ho.hocs.audit.auditdetails.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCorrespondentWithPrimaryFlagResponse;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCorrespondentsResponse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public class GetCorrespondentsResponseTest {

    @Test
    public void testHandleNoCorrespondents(){
        GetCorrespondentsResponse response = new GetCorrespondentsResponse(null);
        String name = response.getPrimaryCorrespondentName();
        assert(name == null);
    }

    @Test
    public void testCorrespondentsButNoPrimary(){
        GetCorrespondentWithPrimaryFlagResponse correspondentOne = new GetCorrespondentWithPrimaryFlagResponse("fullName", false);
        GetCorrespondentWithPrimaryFlagResponse correspondentTwo = new GetCorrespondentWithPrimaryFlagResponse("fullName", false);
        Set<GetCorrespondentWithPrimaryFlagResponse> correspondents = new HashSet<>(Arrays.asList(correspondentOne, correspondentTwo));
        GetCorrespondentsResponse response = new GetCorrespondentsResponse(correspondents);
        String name = response.getPrimaryCorrespondentName();
        assert(name == null);
    }

    @Test
    public void testSinglePrimary(){
        GetCorrespondentWithPrimaryFlagResponse correspondentOne = new GetCorrespondentWithPrimaryFlagResponse("fullName", true);
        Set<GetCorrespondentWithPrimaryFlagResponse> correspondents = new HashSet<>(Arrays.asList(correspondentOne));
        GetCorrespondentsResponse response = new GetCorrespondentsResponse(correspondents);
        String name = response.getPrimaryCorrespondentName();
        assert(name.equals("fullName"));
    }

    @Test
    public void testFindPrimaryAmongOthers(){
        GetCorrespondentWithPrimaryFlagResponse correspondentOne = new GetCorrespondentWithPrimaryFlagResponse("fullName", false);
        GetCorrespondentWithPrimaryFlagResponse correspondentTwo = new GetCorrespondentWithPrimaryFlagResponse("primaryPerson", true);
        GetCorrespondentWithPrimaryFlagResponse correspondentThree = new GetCorrespondentWithPrimaryFlagResponse("fullName", false);
        Set<GetCorrespondentWithPrimaryFlagResponse> correspondents = new HashSet<>(Arrays.asList(correspondentOne, correspondentTwo, correspondentThree));
        GetCorrespondentsResponse response = new GetCorrespondentsResponse(correspondents);
        String name = response.getPrimaryCorrespondentName();
        assert(name.equals("primaryPerson"));
    }

}
