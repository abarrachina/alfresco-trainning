package com.users.migrateservice;


import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import javax.inject.Inject;

import org.alfresco.service.cmr.security.AuthorityService;
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
public class MigrateServiceGroupsTest {

    private static String newuser = "NewUser";
    private static String olduser = "OldUser";

    @Inject
    @InjectMocks
    private MigrateService migrateService;

    @Mock
    private AuthorityService authorityService;

    @Test
    public void testWiring() {
        assertNotNull(migrateService);
    }

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);

        final Set<String> groupsOldUser = new HashSet<String>();
        groupsOldUser.add("Group1");
        groupsOldUser.add("Group2");
        when(authorityService.getAuthoritiesForUser(olduser)).thenReturn(groupsOldUser);
        final Set<String> groupsNewUser = new HashSet<String>();
        groupsNewUser.add("Group3");
        groupsNewUser.add("Group4");
        when(authorityService.getAuthoritiesForUser(newuser)).thenReturn(groupsNewUser);

    }

    @Test
    public void testMigrateGroups() {
        migrateService.migrateGroups(olduser, newuser);
        verify(authorityService, times(1)).addAuthority("Group1", newuser);
        verify(authorityService, times(1)).addAuthority("Group2", newuser);
    }

}