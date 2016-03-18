package com.ixxus.ipm.migration.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.activiti.engine.TaskService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.repo.workflow.WorkflowConstants;
import org.alfresco.repo.workflow.WorkflowNodeConverter;
import org.alfresco.repo.workflow.activiti.ActivitiNodeConverter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
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

@Service
public class MigrateServiceWorkflow implements MigrateService {

    @Inject
    private ServiceRegistry serviceRegistry;

    @Inject
    private PersonService personService;

    @Inject
    private NodeService nodeService;

    @Inject
    private TaskService taskService;

    @Inject
    private ActivitiProcessDAO activitiProcessDAO ;

    private final List<String> notMigrated = new ArrayList<>();

    private static final Log LOGGER = LogFactory.getLog(MigrateUserServiceImpl.class);


    @FunctionalInterface
    public interface EngineService{
        public String getLocalId(final String id);
    }

    EngineService engineService = BPMEngineRegistry::getLocalId;


    @SuppressWarnings("unchecked")
    @Override
    public List<String> getNotMigrate() {

        return notMigrated;
    }


    @Override
    public void migrate(final String olduser, final String newuser) {
        this.changeTaskAsignee(olduser, newuser).changeWorkflowInitiator(olduser, newuser);
    }

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
    private MigrateServiceWorkflow changeWorkflowInitiator(final String olduser, final String newuser) {

        // Getting noderefs for every person
        final NodeRef oldUserNodeRef = personService.getPerson(olduser);
        final List<WorkflowInstance> listWorkflows = getWorkflowsByInitiator(oldUserNodeRef);

        for (final WorkflowInstance workflow : listWorkflows) {
            final List<WorkflowTask> tasks = this.getWorkflowTask(workflow);
            for (final WorkflowTask task : tasks){
                try{
                    this.migrateUsersForWorkflow(task, olduser, newuser);
                }catch (final Exception e) {
                    this.notMigrated.add(task.getId()+ " " +task.getName());
                    LOGGER.error("Failing setting initiator", e);
                }
            }
        }
        //Require method to migrate the task that "I've started"
        // This value is not getted from the Initiator or Initiator_Home variable, so this step is required
        this.forceMigrateInitiator(olduser, newuser);

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
    private MigrateServiceWorkflow changeTaskAsignee(final String olduser, final String newuser) {

        // Searching workflows with olduser like asignee
        final WorkflowTaskQuery query = new WorkflowTaskQuery();
        query.setActorId(olduser);
        final WorkflowService workflowService = serviceRegistry.getWorkflowService();
        final List<WorkflowTask> listTasks = workflowService.queryTasks(query, true);
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
                this.notMigrated.add(task.getId()+ " " +task.getName());
                LOGGER.error("Failing setting asignee", e);
            }
        }

        return this;
    }

}
