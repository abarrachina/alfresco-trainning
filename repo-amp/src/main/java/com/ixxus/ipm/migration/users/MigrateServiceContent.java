package com.ixxus.ipm.migration.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.OwnableService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

/***
 *
 * @author nazareth.jimenez@ixxus.com
 * Migrate Service Content
 *
 */
@Service
public class MigrateServiceContent implements MigrateService{


    //Static properties
    public static final String KEY_ERROR_CONTENT = "Content";
    public static final String KEY_ERROR_FOLDERS = "Folders";
    public static final String KEY_ERROR_COMMENTS = "Comments";

    private static final String AND_QUERY = " AND ";
    private static final String CREATOR_QUERY = "@cm\\:creator:\"";

    private static Log logger = LogFactory.getLog(MigrateServiceContent.class);

    @Inject
    private NodeService nodeService;

    @Inject
    private SearchService searchService;

    @Inject
    private BehaviourFilter policyBehaviourFilter;

    @Inject
    private OwnableService ownableService;


    final Map<String, ArrayList<NodeRef>> notMigrate = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, ArrayList<NodeRef>> getNotMigrate() {
        return notMigrate;
    }

    @Override
    public void migrate(final String olduser, final String newuser) {
        migrateContent(olduser, newuser);
        migrateFolder(olduser, newuser);
        migrateComments(olduser, newuser);

    }

    private void migrateContent(final String olduser, final String newuser) {
        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"";
        strQuery += AND_QUERY;
        strQuery += CREATOR_QUERY + olduser + "\"";
        changeCreator(strQuery, newuser, KEY_ERROR_CONTENT);

    }

    private void migrateFolder(final String olduser, final String newuser) {
        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\"";
        strQuery += AND_QUERY;
        strQuery += CREATOR_QUERY + olduser + "\"";
        changeCreator(strQuery, newuser, KEY_ERROR_FOLDERS);

    }

    private void migrateComments(final String olduser, final String newuser) {
        String strQuery="TYPE:\"fm\\:post\"";
        strQuery += AND_QUERY;
        strQuery += CREATOR_QUERY + olduser + "\"";
        changeCreator(strQuery, newuser, KEY_ERROR_COMMENTS);

    }

    /***
     * Change noderef's creator and modifier
     *
     * @param strQuery
     * @param newuser
     */
    protected void changeCreator(final String strQuery, final String newuser, final String typeContent){
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
    protected void changeCreatorModifier (final NodeRef node, final String newuser, final String typeContent){

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