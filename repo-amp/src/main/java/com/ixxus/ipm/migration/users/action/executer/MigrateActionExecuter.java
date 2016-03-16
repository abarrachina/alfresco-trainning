package com.ixxus.ipm.migration.users.action.executer;


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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Value;

import com.ixxus.ipm.migration.users.MigrateService;
import com.ixxus.ipm.migration.users.MigrateServiceImpl;

/***
 *
 * @author nazareth.jimenez@ixxus.com
 * Action Migrate User
 *
 */

public class MigrateActionExecuter extends ActionExecuterAbstractBase
{
    private static Log logger = LogFactory.getLog(MigrateActionExecuter.class);
    public static final String NAME_MIGRATE = "migrate-action";
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

    @Value("${path.email.template}")
    private String pathTemplate;

    private MigrateService migrateServiceImpl;

    public void setMigrateService(final MigrateService migrateServiceImpl) {
        this.migrateServiceImpl = migrateServiceImpl;
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
        final Boolean sites = Boolean.valueOf(paramSites.toString());
        final Boolean groups = Boolean.valueOf(paramGroups.toString());
        final Boolean content = Boolean.valueOf(paramContent.toString());
        final Boolean comments = Boolean.valueOf(paramComment.toString());
        final Boolean userhome = Boolean.valueOf(paramUserHome.toString());
        final Boolean favorites = Boolean.valueOf(paramFavorites.toString());
        final Boolean workflows = Boolean.valueOf(paramWorkflows.toString());

        if (workflows){
            migrateServiceImpl.migrateWorkflows(olduser, newuser);
        }
        if (sites){
            migrateServiceImpl.migrateSites(olduser, newuser);
        }
        if (groups){
            migrateServiceImpl.migrateGroups(olduser, newuser);
        }
        if (content){
            migrateServiceImpl.migrateContent(olduser, newuser);
            migrateServiceImpl.migrateFolder(olduser, newuser);
        }
        if (comments){
            migrateServiceImpl.migrateComments(olduser, newuser);
        }
        if (userhome){
            migrateServiceImpl.migrateUserHome(olduser, newuser);
        }
        if (favorites){
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
        NodeRef template;

        rs = searchService.query(storeRef, SearchService.LANGUAGE_LUCENE, pathTemplate);

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
        final Map<String, Serializable> templateArgs = new HashMap<>();
        final Map<String, ArrayList<NodeRef>> notMigrate = migrateServiceImpl.getNotMigrate();
        final Map<String, ArrayList<String>> taskNoMigrated = migrateServiceImpl.getTaskNoMigrated();
        final ArrayList<NodeRef> sitesNotMigrate = notMigrate.get(MigrateServiceImpl.KEY_ERROR_SITES);
        final ArrayList<NodeRef> groupsNotMigrate = notMigrate.get(MigrateServiceImpl.KEY_ERROR_GROUPS);
        final ArrayList<NodeRef> contentNotMigrate = notMigrate.get(MigrateServiceImpl.KEY_ERROR_CONTENT);
        final ArrayList<NodeRef> foldersNotMigrate = notMigrate.get(MigrateServiceImpl.KEY_ERROR_FOLDERS);
        final ArrayList<NodeRef> commentsNotMigrate = notMigrate.get(MigrateServiceImpl.KEY_ERROR_COMMENTS);
        final ArrayList<NodeRef> userHomeNotMigrate = notMigrate.get(MigrateServiceImpl.KEY_ERROR_USERHOME);
        final ArrayList<String> taskInitiatorNoMigrated = taskNoMigrated.get(MigrateServiceImpl.KEY_ERROR_TASKINITIATOR);
        final ArrayList<NodeRef> taskAsigneeNoMigrated = notMigrate.get(MigrateServiceImpl.KEY_ERROR_TASKASIGNEE);


        if ((sitesNotMigrate != null) && (!sitesNotMigrate.isEmpty())){
            templateArgs.put("sitesNotMigrate", sitesNotMigrate);
        }
        if ((groupsNotMigrate!= null) && (!groupsNotMigrate.isEmpty())){
            templateArgs.put("groupsNotMigrate", groupsNotMigrate);
        }
        if (((contentNotMigrate) != null) && (!contentNotMigrate.isEmpty())){
            templateArgs.put("contentNotMigrate", contentNotMigrate);
        }
        if ((foldersNotMigrate!= null) && (!foldersNotMigrate.isEmpty())){
            templateArgs.put("foldersNotMigrate", foldersNotMigrate);
        }
        if ((commentsNotMigrate!= null) && (!commentsNotMigrate.isEmpty())){
            templateArgs.put("commentsNotMigrate", commentsNotMigrate);
        }
        if ((userHomeNotMigrate!= null) && (!userHomeNotMigrate.isEmpty())){
            templateArgs.put("userHomeNotMigrate", userHomeNotMigrate);
        }
        if ((taskInitiatorNoMigrated!= null) && (!taskInitiatorNoMigrated.isEmpty())){
            templateArgs.put("taskInitiatorNoMigrated", taskInitiatorNoMigrated);
        }
        if ((taskAsigneeNoMigrated!= null) && (!taskAsigneeNoMigrated.isEmpty())){
            templateArgs.put("taskAsigneeNoMigrated", taskAsigneeNoMigrated);
        }

        return templateArgs;
    }
}