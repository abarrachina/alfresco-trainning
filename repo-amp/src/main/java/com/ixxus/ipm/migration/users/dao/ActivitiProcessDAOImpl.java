package com.ixxus.ipm.migration.users.dao;

import org.mybatis.spring.SqlSessionTemplate;

import com.ixxus.ipm.migration.users.ProcessStarterUser;

public class ActivitiProcessDAOImpl implements ActivitiProcessDAO {

	protected SqlSessionTemplate template;
	
	public ActivitiProcessDAOImpl() {
		
	}
	
	public ActivitiProcessDAOImpl(SqlSessionTemplate template) {
		this.template = template;
	}
	
	public final void setTemplate(SqlSessionTemplate sqlSessionTemplate){
		this.template = sqlSessionTemplate;
	}
	
	public int executeUpdateAuthor (ProcessStarterUser input){
		return this.template.update("workflow-initiator.update_starter_user", input);
	}
}
 