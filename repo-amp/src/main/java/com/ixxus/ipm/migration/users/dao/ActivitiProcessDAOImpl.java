package com.users.migrateservice.dao;

import org.mybatis.spring.SqlSessionTemplate;

import com.users.migrateservice.ProcessStarterUser;

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
		return this.template.update("my_extension.update_author", input);
	}
}
 