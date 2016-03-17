package com.ixxus.ipm.migration.users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    public static final String KEY_ERROR_SITES = MigrateServiceFactory.SITES;
    public static final String KEY_ERROR_GROUPS = MigrateServiceFactory.GROUPS;
    public static final String KEY_ERROR_CONTENT = MigrateServiceFactory.CONTENT;
    public static final String KEY_ERROR_FOLDERS = MigrateServiceFactory.CONTENT;
    public static final String KEY_ERROR_COMMENTS = MigrateServiceFactory.COMMENTS;
    public static final String KEY_ERROR_USERHOME = MigrateServiceFactory.USERHOME;
    public static final String KEY_ERROR_WORKFLOW = MigrateServiceFactory.WORKFLOWS;

    private final Map<String, List<T>> notMigrated = new HashMap<>();


    @SuppressWarnings("unchecked")
    @Override
    public Map<String, List<T>> getNotMigrate(){

        return notMigrated;
    }

    @Override
    public void migrateSites(final String olduser, final String newuser, final Boolean toMigrate) {
        migrate(olduser, newuser, toMigrate, MigrateServiceFactory.SITES);

    }

    @Override
    public void migrateGroups(final String olduser, final String newuser, final Boolean toMigrate) {
        migrate(olduser, newuser, toMigrate, MigrateServiceFactory.GROUPS);
    }

    @Override
    public void migrateContent(final String olduser, final String newuser, final Boolean toMigrate) {
        migrate(olduser, newuser, toMigrate, MigrateServiceFactory.CONTENT);

    }

    @Override
    public void migrateFolder(final String olduser, final String newuser, final Boolean toMigrate) {
        migrate(olduser, newuser, toMigrate, MigrateServiceFactory.CONTENT);

    }

    @Override
    public void migrateComments(final String olduser, final String newuser, final Boolean toMigrate) {
        migrate(olduser, newuser, toMigrate, MigrateServiceFactory.COMMENTS);
    }

    @Override
    public void migrateUserHome(final String olduser, final String newuser, final Boolean toMigrate) {
        migrate(olduser, newuser, toMigrate, MigrateServiceFactory.USERHOME);
    }

    @Override
    public void migratePreferences (final String olduser, final String newuser, final Boolean toMigrate){
        migrate(olduser, newuser, toMigrate, MigrateServiceFactory.PREFERENCES);
    }

    @Override
    public void migrateWorkflows(final String olduser, final String newuser, final Boolean toMigrate) {

        migrate(olduser, newuser, toMigrate, MigrateServiceFactory.WORKFLOWS);
    }

    private void addNoMigrated(final List<T> list, final String type){

        notMigrated.put(type, list);
    }

    private void migrate(final String olduser,final String newuser, final Boolean toMigrate, final String type){
        if (toMigrate){
            final MigrateService migrateService = MigrateServiceFactory.createMigrateService(type);
            migrateService.migrate(olduser, newuser);
            addNoMigrated(migrateService.getNotMigrate(),type);
        }
    }

}