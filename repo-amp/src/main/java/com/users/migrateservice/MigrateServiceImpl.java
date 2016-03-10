package com.users.migrateservice;


import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.activiti.engine.TaskService;
import org.activiti.engine.task.Comment;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.repo.workflow.BPMEngine;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowNodeConverter;
import org.alfresco.repo.workflow.WorkflowServiceImpl;
import org.alfresco.repo.workflow.activiti.ActivitiNodeConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.google.gdata.data.Person;


@Service
public class MigrateServiceImpl implements MigrateService{

    private static Log logger = LogFactory.getLog(MigrateServiceImpl.class);

    @Inject
    private PersonService personService;

    @Inject
    private AuthorityService authorityService;

    @Inject
    private SiteService siteService;

    @Inject
    private NodeService nodeService;

    @Inject
    private SearchService searchService;

    @Inject
    private BehaviourFilter policyBehaviourFilter;
    
    @Inject
    private TaskService taskService;
    
    @Inject
    private ServiceRegistry serviceRegistry;         


    @Inject
    private PreferenceService preferenceService;

    @Override
    public void migrateSites(final String olduser, final String newuser) {
        String authority = "";
        final List<SiteInfo> sites = siteService.listSites(olduser);

        for (final SiteInfo site: sites){
            final String role = siteService.getMembersRole(site.getShortName(), olduser);
            try{
                authority = "GROUP_site_"+site.getShortName()+"_"+role;
                if (!authorityService.getAuthoritiesForUser(newuser).contains(authority)){
                    authorityService.addAuthority(authority, newuser);
                }
            }
            catch(final UnknownAuthorityException ex){
                logger.debug("The authority "+ authority + " not exists " + ex.getMessage());
            }
        }
    }

    @Override
    public void migrateGroups(final String olduser, final String newuser) {
        final Set<String> groups = authorityService.getAuthoritiesForUser(olduser);

        for (final String group:groups){
            if (!authorityService.getAuthoritiesForUser(newuser).contains(group)){
                authorityService.addAuthority(group, newuser);
            }
        }
    }

    @Override
    public void migrateContent(final String olduser, final String newuser) {
        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"";
        strQuery += " AND ";
        strQuery += "@cm\\:creator:\"" + olduser + "\"";
        changeCreator(strQuery, newuser);

    }

    @Override
    public void migrateFolder(final String olduser, final String newuser) {
        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\"";
        strQuery += " AND ";
        strQuery += "@cm\\:creator:\"" + olduser + "\"";
        changeCreator(strQuery, newuser);

    }

    @Override
    public void migrateComments(final String olduser, final String newuser) {
        String strQuery="TYPE:\"fm\\:post\"";
        strQuery += " AND ";
        strQuery += "@cm\\:creator:\"" + olduser + "\"";
        changeCreator(strQuery, newuser);

    }

    @Override
    public void migrateUserHome(final String olduser, final String newuser) {
        final NodeRef oldUserNodeRef = personService.getPerson(olduser);
        final NodeRef newUserNodeRef = personService.getPerson(newuser);
        final NodeRef homespaceOldUserNodeRef = (NodeRef) nodeService.getProperty(oldUserNodeRef, ContentModel.PROP_HOMEFOLDER);
        final NodeRef homespaceNewUserNodeRef = (NodeRef) nodeService.getProperty(newUserNodeRef, ContentModel.PROP_HOMEFOLDER);
        final List<ChildAssociationRef> childs = nodeService.getChildAssocs(homespaceOldUserNodeRef);
        for (final ChildAssociationRef child:childs){
            final NodeRef node = child.getChildRef();
            final String name = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
            final NodeRef existNode = nodeService.getChildByName(homespaceNewUserNodeRef, ContentModel.ASSOC_CONTAINS, name);

            if (existNode == null){
                final NodeRef newnode = nodeService.moveNode(node, homespaceNewUserNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN).getChildRef();
                changeCreatorModifier(newnode, newuser);
            }
            else{
                logger.debug("File or folder exists in the destination");
            }
        }
    }


    @Override
    public void migratePreferences (final String olduser, final String newuser){
        final Map<String, Serializable> preferences = preferenceService.getPreferences(olduser);
        preferenceService.setPreferences(newuser, preferences);
        preferenceService.clearPreferences(olduser);
    }

    @Override
    public void migrateLikes (final String olduser, final String newuser){
        String strQuery="TYPE:\"cm\\:rating\"";
        strQuery += " AND ";
        strQuery += "@cm\\:creator:\"" + olduser + "\"";
        changeCreator(strQuery, newuser);
    }


    
    
    @Override
    public void migrateWorkflows(String olduser, String newuser) {

    	this.changeTaskAsignee(olduser, newuser).changeWorkflowInitiator(olduser, newuser);
    } 
    
    private MigrateServiceImpl changeWorkflowInitiator(String olduser, String newuser){
    	//Getting noderefs for every person
    	final NodeRef oldUserNodeRef = personService.getPerson(olduser);
        final NodeRef newUserNodeRef = personService.getPerson(newuser);
       
        
        //Searching workflows
        WorkflowTaskQuery query = new WorkflowTaskQuery();
        WorkflowService workflowService = serviceRegistry.getWorkflowService();
        final List<WorkflowTask> listTasks = workflowService.queryTasks(query, true);
        for(WorkflowTask task: listTasks){ 
        	WorkflowNodeConverter workflowNodeConverter = new ActivitiNodeConverter(serviceRegistry);

        	final NodeRef currentInitiatorNodeRef = task.getPath().getInstance().getInitiator();
        	String currentInitiator = (String)nodeService.getProperty(currentInitiatorNodeRef, ContentModel.PROP_USERNAME);
			if (currentInitiator != null && currentInitiator.equalsIgnoreCase(olduser)) {

				//Setting users
				String taskId = BPMEngineRegistry.getLocalId(task.getId());				
				Map<String, Object> variables = taskService.getVariables(taskId);
		        variables.put(WorkflowConstants.PROP_INITIATOR, workflowNodeConverter.convertNode(newUserNodeRef));
				taskService.setVariables(taskId, variables);				
			}
        }
        return this;
        
	      
    }
    
    /***
     * Change asignee workflow tasks
     */
    private MigrateServiceImpl changeTaskAsignee(String olduser, String newuser){
    	
    	//Getting noderefs for every person
    	final NodeRef oldUserNodeRef = personService.getPerson(olduser);
        final NodeRef newUserNodeRef = personService.getPerson(newuser);
        
      //Searching workflows
        WorkflowTaskQuery query = new WorkflowTaskQuery();        
        WorkflowService workflowService = serviceRegistry.getWorkflowService();
        final List<WorkflowTask> listTasks = workflowService.queryTasks(query, true);
        for(WorkflowTask task: listTasks){ 
			String assignee = (String) task.getProperties().get(QName.createQName("{http://www.alfresco.org/model/content/1.0}owner"));
			String taskId = BPMEngineRegistry.getLocalId(task.getId());
			
			if (assignee != null && assignee.equalsIgnoreCase(olduser)) {
				taskService.setAssignee(taskId, newuser);
			}
        }
    	
    	return this;
    }
    
     

    /***
     * Change noderef's creator and modifier
     *
     * @param strQuery
     * @param newuser
     */
    private void changeCreator(final String strQuery, final String newuser){
        ResultSet results = null;
        try{
            results = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, strQuery);

            for (final ResultSetRow result:results){

                final NodeRef nodeRef = result.getNodeRef();
                changeCreatorModifier(nodeRef, newuser);
            }
        }
        finally{
            if(results != null)
            {
                results.close();
            }
        }
    }

    /***
     *
     * Change noderef's creator and modifier recursively
     *
     * @param node
     * @param newuser
     */
    private void changeCreatorModifier (final NodeRef node, final String newuser){

        if (nodeService.getType(node).equals(ContentModel.TYPE_FOLDER)){
            final List<ChildAssociationRef> childs = nodeService.getChildAssocs(node);
            for (final ChildAssociationRef child:childs){
                changeCreatorModifier(child.getChildRef(), newuser);
            }
        }

        // Disable auditable aspect to allow change properties of cm:auditable aspect
        policyBehaviourFilter.disableBehaviour(node, ContentModel.ASPECT_AUDITABLE);

        // Update properties of cm:auditable aspect
        nodeService.setProperty(node, ContentModel.PROP_CREATOR, newuser);
        nodeService.setProperty(node, ContentModel.PROP_MODIFIER, newuser);

        // Enable auditable aspect
        policyBehaviourFilter.enableBehaviour(node, ContentModel.ASPECT_AUDITABLE);

    }
   


}