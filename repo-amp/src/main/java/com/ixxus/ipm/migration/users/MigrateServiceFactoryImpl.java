package com.ixxus.ipm.migration.users;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class MigrateServiceFactoryImpl implements MigrateServiceFactory {

	@Autowired
    private ApplicationContext context;

    @Override
    public void setApplicationContext(ApplicationContext appCtx) throws BeansException {
        context = appCtx;
    }
	
	private MigrateServiceFactoryImpl(){}
	
	@Override
	public MigrateService getInstance(String beanName) {
		// TODO Auto-generated method stub
		return (MigrateService) context.getBean(beanName);
	}
}
