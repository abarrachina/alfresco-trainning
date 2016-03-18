package com.ixxus.ipm.migration.users;

import java.util.List;

import javax.inject.Inject;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.NodeLockedException;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

@Service
public class MigrateServiceUserHome extends AbstractMigrateService {

    public static final String KEY_ERROR_USERHOME = "UserHome";

    @Inject
    private PersonService personService;

    @Inject
    private NodeService nodeService;

    private static final Log LOGGER = LogFactory.getLog(MigrateUserServiceImpl.class);

    @Override
    public void migrate(final String olduser, final String newuser) {
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
                try{
                    final NodeRef newnode = nodeService.moveNode(node, homespaceNewUserNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN).getChildRef();
                    changeCreatorModifier(newnode, newuser, KEY_ERROR_USERHOME);
                }
                catch (final NodeLockedException e)
                {
                    this.addNodeNotMigrate(node);
                    LOGGER.error("The node " + node.toString() + " has locked", e);
                }
            }
            else{
                this.addNodeNotMigrate(node);
                LOGGER.error("File or folder exists in the destination");
            }
        }
    }

}
