package com.ixxus.ipm.migration.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.workflow.WorkflowTask;

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
    public void migrateSites (final String olduser, final String newuser);

    /***
     * Migrate Groups from olduser to newuser
     *
     * @param olduser
     * @param newuser
     */
    public void migrateGroups (final String olduser, final String newuser);

    /***
     * Migrate Content from olduser to newuser
     *
     * @param olduser
     * @param newuser
     */
    public void migrateContent (final String olduser, final String newuser);

    /***
     * Migrate Folder from olduser to newuser
     *
     * @param olduser
     * @param newuser
     */
    public void migrateFolder (final String olduser, final String newuser);

    /***
     * Migrate comments from olduser to newuser
     *
     * @param olduser
     * @param newuser
     */
    public void migrateComments (final String olduser, final String newuser);

    /***
     * Migrate user home from olduser to newuser
     *
     * @param olduser
     * @param newuser
     */
    public void migrateUserHome (final String olduser, final String newuser);

    /***
     * Migrate preferences from olduser to newuser
     *
     * @param olduser
     * @param newuser
     */
    public void migratePreferences (final String olduser, final String newuser);

    /***
     * Migrate workflows from olduser to newuser
     */
    public void migrateWorkflows (final String olduser, final String newuser);

    /***
     *
     * @return Content that the process can't migrate
     */
    public Map<String, ArrayList<NodeRef>> getNotMigrate();
    
    public Map<String, ArrayList<String>> getTaskNoMigrated();
}
