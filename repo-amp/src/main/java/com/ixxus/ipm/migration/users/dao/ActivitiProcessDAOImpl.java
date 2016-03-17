package com.ixxus.ipm.migration.users.dao;

import org.mybatis.spring.SqlSessionTemplate;

import com.ixxus.ipm.migration.users.ProcessStarterUser;

public class ActivitiProcessDAOImpl implements ActivitiProcessDAO {

    protected SqlSessionTemplate template;

    public ActivitiProcessDAOImpl() {
        // Empty constructor
    }

    public ActivitiProcessDAOImpl(final SqlSessionTemplate template) {
        this.template = template;
    }

    public final void setTemplate(final SqlSessionTemplate sqlSessionTemplate){
        this.template = sqlSessionTemplate;
    }

    @Override
    public int executeUpdateAuthor (final ProcessStarterUser input){
        return this.template.update("workflow-initiator.update_starter_user", input);
    }
}
