package com.ixxus.ipm.migration.users;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.activiti.engine.TaskService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowNodeConverter;
import org.alfresco.repo.workflow.activiti.ActivitiNodeConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.OwnableService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.ixxus.ipm.migration.users.dao.ActivitiProcessDAO;


/***
 *
 * @author nazareth.jimenez@ixxus.com
 * Migrate Service Impl
 * @param <T>
 *
 */
@Service
public class MigrateUserServiceImpl<T> implements MigrateUserService{


    //Static properties
    public static final String KEY_ERROR_SITES = "Sites";
    public static final String KEY_ERROR_GROUPS = "Groups";
    public static final String KEY_ERROR_CONTENT = "Content";
    public static final String KEY_ERROR_FOLDERS = "Folders";
    public static final String KEY_ERROR_COMMENTS = "Comments";
    public static final String KEY_ERROR_USERHOME = "UserHome";
    public static final String KEY_ERROR_WORKFLOW = "Workflow";
	
	private Map<String, ArrayList<T>> notMigrated = new HashMap<>(); 

    
    @Override
    public Map<String, ArrayList<T>> getNotMigrate(){
    	
    	return notMigrated;
    }
    
    private void addNoMigrated(ArrayList<T> list, String type){
    	
    	notMigrated.put(type, list);    	
    }
    
    private void migrate(String olduser,String newuser, Boolean toMigrate, String type){
    	if (toMigrate){
    		MigrateService migrateService = MigrateServiceFactory.createMigrateService(type);
        	migrateService.migrate(olduser, newuser);
    	}    	
    }

    @Override
    public void migrateSites(final String olduser, final String newuser, Boolean toMigrate) {
    	migrate(olduser, newuser, toMigrate, MigrateServiceFactory.SITES);
    		
    }

    @Override
    public void migrateGroups(final String olduser, final String newuser, Boolean toMigrate) {
    	migrate(olduser, newuser, toMigrate, MigrateServiceFactory.GROUPS);
    }

    @Override
    public void migrateContent(final String olduser, final String newuser, Boolean toMigrate) {
    	migrate(olduser, newuser, toMigrate, MigrateServiceFactory.CONTENT);

    }

    @Override
    public void migrateFolder(final String olduser, final String newuser, Boolean toMigrate) {
        migrate(olduser, newuser, toMigrate, MigrateServiceFactory.CONTENT);

    }

    @Override
    public void migrateComments(final String olduser, final String newuser, Boolean toMigrate) {
    	migrate(olduser, newuser, toMigrate, MigrateServiceFactory.COMMENTS);
    }

    @Override
    public void migrateUserHome(final String olduser, final String newuser, Boolean toMigrate) {
    	migrate(olduser, newuser, toMigrate, MigrateServiceFactory.USERHOME);
    }

    @Override
    public void migratePreferences (final String olduser, final String newuser, Boolean toMigrate){
    	migrate(olduser, newuser, toMigrate, MigrateServiceFactory.PREFERENCES);
    }

    @Override
    public void migrateWorkflows(final String olduser, final String newuser, Boolean toMigrate) {

    	migrate(olduser, newuser, toMigrate, MigrateServiceFactory.WORKFLOWS);
    }
}