package com.ixxus.ipm.migration.users;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
public class MigrateServiceFactoryImpl implements MigrateServiceFactory {

    @Autowired
    private ApplicationContext context;

    @Override
    public void setApplicationContext(final ApplicationContext appCtx) throws BeansException {
        context = appCtx;
    }

    @Override
    public MigrateService getInstance(final String beanName) {
        // TODO Auto-generated method stub
        return (MigrateService) context.getBean(beanName);
    }
}
