package com.ixxus.ipm.migration.users;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class MigrateServiceSitesTest {

    @Inject
    @InjectMocks
    private MigrateServiceSites migrateServiceSites;

    @Mock
    private AuthorityService authorityService;

    @Mock
    private SiteService siteService;

    @Mock
    private SiteInfo site;

    @Mock
    private Log log;

    private final String newuser = "newuser";
    private final String olduser = "olduser";
    private final Set<String> siteNames = new HashSet<String>(); //Sites where newuser is an authority,
    private static final String SITE_NAME = "test";
    private static final String SITE_ROLE = "Sample_role";


    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);
        final List<SiteInfo> sites = listSites();
        when(authorityService.getAuthoritiesForUser(newuser)).thenReturn(siteNames);
        when(siteService.getMembersRole(any(String.class), eq(olduser))).thenReturn(SITE_ROLE);
        when(siteService.listSites(eq(olduser))).thenReturn(sites);
    }

    @Test
    public void migrateToAnExistingUserTest() {

        addSiteToNewUser();
        migrateServiceSites.migrate(olduser, newuser);
        verify(authorityService, never()).addAuthority(getDummyAuthority(), newuser);
        siteNames.clear();
    }
    @Test
    public void migrateToAnUnexistingUserTest() {

        migrateServiceSites.migrate(olduser, newuser);
        verify(authorityService, times(1)).addAuthority(getDummyAuthority(), newuser);
    }

    @Test
    public void unknownAuthorityExceptionTest(){
        doThrow(new UnknownAuthorityException("")).when(authorityService).addAuthority(getDummyAuthority(), newuser);
        migrateServiceSites.migrate(olduser, newuser);
        verify(site,times(1)).getNodeRef();
    }

    /***
     * Create a list with just one site
     * @return
     */
    private List<SiteInfo> listSites(){

        final List<SiteInfo> sites = new ArrayList<>();
        when(site.getShortName()).thenReturn(SITE_NAME);
        sites.add(site);
        return sites;
    }

    /***
     * Adding a site to siteNames list
     */
    private void addSiteToNewUser(){

        siteNames.add(getDummyAuthority());
    }

    private String getDummyAuthority(){

        return "GROUP_site_"+SITE_NAME+"_"+SITE_ROLE;
    }

}
