package com.users.migrateservice;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import org.activiti.engine.TaskService;
import org.alfresco.model.ContentModel;
import org.alfresco.repo.workflow.BPMEngineRegistry;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.workflow.WorkflowPath;
import org.alfresco.service.cmr.workflow.WorkflowService;
import org.alfresco.service.cmr.workflow.WorkflowTask;
import org.alfresco.service.cmr.workflow.WorkflowTaskDefinition;
import org.alfresco.service.cmr.workflow.WorkflowTaskQuery;
import org.alfresco.service.cmr.workflow.WorkflowTaskState;
import org.alfresco.service.namespace.QName;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.tradeshift.test.remote.Remote;

@RunWith(PowerMockRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@PrepareForTest(BPMEngineRegistry.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class MigrateServiceWorkflowsTest{

    private static String newuser = "NewUser";
    private static String olduser = "OldUser";
    private static final String URI = "http://test";
    private final List<WorkflowTask> listTasks = new ArrayList<WorkflowTask>();
    @Inject
    @InjectMocks
    private MigrateService migrateService;

    @Mock
    private PersonService personService;

    @Mock
    private WorkflowService workflowService;

    @Mock
    private TaskService taskService;

    @Mock
    private ServiceRegistry serviceRegistry;

    @Before
    public void setUp() {

        PowerMockito.mockStatic(BPMEngineRegistry.class);
        PowerMockito.when(BPMEngineRegistry.getLocalId(any(String.class))).thenReturn("task1");

        MockitoAnnotations.initMocks(this);


        final Date date = new Date();
        final WorkflowTask task1 = makeTask(date);
        listTasks.add(task1);


        when(serviceRegistry.getWorkflowService()).thenReturn(workflowService);
        when(workflowService.queryTasks(any(WorkflowTaskQuery.class), eq(true))).thenReturn(listTasks);
    }

    @Test
    public void testMigrateGroups() {
        migrateService.migrateWorkflows(olduser, newuser);
        verify(taskService, times(1)).setAssignee(any(String.class), eq(newuser));

    }

    protected WorkflowTask makeTask(final Date date) {
        final String description = "Task Description";
        final String id = "task1";
        final String name = "Task Name";
        final WorkflowTaskState state = WorkflowTaskState.IN_PROGRESS;
        final String title = "Task Title";
        final WorkflowPath path = null;
        final WorkflowTaskDefinition definition = null;
        final HashMap<QName, Serializable> properties = makeTaskProperties(date);
        return new WorkflowTask(id, definition, name, title, description, state, path, properties);
    }

    protected HashMap<QName, Serializable> makeTaskProperties(final Date date) {
        final HashMap<QName, Serializable> properties = new HashMap<QName, Serializable>();
        properties.put(ContentModel.PROP_OWNER, olduser);
        final QName testInt = QName.createQName(URI, "int");
        properties.put(testInt, 5);
        final QName testBoolean = QName.createQName(URI, "boolean");
        properties.put(testBoolean, false);
        final QName testString = QName.createQName(URI, "string");
        properties.put(testString, "foo bar");
        final QName testDate = QName.createQName(URI, "date");
        properties.put(testDate, date);
        return properties;
    }

}