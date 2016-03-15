package com.users.migrateservice;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.activiti.engine.TaskService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.domain.activities.ActivitiesDAO;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.repo.template.Workflow;
import org.alfresco.repo.workflow.BPMEngine;
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
import org.alfresco.service.cmr.workflow.WorkflowDefinition;
import org.alfresco.service.cmr.workflow.WorkflowInstance;
import org.alfresco.service.cmr.workflow.WorkflowInstanceQuery;
import org.alfresco.service.cmr.workflow.WorkflowNode;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import com.users.migrateservice.dao.ActivitiProcessDAO;


/***
 *
 * @author nazareth.jimenez@ixxus.com
 * Migrate Service Impl
 *
 */
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
    private OwnableService ownableService;
    
    @Inject
    private ActivitiProcessDAO activitiProcessDAO ;


    @Inject
    private PreferenceService preferenceService;

    abstract class EngineService{
        public abstract String getLocalId(final String id);
    }

    EngineService engineService = new EngineService(){

        @Override
        public String getLocalId (final String id){
            return BPMEngineRegistry.getLocalId(id);
        }

    };

    final Map<String, ArrayList<NodeRef>> notMigrate = new HashMap<String, ArrayList<NodeRef>>();

    @Override
    public Map<String, ArrayList<NodeRef>> getNotMigrate() {
        return notMigrate;
    }

    @Override
    public void migrateSites(final String olduser, final String newuser) {
        String authority = "";
        final List<SiteInfo> sites = siteService.listSites(olduser);
        final List<NodeRef> sitesNotMigrate =  new ArrayList<NodeRef>();

        for (final SiteInfo site: sites){
            try{
                final String role = siteService.getMembersRole(site.getShortName(), olduser);
                authority = "GROUP_site_"+site.getShortName()+"_"+role;
                if (!authorityService.getAuthoritiesForUser(newuser).contains(authority)){
                    authorityService.addAuthority(authority, newuser);
                }
            }
            catch(final UnknownAuthorityException ex){
                sitesNotMigrate.add(site.getNodeRef());
                logger.debug("The authority "+ authority + " not exists " + ex.getMessage());
            }
        }
        notMigrate.put("Sites", (ArrayList<NodeRef>) sitesNotMigrate);
    }

    @Override
    public void migrateGroups(final String olduser, final String newuser) {
        final Set<String> groups = authorityService.getAuthoritiesForUser(olduser);
        final List<NodeRef> groupsNotMigrate  = new ArrayList<NodeRef>();

        for (final String group:groups){
            try{
                if (!authorityService.getAuthoritiesForUser(newuser).contains(group)){
                    authorityService.addAuthority(group, newuser);
                }
            }
            catch(final UnknownAuthorityException ex){
                groupsNotMigrate.add(authorityService.getAuthorityNodeRef(group));
                logger.debug("The authority "+ group + " not exists " + ex.getMessage());
            }
        }
        notMigrate.put("Groups", (ArrayList<NodeRef>) groupsNotMigrate);
    }

    @Override
    public void migrateContent(final String olduser, final String newuser) {
        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"";
        strQuery += " AND ";
        strQuery += "@cm\\:creator:\"" + olduser + "\"";
        changeCreator(strQuery, newuser, "Content");

    }

    @Override
    public void migrateFolder(final String olduser, final String newuser) {
        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\"";
        strQuery += " AND ";
        strQuery += "@cm\\:creator:\"" + olduser + "\"";
        changeCreator(strQuery, newuser, "Folders");

    }

    @Override
    public void migrateComments(final String olduser, final String newuser) {
        String strQuery="TYPE:\"fm\\:post\"";
        strQuery += " AND ";
        strQuery += "@cm\\:creator:\"" + olduser + "\"";
        changeCreator(strQuery, newuser, "Comments");

    }

    @Override
    public void migrateUserHome(final String olduser, final String newuser) {
        final NodeRef oldUserNodeRef = personService.getPerson(olduser);
        final NodeRef newUserNodeRef = personService.getPerson(newuser);
        final NodeRef homespaceOldUserNodeRef = (NodeRef) nodeService.getProperty(oldUserNodeRef, ContentModel.PROP_HOMEFOLDER);
        final NodeRef homespaceNewUserNodeRef = (NodeRef) nodeService.getProperty(newUserNodeRef, ContentModel.PROP_HOMEFOLDER);
        final List<ChildAssociationRef> childs = nodeService.getChildAssocs(homespaceOldUserNodeRef);
        final List<NodeRef> userHomeNotMigrate = new ArrayList<NodeRef>();

        for (final ChildAssociationRef child:childs){
            final NodeRef node = child.getChildRef();
            final String name = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
            final NodeRef existNode = nodeService.getChildByName(homespaceNewUserNodeRef, ContentModel.ASSOC_CONTAINS, name);

            if (existNode == null){
                try{
                    final NodeRef newnode = nodeService.moveNode(node, homespaceNewUserNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN).getChildRef();
                    changeCreatorModifier(newnode, newuser, "UserHome");
                }
                catch (final NodeLockedException e)
                {
                    userHomeNotMigrate.add(node);
                    notMigrate.put("UserHome", (ArrayList<NodeRef>) userHomeNotMigrate);
                    logger.debug("The node " + node.toString() + " has locked");
                }

            }
            else{
                userHomeNotMigrate.add(node);
                notMigrate.put("UserHome", (ArrayList<NodeRef>) userHomeNotMigrate);
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
        changeCreator(strQuery, newuser, "Likes");
    }
    
    @Override
    public void migrateWorkflows(String olduser, String newuser) {
    	
    	this.changeTaskAsignee(olduser, newuser).changeWorkflowInitiator(olduser, newuser);
    }; 
    

    /***
	 * Get Workflows started by an user
	 * @param oldUserNodeRef Workflow initiator
	 * @return
	 */
	private List<WorkflowInstance> getWorkflowsByInitiator(NodeRef oldUserNodeRef) {

		// Searching workflows
		final WorkflowInstanceQuery query = new WorkflowInstanceQuery();
		Map<QName, Object> filters = new HashMap<QName, Object>();
		filters.put(QName.createQName(NamespaceService.DEFAULT_URI, "initiator"), oldUserNodeRef);
		query.setCustomProps(filters);
		final WorkflowService workflowService = serviceRegistry.getWorkflowService();
		return workflowService.getWorkflows(query);

	}

	/***
	 * Get Tasks for a workflow 
	 * @param workflow Workflow instance
	 * @return
	 */
	private List<WorkflowTask> getWorkflowTask(WorkflowInstance workflow) {

		WorkflowTaskQuery query = new WorkflowTaskQuery();
		query.setActive(null);
		query.setWorkflowDefinitionName(workflow.getDefinition().getName());
		final WorkflowService workflowService = serviceRegistry.getWorkflowService();
		return workflowService.queryTasks(query, true);
	}

	/***
	 * Migrate a task from olduser to newuser
	 * @param task
	 * @param olduser
	 * @param newuser
	 */
	private void migrateUsersForWorkflow(WorkflowTask task, String olduser, String newuser) {

		final NodeRef newUserNodeRef = personService.getPerson(newuser);
		final NodeRef homespaceNodeRef = (NodeRef) nodeService.getProperty(newUserNodeRef,
				ContentModel.PROP_HOMEFOLDER); // Might be null

		final WorkflowNodeConverter workflowNodeConverter = new ActivitiNodeConverter(serviceRegistry);

		final NodeRef currentInitiatorNodeRef = task.getPath().getInstance().getInitiator();
		if (nodeService.exists(currentInitiatorNodeRef)) {
			final String currentInitiator = (String) nodeService.getProperty(currentInitiatorNodeRef,
					ContentModel.PROP_USERNAME);
			if ((currentInitiator != null) && currentInitiator.equalsIgnoreCase(olduser)) {

				// Setting users
				final String taskId = engineService.getLocalId(task.getId());
				final Map<String, Object> variables = taskService.getVariables(taskId);
				variables.put(WorkflowConstants.PROP_INITIATOR, workflowNodeConverter.convertNode(newUserNodeRef));
				variables.put(WorkflowConstants.PROP_INITIATOR_HOME,
						workflowNodeConverter.convertNode(homespaceNodeRef));
				taskService.setVariables(taskId, variables);

			}
		}
	}

	private MigrateServiceImpl changeWorkflowInitiator(final String olduser, final String newuser) {

		// Getting noderefs for every person
		final NodeRef oldUserNodeRef = personService.getPerson(olduser);
		
		final List<WorkflowInstance> listWorkflows = getWorkflowsByInitiator(oldUserNodeRef);		
		for (final WorkflowInstance workflow : listWorkflows) {
			List<WorkflowTask> tasks = this.getWorkflowTask(workflow);
			for (final WorkflowTask task : tasks){
				this.migrateUsersForWorkflow(task, olduser, newuser);
			}
		}
		if (true){
			//Require method to migrate the task that "I've started"
			// This value is not getted from the Initiator or Initiator_Home variable, so this step is required
			this.forceMigrateInitiator(olduser, newuser);
		}
		return this;
	}
	
	/***
	 * Updated starteruser from DB 
	 * @param olduser
	 * @param newuser
	 */
	private void forceMigrateInitiator(String olduser, String newuser){
	
		ProcessStarterUser processStarterUser = new ProcessStarterUser(olduser, newuser);
		this.activitiProcessDAO.executeUpdateAuthor(processStarterUser);
	}

	/***
	 * Change asignee workflow tasks
	 */
	private MigrateServiceImpl changeTaskAsignee(final String olduser, final String newuser) {

		// Getting noderefs for every person
		final NodeRef oldUserNodeRef = personService.getPerson(olduser);
		final NodeRef newUserNodeRef = personService.getPerson(newuser);

		// Searching workflows
		final WorkflowTaskQuery query = new WorkflowTaskQuery();
		final WorkflowService workflowService = serviceRegistry.getWorkflowService();
		final List<WorkflowTask> listTasks = workflowService.queryTasks(query, true);
		for (final WorkflowTask task : listTasks) {
			final String assignee = (String) task.getProperties()
					.get(QName.createQName("{http://www.alfresco.org/model/content/1.0}owner"));
			final String taskId = engineService.getLocalId(task.getId());

			if ((assignee != null) && assignee.equalsIgnoreCase(olduser)) {
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
    private void changeCreator(final String strQuery, final String newuser, final String typeContent){
        List<NodeRef> nodeRefs = new ArrayList<>();
        ResultSet results = null;
        try{
            results = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, strQuery);
            nodeRefs = results.getNodeRefs();
            for (final NodeRef nodeRef:nodeRefs){
                changeCreatorModifier(nodeRef, newuser, typeContent);
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
    private void changeCreatorModifier (final NodeRef node, final String newuser, final String typeContent){

        List<NodeRef> contentNotMigrate = null;
        if (typeContent == "UserHome"){
            contentNotMigrate = notMigrate.get(typeContent);
        }

        if (contentNotMigrate == null){
            contentNotMigrate = new ArrayList<NodeRef>();
        }

        if (nodeService.getType(node).equals(ContentModel.TYPE_FOLDER)){
            final List<ChildAssociationRef> childs = nodeService.getChildAssocs(node);
            for (final ChildAssociationRef child:childs){
                changeCreatorModifier(child.getChildRef(), newuser, typeContent);
            }
        }

        // Disable auditable aspect to allow change properties of cm:auditable aspect
        policyBehaviourFilter.disableBehaviour(node, ContentModel.ASPECT_AUDITABLE);

        try
        {
            // Update properties of cm:auditable aspect
            nodeService.setProperty(node, ContentModel.PROP_CREATOR, newuser);
            ownableService.setOwner(node, newuser);
            nodeService.setProperty(node, ContentModel.PROP_MODIFIER, newuser);
        }
        catch(final InvalidNodeRefException ex){
            contentNotMigrate.add(node);
            logger.debug("The noderef "+ node.toString() + " can't migrate " + ex.getMessage());
        }
        finally
        {
            // Enable auditable aspect
            policyBehaviourFilter.enableBehaviour(node, ContentModel.ASPECT_AUDITABLE);
            notMigrate.put(typeContent, (ArrayList<NodeRef>) contentNotMigrate);
        }
    }

}