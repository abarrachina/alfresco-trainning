package com.users.migrateservice;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.service.cmr.preference.PreferenceService;
import org.alfresco.service.cmr.repository.ChildAssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.ResultSetRow;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PersonService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

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
    private PreferenceService preferenceService;

    @Override
    public void migrateSites(final String olduser, final String newuser) {
        String authority = "";
        final List<SiteInfo> sites = siteService.listSites(olduser);

        for (final SiteInfo site: sites){
            final String role = siteService.getMembersRole(site.getShortName(), olduser);
            try{
                authority = "GROUP_site_"+site.getShortName()+"_"+role;
                if (!authorityService.getAuthoritiesForUser(newuser).contains(authority)){
                    authorityService.addAuthority(authority, newuser);
                }
            }
            catch(final UnknownAuthorityException ex){
                logger.debug("The authority "+ authority + " not exists " + ex.getMessage());
            }
        }

    }

    @Override
    public void migrateGroups(final String olduser, final String newuser) {
        final Set<String> groups = authorityService.getAuthoritiesForUser(olduser);

        for (final String group:groups){
            if (!authorityService.getAuthoritiesForUser(newuser).contains(group)){
                authorityService.addAuthority(group, newuser);
            }
        }
    }

    @Override
    public void migrateContent(final String olduser, final String newuser) {
        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"";
        strQuery += " AND ";
        strQuery += "@cm\\:creator:\"" + olduser + "\"";
        changeCreator(strQuery, newuser);

    }

    @Override
    public void migrateFolder(final String olduser, final String newuser) {
        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\"";
        strQuery += " AND ";
        strQuery += "@cm\\:creator:\"" + olduser + "\"";
        changeCreator(strQuery, newuser);

    }

    @Override
    public void migrateComments(final String olduser, final String newuser) {
        String strQuery="TYPE:\"fm\\:post\"";
        strQuery += " AND ";
        strQuery += "@cm\\:creator:\"" + olduser + "\"";
        changeCreator(strQuery, newuser);

    }

    @Override
    public void migrateUserHome(final String olduser, final String newuser) {
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
                final NodeRef newnode = nodeService.moveNode(node, homespaceNewUserNodeRef, ContentModel.ASSOC_CONTAINS, ContentModel.ASSOC_CHILDREN).getChildRef();
                changeCreatorModifier(newnode, newuser);
            }
            else{
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
        changeCreator(strQuery, newuser);
    }

    /***
     * Change noderef's creator and modifier
     *
     * @param strQuery
     * @param newuser
     */
    private void changeCreator(final String strQuery, final String newuser){
        ResultSet results = null;
        try{
            results = searchService.query(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, SearchService.LANGUAGE_LUCENE, strQuery);

            for (final ResultSetRow result:results){

                final NodeRef nodeRef = result.getNodeRef();
                changeCreatorModifier(nodeRef, newuser);
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
    private void changeCreatorModifier (final NodeRef node, final String newuser){

        if (nodeService.getType(node).equals(ContentModel.TYPE_FOLDER)){
            final List<ChildAssociationRef> childs = nodeService.getChildAssocs(node);
            for (final ChildAssociationRef child:childs){
                changeCreatorModifier(child.getChildRef(), newuser);
            }
        }

        // Disable auditable aspect to allow change properties of cm:auditable aspect
        policyBehaviourFilter.disableBehaviour(node, ContentModel.ASPECT_AUDITABLE);

        // Update properties of cm:auditable aspect
        nodeService.setProperty(node, ContentModel.PROP_CREATOR, newuser);
        nodeService.setProperty(node, ContentModel.PROP_MODIFIER, newuser);

        // Enable auditable aspect
        policyBehaviourFilter.enableBehaviour(node, ContentModel.ASPECT_AUDITABLE);

    }

}