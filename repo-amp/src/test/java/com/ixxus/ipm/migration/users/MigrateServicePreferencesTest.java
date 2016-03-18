package com.ixxus.ipm.migration.users;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.HashMap;

import javax.inject.Inject;

import org.alfresco.service.cmr.preference.PreferenceService;
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

@RunWith(RemoteTestRunner.class)@Remote(runnerClass = SpringJUnit4ClassRunner.class)
//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class MigrateServicePreferencesTest {


    private static String newuser = "NewUser";
    private static String olduser = "OldUser";

    @Inject
    @InjectMocks
    private MigrateServicePreferences migrateServicePreferences;

    @Mock
    private PreferenceService preferenceService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testMigratePreferences() {

        final HashMap<String, Serializable> preferences = new HashMap<String, Serializable>();
        preferences.put("favorites", "workspace://SpacesStore/contentnode1");
        when(preferenceService.getPreferences(olduser)).thenReturn(preferences);
        migrateServicePreferences.migrate(olduser, newuser);
        verify(preferenceService, times(1)).getPreferences(olduser);
        verify(preferenceService, times(1)).setPreferences(newuser, preferences);
        verify(preferenceService, times(1)).clearPreferences(olduser);
    }
}
