package com.users.migrateservice;


import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.namespace.QName;
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


@RunWith(RemoteTestRunner.class)
@Remote(runnerClass = SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:alfresco/application-context.xml")
public class MigrateServiceUserHomeTest {

    private static String newuser = "NewUser";
    private static String olduser = "OldUser";
    private static NodeRef nodeRefOldHome = new NodeRef("workspace://SpacesStore/oldhomenoderef");
    private static NodeRef nodeRefNewHome = new NodeRef("workspace://SpacesStore/newhomenoderef");
    private static NodeRef nodeRefOldUser = new NodeRef("workspace://SpacesStore/oldnoderef");
    private static NodeRef nodeRefNewUser = new NodeRef("workspace://SpacesStore/newnoderef");
    private static NodeRef nodeRefChild1OldUser = new NodeRef("workspace://SpacesStore/oldnoderefchild1");
    private static NodeRef nodeRefChild1NewUser = new NodeRef("workspace://SpacesStore/newnoderefchild1");
    private ChildAssociationRef child1, child2;


    @Inject
    @InjectMocks
    private MigrateService migrateService;

    @Mock
    private PersonService personService;

    @Mock
    private NodeService nodeService;

    @Mock
    private BehaviourFilter policyBehaviourFilter;

    @Test
    public void testWiring() {
        assertNotNull(migrateService);
    }

    @Before
    public void setUp(){
        MockitoAnnotations.initMocks(this);

        when(personService.getPerson(olduser)).thenReturn(nodeRefOldUser);
        when(personService.getPerson(newuser)).thenReturn(nodeRefNewUser);

        when(nodeService.getProperty(nodeRefOldUser, ContentModel.PROP_HOMEFOLDER)).thenReturn(nodeRefOldHome);
        when(nodeService.getProperty(nodeRefNewUser, ContentModel.PROP_HOMEFOLDER)).thenReturn(nodeRefNewHome);

        final List<ChildAssociationRef> childsOldHome = new ArrayList<>();

        child1 = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, nodeRefOldHome, QName.createQName("Child1Assocs"), nodeRefChild1OldUser);
        child2 = new ChildAssociationRef(ContentModel.ASSOC_CONTAINS, nodeRefNewHome, QName.createQName("Child2Assocs"), nodeRefChild1NewUser);
        childsOldHome.add(child1);

        when(nodeService.getChildAssocs(nodeRefOldHome)).thenReturn(childsOldHome);
        when(nodeService.getChildByName(eq(nodeRefNewHome), eq(ContentModel.ASSOC_CONTAINS), any(String.class))).thenReturn(null);
        when(nodeService.moveNode(nodeRefChild1OldUser, nodeRefNewHome,  ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN)).thenReturn(child2);
        when(nodeService.getType(any(NodeRef.class))).thenReturn(ContentModel.TYPE_CONTENT);

    }

    @Test
    public void testMigrateHome() {
        migrateService.migrateUserHome(olduser, newuser);
        verify(nodeService, times(1)).setProperty(child2.getChildRef(), ContentModel.PROP_CREATOR, newuser);
        verify(nodeService, times(1)).setProperty(child2.getChildRef(), ContentModel.PROP_MODIFIER, newuser);
    }

}