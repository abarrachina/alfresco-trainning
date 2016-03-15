package com.repo.action.executer;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.repo.action.executer.MailActionExecuter;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ActionService;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import com.users.migrateservice.MigrateService;

public class MigrateActionExecuter extends ActionExecuterAbstractBase
{
    private static Log logger = LogFactory.getLog(MigrateActionExecuter.class);
    public static final String NAME = "migrate-action";
    public static final String PARAM_NEW_USER = "newuser";
    public static final String PARAM_OLD_USER = "olduser";
    public static final String PARAM_SITES = "sites";
    public static final String PARAM_GROUPS = "groups";
    public static final String PARAM_CONTENT = "content";
    public static final String PARAM_COMMENT = "comments";
    public static final String PARAM_USERHOME = "userhome";
    public static final String PARAM_FAVORITES = "favorites";
    public static final String PARAM_WORKFLOWS = "workflows";

    @Value("${mail.to.migration}")
    private String email;

    private MigrateService migrateServiceImpl;

    public void setMigrateService(final MigrateService migrateServiceImpl) {
        this.migrateServiceImpl = migrateServiceImpl;
    }

    private PersonService personService;

    public void setPersonService(final PersonService  personService) {
        this. personService =  personService;
    }

    private ServiceRegistry serviceRegistry;

    public void setServiceRegistry(final ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }

    private SearchService searchService;

    public void setSearchService(final SearchService searchService) {
        this.searchService = searchService;
    }


    @Override
    protected void addParameterDefinitions(final List<ParameterDefinition> paramList) {
        paramList.add(new ParameterDefinitionImpl(PARAM_NEW_USER, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_NEW_USER)));
        paramList.add(new ParameterDefinitionImpl(PARAM_OLD_USER, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_OLD_USER)));
        paramList.add(new ParameterDefinitionImpl(PARAM_SITES, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_SITES)));
        paramList.add(new ParameterDefinitionImpl(PARAM_GROUPS, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_GROUPS)));
        paramList.add(new ParameterDefinitionImpl(PARAM_CONTENT, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_CONTENT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_COMMENT, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_COMMENT)));
        paramList.add(new ParameterDefinitionImpl(PARAM_USERHOME, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_USERHOME)));
        paramList.add(new ParameterDefinitionImpl(PARAM_FAVORITES, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_FAVORITES)));
        paramList.add(new ParameterDefinitionImpl(PARAM_WORKFLOWS, DataTypeDefinition.TEXT, false, getParamDisplayLabel(PARAM_WORKFLOWS)));

    }

    @Override
    protected void executeImpl(final Action action, final NodeRef actionedUponNodeRef) {
        final Serializable paramNewUser = action.getParameterValue(PARAM_NEW_USER);
        final Serializable paramOldUser = action.getParameterValue(PARAM_OLD_USER);
        final Serializable paramSites = action.getParameterValue(PARAM_SITES);
        final Serializable paramGroups = action.getParameterValue(PARAM_GROUPS);
        final Serializable paramContent = action.getParameterValue(PARAM_CONTENT);
        final Serializable paramComment = action.getParameterValue(PARAM_COMMENT);
        final Serializable paramUserHome = action.getParameterValue(PARAM_USERHOME);
        final Serializable paramFavorites = action.getParameterValue(PARAM_FAVORITES);
        final Serializable paramWorkflows = action.getParameterValue(PARAM_WORKFLOWS);

        final String newuser = paramNewUser.toString();
        final String olduser = paramOldUser.toString();
        final String sites = paramSites.toString();
        final String groups = paramGroups.toString();
        final String content = paramContent.toString();
        final String comments = paramComment.toString();
        final String userhome = paramUserHome.toString();
        final String favorites = paramFavorites.toString();
        final String workflows = paramWorkflows.toString();

        if ((workflows != null) && (workflows.equalsIgnoreCase("true"))){
            migrateServiceImpl.migrateWorkflows(olduser, newuser);
        }
        if ((sites != null) && (sites.equalsIgnoreCase("true"))){
            migrateServiceImpl.migrateSites(olduser, newuser);
        }
        if ((groups != null) && (groups.equalsIgnoreCase("true"))){
            migrateServiceImpl.migrateGroups(olduser, newuser);
        }
        if ((content != null) && (content.equalsIgnoreCase("true"))){
            migrateServiceImpl.migrateContent(olduser, newuser);
            migrateServiceImpl.migrateFolder(olduser, newuser);
        }
        if ((comments != null) && (comments.equalsIgnoreCase("true"))){
            migrateServiceImpl.migrateComments(olduser, newuser);
        }
        if ((userhome != null) && (userhome.equalsIgnoreCase("true"))){
            migrateServiceImpl.migrateUserHome(olduser, newuser);
        }
        if ((favorites != null) && (favorites.equalsIgnoreCase("true"))){
            migrateServiceImpl.migratePreferences(olduser, newuser);
        }

        sendEmail();
    }

    /***
     * Send the email with the content that the process can't migrate
     */
    private void sendEmail(){
        final ActionService actionService = serviceRegistry.getActionService();
        final Action mailAction = actionService.createAction(MailActionExecuter.NAME);
        mailAction.setParameterValue(MailActionExecuter.PARAM_SUBJECT, "Migration Information");

        final StoreRef storeRef = new StoreRef(StoreRef.PROTOCOL_WORKSPACE, "SpacesStore");
        ResultSet rs;
        NodeRef template = null;

        rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, "PATH:\"/app:company_home/app:dictionary/app:email_templates/cm:notify_user_email_migration.html.ftl\"");

        if ((rs != null) && (rs.length()>0)) {
            template = rs.getNodeRef(0);

            mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE, template);
            mailAction.setParameterValue(MailActionExecuter.PARAM_TO, email);

            final Map<String, Serializable> templateArgs = prepareTemplate();

            mailAction.setParameterValue(MailActionExecuter.PARAM_TEMPLATE_MODEL,(Serializable)templateArgs);
            actionService.executeAction(mailAction, null);
        }
        else{
            logger.debug("The email template doesn't exist");
        }

    }

    /***
     *
     * @return email template
     */
    private Map<String, Serializable> prepareTemplate(){
        final Map<String, Serializable> templateArgs = new HashMap<String, Serializable>();
        final Map<String, ArrayList<NodeRef>> notMigrate = migrateServiceImpl.getNotMigrate();
        final ArrayList<NodeRef> sitesNotMigrate = notMigrate.get("Sites");
        final ArrayList<NodeRef> groupsNotMigrate = notMigrate.get("Groups");
        final ArrayList<NodeRef> contentNotMigrate = notMigrate.get("Content");
        final ArrayList<NodeRef> foldersNotMigrate = notMigrate.get("Folders");
        final ArrayList<NodeRef> commentsNotMigrate = notMigrate.get("Comments");
        final ArrayList<NodeRef> userHomeNotMigrate = notMigrate.get("UserHome");

        if ((sitesNotMigrate != null) && (!sitesNotMigrate.isEmpty())){
            templateArgs.put("sitesNotMigrate", sitesNotMigrate);
        }
        if ((groupsNotMigrate!= null) && (!sitesNotMigrate.isEmpty())){
            templateArgs.put("groupsNotMigrate", groupsNotMigrate);
        }
        if (((contentNotMigrate) != null) && (!sitesNotMigrate.isEmpty())){
            templateArgs.put("contentNotMigrate", contentNotMigrate);
        }
        if ((foldersNotMigrate!= null) && (!sitesNotMigrate.isEmpty())){
            templateArgs.put("foldersNotMigrate", foldersNotMigrate);
        }
        if ((commentsNotMigrate!= null) && (!sitesNotMigrate.isEmpty())){
            templateArgs.put("commentsNotMigrate", commentsNotMigrate);
        }
        if ((userHomeNotMigrate!= null) && (!sitesNotMigrate.isEmpty())){
            templateArgs.put("userHomeNotMigrate", userHomeNotMigrate);
        }

        return templateArgs;
    }
}