package com.ixxus.ipm.migration.users.action.executer;

import static org.mockito.AdditionalMatchers.and;
import static org.mockito.AdditionalMatchers.not;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.ixxus.ipm.migration.users.MigrateUserServiceImpl;
import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

@RunWith(RemoteTestRunner.class)@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class MigrateActionExecuterTest {

    @Inject
    @InjectMocks
    private MigrateActionExecuter migrateActionExecuter;
    
    @Mock
    private MigrateUserServiceImpl migrateServiceImpl;
    
    @Mock
    private SearchService searchService;
    
    @Mock
    private ServiceRegistry serviceRegistry;
    
    @Mock
    private Action actionMail;
    
    @Mock 
    private ActionService actionService;

    
    private String olduser = "olduser";
    private String newuser = "newuser";
    
    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);        
        ResultSet resultSet = mock(ResultSet.class);
        
        NodeRef templateNodeRef = new NodeRef("workspace://SpacesStore/template");
        final Map<String, ArrayList<NodeRef>> noMigrated = createNoMigratedMap();
        final Map<String, ArrayList<String>> taskNoMigrated = createTaskNoMigratedMap();
        
        when(resultSet.length()).thenReturn(1);
        when(resultSet.getNodeRef(0)).thenReturn(templateNodeRef);
        when(searchService.query(any(StoreRef.class),eq(SearchService.LANGUAGE_LUCENE),any(String.class))).thenReturn(resultSet);
        when(migrateServiceImpl.getNotMigrate()).thenReturn(noMigrated);
        when(serviceRegistry.getActionService()).thenReturn(actionService);
        when(actionService.createAction(any(String.class))).thenReturn(actionMail);
    }
	
	@Test
	public void executeImplTest() {
		
		//Mocking action
		Action action = mock(Action.class);
		when(action.getParameterValue(and(not(eq(MigrateActionExecuter.PARAM_NEW_USER)),not(eq(MigrateActionExecuter.PARAM_OLD_USER))))).thenReturn("true");
		when(action.getParameterValue(MigrateActionExecuter.PARAM_NEW_USER)).thenReturn(newuser);
		when(action.getParameterValue(MigrateActionExecuter.PARAM_OLD_USER)).thenReturn(olduser);
		
		//Dummy NodeRef
		NodeRef nodeRef = new NodeRef("workspace://SpacesStore/contentnode1");
		
		final Map<String, Serializable> templateArgs = buildReturnedTemplate();
		
		//Executing tested method
		migrateActionExecuter.executeImpl(action, nodeRef);	
		
		verify(migrateServiceImpl,times(1)).migrateWorkflows(olduser, newuser, true);
		verify(migrateServiceImpl,times(1)).migrateSites(olduser, newuser, true);
		verify(migrateServiceImpl,times(1)).migrateGroups(olduser, newuser, true);
		verify(migrateServiceImpl,times(1)).migrateContent(olduser, newuser, true);
		verify(migrateServiceImpl,times(1)).migrateFolder(olduser, newuser, true);
		verify(migrateServiceImpl,times(1)).migrateComments(olduser, newuser, true);
		verify(migrateServiceImpl,times(1)).migrateUserHome(olduser, newuser, true);
		verify(migrateServiceImpl,times(1)).migratePreferences(olduser, newuser, true);
		StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
		verify(searchService).query(storeRef,SearchService.LANGUAGE_LUCENE, migrateActionExecuter.getPathTemplate());

		
	}
	
	private Map<String, ArrayList<NodeRef>> createNoMigratedMap(){
		
		final Map<String, ArrayList<NodeRef>> noMigrated = new HashMap<>();
		
		noMigrated.put(MigrateUserServiceImpl.KEY_ERROR_SITES, createDummyArrayNodeRef());
		noMigrated.put(MigrateUserServiceImpl.KEY_ERROR_GROUPS, createDummyArrayNodeRef());
		noMigrated.put(MigrateUserServiceImpl.KEY_ERROR_CONTENT, createDummyArrayNodeRef());
		noMigrated.put(MigrateUserServiceImpl.KEY_ERROR_FOLDERS, createDummyArrayNodeRef());
		noMigrated.put(MigrateUserServiceImpl.KEY_ERROR_COMMENTS, createDummyArrayNodeRef());
		noMigrated.put(MigrateUserServiceImpl.KEY_ERROR_USERHOME, createDummyArrayNodeRef());
		
		return noMigrated;
	}
	
	private ArrayList<NodeRef> createDummyArrayNodeRef(){
		ArrayList<NodeRef> arrayList = new ArrayList<>();
		arrayList.add(new NodeRef("workspace://SpacesStore/dummy"));
		return arrayList;
	}
	
	private Map<String, ArrayList<String>> createTaskNoMigratedMap(){
		final Map<String, ArrayList<String>> noMigrated = new HashMap<>();
		
		ArrayList<String> arrayList = new ArrayList<>();
		arrayList.add("dummy");
		noMigrated.put(MigrateUserServiceImpl.KEY_ERROR_WORKFLOW, arrayList);
		
		return noMigrated;
	}
	
	private Map<String, Serializable> buildReturnedTemplate(){
		
		final Map<String, Serializable> templateArgs = new HashMap<String, Serializable>();
		ArrayList<String> arrayList = new ArrayList<>();
		arrayList.add("dummy");
		
		templateArgs.put("sitesNotMigrate", createDummyArrayNodeRef());
		templateArgs.put("groupsNotMigrate", createDummyArrayNodeRef());
		templateArgs.put("contentNotMigrate", createDummyArrayNodeRef());
		templateArgs.put("foldersNotMigrate", createDummyArrayNodeRef());
		templateArgs.put("commentsNotMigrate", createDummyArrayNodeRef());
		templateArgs.put("userHomeNotMigrate", createDummyArrayNodeRef());
		templateArgs.put("taskInitiatorNoMigrated", arrayList);
		templateArgs.put("taskAsigneeNoMigrated", arrayList);
		
		return templateArgs;
	}
	
	
    

}
