package com.ixxus.ipm.migration.users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.stereotype.Service;


/***
 *
 * @author nazareth.jimenez@ixxus.com
 * Migrate Service Impl
 * @param <T>
 *
 */
@Service
public class MigrateUserServiceImpl<T> implements MigrateUserService{


    //Static properties
    public static final String KEY_ERROR_SITES = "Sites";
    public static final String KEY_ERROR_GROUPS = "Groups";
    public static final String KEY_ERROR_CONTENT = "Content";
    public static final String KEY_ERROR_FOLDERS = "Folders";
    public static final String KEY_ERROR_COMMENTS = "Comments";
    public static final String KEY_ERROR_USERHOME = "UserHome";
    public static final String KEY_ERROR_PREFERENCES = "Preferences";
    public static final String KEY_ERROR_WORKFLOW = "Workflow";

    private final Map<String, List<T>> notMigrated = new HashMap<>();

    @Inject
    private MigrateServiceFactory factory;

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, List<T>> getNotMigrate(){

        return notMigrated;
    }

    @Override
    public void migrateSites(final String olduser, final String newuser, final Boolean toMigrate) {
        migrate(olduser, newuser, toMigrate, KEY_ERROR_SITES);

    }

    @Override
    public void migrateGroups(final String olduser, final String newuser, final Boolean toMigrate) {
        migrate(olduser, newuser, toMigrate, KEY_ERROR_GROUPS);
    }

    @Override
    public void migrateContent(final String olduser, final String newuser, final Boolean toMigrate) {
        migrate(olduser, newuser, toMigrate, KEY_ERROR_CONTENT);

    }

    @Override
    public void migrateFolder(final String olduser, final String newuser, final Boolean toMigrate) {
        migrate(olduser, newuser, toMigrate, KEY_ERROR_FOLDERS);

    }

    @Override
    public void migrateComments(final String olduser, final String newuser, final Boolean toMigrate) {
        migrate(olduser, newuser, toMigrate, KEY_ERROR_COMMENTS);
    }

    @Override
    public void migrateUserHome(final String olduser, final String newuser, final Boolean toMigrate) {
        migrate(olduser, newuser, toMigrate, KEY_ERROR_COMMENTS);
    }

    @Override
    public void migratePreferences (final String olduser, final String newuser, final Boolean toMigrate){
        migrate(olduser, newuser, toMigrate, KEY_ERROR_PREFERENCES);
    }

    @Override
    public void migrateWorkflows(final String olduser, final String newuser, final Boolean toMigrate) {

        migrate(olduser, newuser, toMigrate, KEY_ERROR_WORKFLOW);
    }

    private void addNoMigrated(final List<T> list, final String type){

        notMigrated.put(type, list);
    }

    private void migrate(final String olduser,final String newuser, final Boolean toMigrate, final String type){
        if (toMigrate){
            final MigrateService migrateService = factory.getInstance(type);
            migrateService.migrate(olduser, newuser);
            addNoMigrated(migrateService.getNotMigrate(),type);
        }
    }

}