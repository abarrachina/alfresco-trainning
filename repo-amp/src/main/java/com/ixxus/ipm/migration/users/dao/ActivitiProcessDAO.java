package com.ixxus.ipm.migration.users.dao;

import com.ixxus.ipm.migration.users.ProcessStarterUser;

@FunctionalInterface
public interface ActivitiProcessDAO {

    public int executeUpdateAuthor (ProcessStarterUser input);
}
