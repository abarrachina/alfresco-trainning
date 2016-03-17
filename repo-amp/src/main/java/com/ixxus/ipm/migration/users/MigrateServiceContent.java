package com.ixxus.ipm.migration.users;

import org.springframework.stereotype.Service;

/***
 *
 * @author nazareth.jimenez@ixxus.com
 * Migrate Service Content
 *
 */
@Service
public class MigrateServiceContent extends AbstractMigrateService{

    //Static properties
    public static final String KEY_ERROR_CONTENT = "Content";
    public static final String KEY_ERROR_FOLDERS = "Folders";

    private static final String AND_QUERY = " AND ";
    private static final String CREATOR_QUERY = "@cm\\:creator:\"";

    @Override
    public void migrate(final String olduser, final String newuser) {
        migrateContent(olduser, newuser);
        migrateFolder(olduser, newuser);
    }

    private void migrateContent(final String olduser, final String newuser) {
        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"";
        strQuery += AND_QUERY;
        strQuery += CREATOR_QUERY + olduser + "\"";
        changeCreator(strQuery, newuser, KEY_ERROR_CONTENT);

    }

    private void migrateFolder(final String olduser, final String newuser) {
        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\"";
        strQuery += AND_QUERY;
        strQuery += CREATOR_QUERY + olduser + "\"";
        changeCreator(strQuery, newuser, KEY_ERROR_FOLDERS);

    }
}