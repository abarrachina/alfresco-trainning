package com.ixxus.ipm.migration.users;

import org.springframework.context.ApplicationContextAware;

public interface MigrateServiceFactory extends ApplicationContextAware {

	public MigrateService getInstance(String beanName);
}
