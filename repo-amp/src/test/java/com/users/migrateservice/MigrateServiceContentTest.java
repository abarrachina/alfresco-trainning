package com.users.migrateservice;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.powermock.api.mockito.PowerMockito.mock;


import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.search.AbstractResultSet;
import org.alfresco.repo.search.impl.lucene.LuceneResultSet;
import org.alfresco.repo.search.impl.lucene.SolrJSONResultSet;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetMetaData;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.apache.lucene.search.Hits;

import com.tradeshift.test.remote.Remote;
import com.tradeshift.test.remote.RemoteTestRunner;

@RunWith(RemoteTestRunner.class)@Remote(runnerClass = SpringJUnit4ClassRunner.class)
//@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class MigrateServiceContentTest {

    
    private static String newuser = "NewUser";
    private static String olduser = "OldUser";
    private NodeRef content1, content2;
	
    @Inject
	@InjectMocks
    private MigrateService migrateService;
	
    @Mock
    private SearchService searchService;

    @Mock
    private NodeService nodeService;
    
    @Mock
    private ResultSet rs;
    
    @Mock
    private BehaviourFilter policyBehaviourFilter;
    
    
    
	@Before
	public void setUp() throws Exception {
		MockitoAnnotations.initMocks(this);


		
		//Doing mock for MigrateService.changeCreator
		content1 = new NodeRef("workspace://SpacesStore/contentnode1");
		content2 = new NodeRef("workspace://SpacesStore/contentnode2");
		
		//Doing mock for changeCreatorModifier
		NodeRef folderNodeRef = new NodeRef("workspace://SpacesStore/oldnoderef");
		NodeRef contentNodeRef = new NodeRef("workspace://SpacesStore/oldnoderef");		
		
	}

	@Test
	public void testMigrateContent() {
		
		ResultSet rs = mock(AbstractResultSet.class);
		List<NodeRef> listNodeRefs = new ArrayList<>();
		listNodeRefs.add(content1);
		listNodeRefs.add(content2);
		
		when(searchService.query(eq(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), eq(SearchService.LANGUAGE_LUCENE), any(String.class))).
		thenReturn(rs);
		when(nodeService.getType(any(NodeRef.class))).thenReturn(ContentModel.TYPE_CONTENT);
		when(rs.getNodeRefs()).thenReturn(listNodeRefs);
		
		migrateService.migrateContent(olduser, newuser);
		verify(nodeService, times(1)).setProperty(content1, ContentModel.PROP_CREATOR, newuser);
		verify(nodeService, times(1)).setProperty(content2, ContentModel.PROP_CREATOR, newuser);
		verify(nodeService, times(1)).setProperty(content1, ContentModel.PROP_MODIFIER, newuser);
		verify(nodeService, times(1)).setProperty(content2, ContentModel.PROP_MODIFIER, newuser);
	}
	
	@Test
	public void testMigrateFolder() {
		
		ResultSet rs = mock(AbstractResultSet.class);
		List<NodeRef> listNodeRefs = new ArrayList<>();
		listNodeRefs.add(content1);
		listNodeRefs.add(content2);
		
		when(searchService.query(eq(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), eq(SearchService.LANGUAGE_LUCENE), any(String.class))).
		thenReturn(rs);
		when(nodeService.getType(any(NodeRef.class))).thenReturn(ContentModel.TYPE_FOLDER);
		when(rs.getNodeRefs()).thenReturn(listNodeRefs);
		
		migrateService.migrateContent(olduser, newuser);
		verify(nodeService, times(1)).setProperty(content1, ContentModel.PROP_CREATOR, newuser);
		verify(nodeService, times(1)).setProperty(content2, ContentModel.PROP_CREATOR, newuser);
		verify(nodeService, times(1)).setProperty(content1, ContentModel.PROP_MODIFIER, newuser);
		verify(nodeService, times(1)).setProperty(content2, ContentModel.PROP_MODIFIER, newuser);
	}

}
