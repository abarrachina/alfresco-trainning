package com.ixxus.ipm.migration.users;

import java.util.List;
import java.util.Map;





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
    public void migrate (final String olduser, final String newuser);


    /***
     *
     * @return Content that the process can't migrate
     */
    public <T> List<T> getNotMigrate();

}
