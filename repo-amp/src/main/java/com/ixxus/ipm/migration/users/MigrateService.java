package com.ixxus.ipm.migration.users;

import java.util.ArrayList;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

/***
 *
 * @author nazareth.jimenez@ixxus.com
 *
 */
public interface MigrateService{

    /***
     * Migrate Site Roles from olduser to newuser
     *
     * @param olduser
     * @param newuser
     */
    public void migrate (final String olduser, final String newuser, final Boolean toMigrate);


    /***
     *
     * @return Content that the process can't migrate
     */
    public Map<String, ArrayList<NodeRef>> getNotMigrate();
    public Map<String, ArrayList<String>> getTaskNoMigrated();
}
