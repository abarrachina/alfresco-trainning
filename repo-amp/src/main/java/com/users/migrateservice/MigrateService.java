package com.users.migrateservice;

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
     * Migrate likes from olduser to newuser
     *
     * @param olduser
     * @param newuser
     */
    public void migrateLikes (final String olduser, final String newuser);
    
    /***
     * Migrate workflows from olduser to newuser
     */
    public void migrateWorkflows (final String olduser, final String newuser);
}
