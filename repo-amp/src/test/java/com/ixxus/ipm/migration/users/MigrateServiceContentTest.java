package com.ixxus.ipm.migration.users;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.search.AbstractResultSet;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.OwnableService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

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
    private MigrateUserService migrateUserService;

    @Mock
    private SearchService searchService;

    @Mock
    private NodeService nodeService;

    @Mock
    private ResultSet rs;

    @Mock
    private BehaviourFilter policyBehaviourFilter;

    @Mock
    private OwnableService ownableService;



    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);



        //Doing mock for MigrateService.changeCreator
        content1 = new NodeRef("workspace://SpacesStore/contentnode1");
        content2 = new NodeRef("workspace://SpacesStore/contentnode2");

        //Doing mock for changeCreatorModifier
        final NodeRef folderNodeRef = new NodeRef("workspace://SpacesStore/oldnoderef");
        final NodeRef contentNodeRef = new NodeRef("workspace://SpacesStore/oldnoderef");

    }

    @Test
    public void testMigrateContent() {

        final ResultSet rs = mock(AbstractResultSet.class);
        final List<NodeRef> listNodeRefs = new ArrayList<>();
        listNodeRefs.add(content1);
        listNodeRefs.add(content2);

        when(searchService.query(eq(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), eq(SearchService.LANGUAGE_LUCENE), any(String.class))).
        thenReturn(rs);
        when(nodeService.getType(any(NodeRef.class))).thenReturn(ContentModel.TYPE_CONTENT);
        when(rs.getNodeRefs()).thenReturn(listNodeRefs);

        migrateUserService.migrateContent(olduser, newuser);
        verify(nodeService, times(1)).setProperty(content1, ContentModel.PROP_CREATOR, newuser);
        verify(nodeService, times(1)).setProperty(content2, ContentModel.PROP_CREATOR, newuser);
        verify(ownableService, times(1)).setOwner(content1, newuser);
        verify(ownableService, times(1)).setOwner(content2, newuser);
        verify(nodeService, times(1)).setProperty(content1, ContentModel.PROP_MODIFIER, newuser);
        verify(nodeService, times(1)).setProperty(content2, ContentModel.PROP_MODIFIER, newuser);
    }

    @Test
    public void testMigrateComment() {

        final ResultSet rs = mock(AbstractResultSet.class);
        final List<NodeRef> listNodeRefs = new ArrayList<>();
        listNodeRefs.add(content1);
        listNodeRefs.add(content2);

        when(searchService.query(eq(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), eq(SearchService.LANGUAGE_LUCENE), any(String.class))).
        thenReturn(rs);
        when(nodeService.getType(any(NodeRef.class))).thenReturn(ContentModel.TYPE_CONTENT);
        when(rs.getNodeRefs()).thenReturn(listNodeRefs);

        migrateUserService.migrateComments(olduser, newuser);
        verify(nodeService, times(1)).setProperty(content1, ContentModel.PROP_CREATOR, newuser);
        verify(nodeService, times(1)).setProperty(content2, ContentModel.PROP_CREATOR, newuser);
        verify(ownableService, times(1)).setOwner(content1, newuser);
        verify(ownableService, times(1)).setOwner(content2, newuser);
        verify(nodeService, times(1)).setProperty(content1, ContentModel.PROP_MODIFIER, newuser);
        verify(nodeService, times(1)).setProperty(content2, ContentModel.PROP_MODIFIER, newuser);
    }
    
    @Test
    public void testMigrateFolder() {

        final ResultSet rs = mock(AbstractResultSet.class);
        final List<NodeRef> listNodeRefs = new ArrayList<>();
        listNodeRefs.add(content1);
        listNodeRefs.add(content2);

        when(searchService.query(eq(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE), eq(SearchService.LANGUAGE_LUCENE), any(String.class))).
        thenReturn(rs);
        when(nodeService.getType(any(NodeRef.class))).thenReturn(ContentModel.TYPE_FOLDER);
        when(rs.getNodeRefs()).thenReturn(listNodeRefs);

        migrateUserService.migrateFolder(olduser, newuser);
        verify(nodeService, times(1)).setProperty(content1, ContentModel.PROP_CREATOR, newuser);
        verify(nodeService, times(1)).setProperty(content2, ContentModel.PROP_CREATOR, newuser);
        verify(ownableService, times(1)).setOwner(content1, newuser);
        verify(ownableService, times(1)).setOwner(content2, newuser);
        verify(nodeService, times(1)).setProperty(content1, ContentModel.PROP_MODIFIER, newuser);
        verify(nodeService, times(1)).setProperty(content2, ContentModel.PROP_MODIFIER, newuser);
    }

}
