package com.ixxus.ipm.migration.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.springframework.stereotype.Service;

/***
 *
 * @author nazareth.jimenez@ixxus.com
 * Migrate Service Preferences
 *
 */
@Service
public class MigrateServicePreferences implements MigrateService{

    @Inject
    private PreferenceService preferenceService;


    final Map<String, ArrayList<NodeRef>> notMigrate = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, ArrayList<NodeRef>> getNotMigrate() {
        return notMigrate;
    }

    @Override
    public void migrate(final String olduser, final String newuser) {
        migratePreferences(olduser, newuser);
    }

    private void migratePreferences (final String olduser, final String newuser){
        final Map<String, Serializable> preferences = preferenceService.getPreferences(olduser);
        preferenceService.setPreferences(newuser, preferences);
        preferenceService.clearPreferences(olduser);
    }
}