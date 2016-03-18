package com.ixxus.ipm.migration.users.utils;

import java.util.List;

/***
 *
 * @author nazareth.jimenez@ixxus.com
 * Utils Migrate User
 *
 */

public class MigrateActionUtils{

    private MigrateActionUtils() {
        //Empty Constructor
    }

    public static <T> Boolean isNullOrEmpty(final List<T> entry){
        if ((entry != null) && (!entry.isEmpty())){
            return false;
        }
        return true;
    }
}
