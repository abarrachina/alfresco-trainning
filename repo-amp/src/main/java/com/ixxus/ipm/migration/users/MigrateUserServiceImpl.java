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
 *
 */
@Service
public class MigrateUserServiceImpl implements MigrateUserService{


    //Static properties
    public static final String KEY_ERROR_SITES = "Sites";
    public static final String KEY_ERROR_GROUPS = "Groups";
    public static final String KEY_ERROR_CONTENT = "Content";
    public static final String KEY_ERROR_FOLDERS = "Folders";
    public static final String KEY_ERROR_COMMENTS = "Comments";
    public static final String KEY_ERROR_USERHOME = "UserHome";
    public static final String KEY_ERROR_TASKINITIATOR = "TaskInitiator";
    public static final String KEY_ERROR_TASKASIGNEE = "TaskAsignee";

    private static final String AND_QUERY = " AND ";
    private static final String CREATOR_QUERY = "@cm\\:creator:\"";

    private static Log logger = LogFactory.getLog(MigrateUserServiceImpl.class);

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


    final Map<String, ArrayList<NodeRef>> notMigrate = new HashMap<>();
    final Map<String, ArrayList<String>> taskNoMigrated = new HashMap<>();

    /**
     * Abstract property to get Local Id from task.
     * @author antoniobarrachina
     *
     */
    @FunctionalInterface
    public interface EngineService{
        public String getLocalId(final String id);
    }

    EngineService engineService = BPMEngineRegistry::getLocalId;

    @Override
    public Map<String, ArrayList<NodeRef>> getNotMigrate() {
        return notMigrate;
    }

    @Override
    public Map<String, ArrayList<String>> getTaskNoMigrated() {
        return taskNoMigrated;
    }

    @Override
    public void migrateSites(final String olduser, final String newuser) {
        String authority = "";
        final List<SiteInfo> sites = siteService.listSites(olduser);
        final List<NodeRef> sitesNotMigrate =  new ArrayList<>();

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
                logger.error("The authority "+ authority + " not exists " + ex.getMessage(),ex);

            }
        }
        notMigrate.put(KEY_ERROR_SITES, (ArrayList<NodeRef>) sitesNotMigrate);
    }

    @Override
    public void migrateGroups(final String olduser, final String newuser) {
        final Set<String> groups = authorityService.getAuthoritiesForUser(olduser);
        final List<NodeRef> groupsNotMigrate  = new ArrayList<>();

        for (final String group:groups){
            try{
                if (!authorityService.getAuthoritiesForUser(newuser).contains(group)){
                    authorityService.addAuthority(group, newuser);
                }
            }
            catch(final UnknownAuthorityException ex){
                groupsNotMigrate.add(authorityService.getAuthorityNodeRef(group));
                logger.error("The authority "+ group + " not exists " + ex.getMessage(), ex);
            }
        }
        notMigrate.put(KEY_ERROR_GROUPS, (ArrayList<NodeRef>) groupsNotMigrate);
    }

    @Override
    public void migrateContent(final String olduser, final String newuser) {
        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"";
        strQuery += AND_QUERY;
        strQuery += CREATOR_QUERY + olduser + "\"";
        changeCreator(strQuery, newuser, KEY_ERROR_CONTENT);

    }

    @Override
    public void migrateFolder(final String olduser, final String newuser) {
        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\"";
        strQuery += AND_QUERY;
        strQuery += CREATOR_QUERY + olduser + "\"";
        changeCreator(strQuery, newuser, KEY_ERROR_FOLDERS);

    }

    @Override
    public void migrateComments(final String olduser, final String newuser) {
        String strQuery="TYPE:\"fm\\:post\"";
        strQuery += AND_QUERY;
        strQuery += CREATOR_QUERY + olduser + "\"";
        changeCreator(strQuery, newuser, KEY_ERROR_COMMENTS);

    }

    @Override
    public void migrateUserHome(final String olduser, final String newuser) {
        final NodeRef oldUserNodeRef = personService.getPerson(olduser);
        final NodeRef newUserNodeRef = personService.getPerson(newuser);
        final NodeRef homespaceOldUserNodeRef = (NodeRef) nodeService.getProperty(oldUserNodeRef, ContentModel.PROP_HOMEFOLDER);
        final NodeRef homespaceNewUserNodeRef = (NodeRef) nodeService.getProperty(newUserNodeRef, ContentModel.PROP_HOMEFOLDER);
        final List<ChildAssociationRef> childs = nodeService.getChildAssocs(homespaceOldUserNodeRef);
        final List<NodeRef> userHomeNotMigrate = new ArrayList<>();

        for (final ChildAssociationRef child:childs){
            final NodeRef node = child.getChildRef();
            final String name = (String) nodeService.getProperty(node, ContentModel.PROP_NAME);
            final NodeRef existNode = nodeService.getChildByName(homespaceNewUserNodeRef, ContentModel.ASSOC_CONTAINS, name);

            if (existNode == null){
                try{
                    final NodeRef newnode = nodeService.moveNode(node, homespaceNewUserNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN).getChildRef();
                    changeCreatorModifier(newnode, newuser, KEY_ERROR_USERHOME);
                }
                catch (final NodeLockedException e)
                {
                    userHomeNotMigrate.add(node);
                    logger.error("The node " + node.toString() + " has locked", e);
                }
            }
            else{
                userHomeNotMigrate.add(node);
                logger.error("File or folder exists in the destination");
            }
        }

        //Adding no migrated elements
        notMigrate.put(KEY_ERROR_USERHOME, (ArrayList<NodeRef>) userHomeNotMigrate);
    }

    @Override
    public void migratePreferences (final String olduser, final String newuser){
        final Map<String, Serializable> preferences = preferenceService.getPreferences(olduser);
        preferenceService.setPreferences(newuser, preferences);
        preferenceService.clearPreferences(olduser);
    }

    @Override
    public void migrateWorkflows(final String olduser, final String newuser) {

        this.changeTaskAsignee(olduser, newuser).changeWorkflowInitiator(olduser, newuser);
    };


    /***
     * Get Workflows started by an user
     * @param oldUserNodeRef Workflow initiator
     * @return
     */
    private List<WorkflowInstance> getWorkflowsByInitiator(final NodeRef oldUserNodeRef) {

        // Searching workflows
        final WorkflowInstanceQuery query = new WorkflowInstanceQuery();
        final Map<QName, Object> filters = new HashMap<>();
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
    private List<WorkflowTask> getWorkflowTask(final WorkflowInstance workflow) {

        final WorkflowTaskQuery query = new WorkflowTaskQuery();
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
    private void migrateUsersForWorkflow(final WorkflowTask task, final String olduser, final String newuser) {

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

    /***
     * Change workflow initiator and initiator home.
     * @param olduser
     * @param newuser
     * @return
     */
    private MigrateUserServiceImpl changeWorkflowInitiator(final String olduser, final String newuser) {

        // Getting noderefs for every person
        final NodeRef oldUserNodeRef = personService.getPerson(olduser);
        final List<WorkflowInstance> listWorkflows = getWorkflowsByInitiator(oldUserNodeRef);
        final ArrayList<String> workflowsOwnerNoMigrated = new ArrayList<>();

        for (final WorkflowInstance workflow : listWorkflows) {
            final List<WorkflowTask> tasks = this.getWorkflowTask(workflow);
            for (final WorkflowTask task : tasks){
                try{
                    this.migrateUsersForWorkflow(task, olduser, newuser);
                }catch (final Exception e) {
                    workflowsOwnerNoMigrated.add(task.getId()+ " " +task.getName());
                    logger.error("Failing setting initiator", e);
                }
            }
        }
        //Require method to migrate the task that "I've started"
        // This value is not getted from the Initiator or Initiator_Home variable, so this step is required
        this.forceMigrateInitiator(olduser, newuser);

        //Setting workflows error map
        this.taskNoMigrated.put(KEY_ERROR_TASKINITIATOR, workflowsOwnerNoMigrated);

        return this;
    }

    /***
     * Updated starteruser from DB
     * @param olduser
     * @param newuser
     */
    private void forceMigrateInitiator(final String olduser, final String newuser){

        final ProcessStarterUser processStarterUser = new ProcessStarterUser(olduser, newuser);
        this.activitiProcessDAO.executeUpdateAuthor(processStarterUser);
    }

    /***
     * Change asignee workflow tasks
     */
    private MigrateUserServiceImpl changeTaskAsignee(final String olduser, final String newuser) {

        // Searching workflows with olduser like asignee
        final WorkflowTaskQuery query = new WorkflowTaskQuery();
        query.setActorId(olduser);
        final WorkflowService workflowService = serviceRegistry.getWorkflowService();
        final List<WorkflowTask> listTasks = workflowService.queryTasks(query, true);
        final ArrayList<String> workflowsAsigneeNoMigrated = new ArrayList<>();
        //For every task we will set de asignee.
        for (final WorkflowTask task : listTasks) {
            final String assignee = (String) task.getProperties()
                    .get(QName.createQName("{http://www.alfresco.org/model/content/1.0}owner"));
            try{
                final String taskId = engineService.getLocalId(task.getId());

                if ((assignee != null) && assignee.equalsIgnoreCase(olduser)) {
                    taskService.setAssignee(taskId, newuser);
                }
            }catch(final Exception e){
                workflowsAsigneeNoMigrated.add(task.getId()+ " " +task.getName());
                logger.error("Failing setting asignee", e);
            }
        }

        //Setting workflows error map
        this.taskNoMigrated.put(KEY_ERROR_TASKASIGNEE, workflowsAsigneeNoMigrated);

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
            contentNotMigrate = new ArrayList<>();
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
            logger.debug("The noderef "+ node.toString() + " can't migrate " + ex.getMessage(), ex);
        }
        finally
        {
            // Enable auditable aspect
            policyBehaviourFilter.enableBehaviour(node, ContentModel.ASPECT_AUDITABLE);
            notMigrate.put(typeContent, (ArrayList<NodeRef>) contentNotMigrate);
        }
    }

}