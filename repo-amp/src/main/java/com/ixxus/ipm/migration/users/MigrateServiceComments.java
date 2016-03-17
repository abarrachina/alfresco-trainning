package com.ixxus.ipm.migration.users;

import org.springframework.stereotype.Service;

/***
 *
 * @author nazareth.jimenez@ixxus.com
 * Migrate Service Content
 *
 */
@Service
public class MigrateServiceComments extends AbstractMigrateService {

    //Static properties
    public static final String KEY_ERROR_COMMENTS = "Comments";

    private static final String AND_QUERY = " AND ";
    private static final String CREATOR_QUERY = "@cm\\:creator:\"";

    @Override
    public void migrate(final String olduser, final String newuser) {
        migrateComments(olduser, newuser);

    }

    private void migrateComments(final String olduser, final String newuser) {
        String strQuery="TYPE:\"fm\\:post\"";
        strQuery += AND_QUERY;
        strQuery += CREATOR_QUERY + olduser + "\"";
        changeCreator(strQuery, newuser, KEY_ERROR_COMMENTS);
    }
}