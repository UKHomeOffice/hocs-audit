package uk.gov.digital.ho.hocs.audit.auditdetails.dto;

import org.junit.Test;
import uk.gov.digital.ho.hocs.audit.export.caseworkclient.dto.GetCorrespondentOutlineResponse;
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
        GetCorrespondentOutlineResponse correspondentOne = new GetCorrespondentOutlineResponse(null, "fullName", false);
        GetCorrespondentOutlineResponse correspondentTwo = new GetCorrespondentOutlineResponse(null, "fullName", false);
        Set<GetCorrespondentOutlineResponse> correspondents = new HashSet<>(Arrays.asList(correspondentOne, correspondentTwo));
        GetCorrespondentsResponse response = new GetCorrespondentsResponse(correspondents);
        String name = response.getPrimaryCorrespondentName();
        assert(name == null);
    }

    @Test
    public void testSinglePrimary(){
        GetCorrespondentOutlineResponse correspondentOne = new GetCorrespondentOutlineResponse(null, "fullName", true);
        Set<GetCorrespondentOutlineResponse> correspondents = new HashSet<>(Arrays.asList(correspondentOne));
        GetCorrespondentsResponse response = new GetCorrespondentsResponse(correspondents);
        String name = response.getPrimaryCorrespondentName();
        assert(name.equals("fullName"));
    }

    @Test
    public void testFindPrimaryAmongOthers(){
        GetCorrespondentOutlineResponse correspondentOne = new GetCorrespondentOutlineResponse(null, "fullName", false);
        GetCorrespondentOutlineResponse correspondentTwo = new GetCorrespondentOutlineResponse(null, "primaryPerson", true);
        GetCorrespondentOutlineResponse correspondentThree = new GetCorrespondentOutlineResponse(null, "fullName", false);
        Set<GetCorrespondentOutlineResponse> correspondents = new HashSet<>(Arrays.asList(correspondentOne, correspondentTwo, correspondentThree));
        GetCorrespondentsResponse response = new GetCorrespondentsResponse(correspondents);
        String name = response.getPrimaryCorrespondentName();
        assert(name.equals("primaryPerson"));
    }

}
