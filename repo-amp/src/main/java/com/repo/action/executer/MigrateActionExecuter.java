package com.repo.action.executer;


import java.io.Serializable;
import java.util.List;

import org.alfresco.repo.action.ParameterDefinitionImpl;
import org.alfresco.repo.action.executer.ActionExecuterAbstractBase;
import org.alfresco.service.cmr.action.Action;
import org.alfresco.service.cmr.action.ParameterDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

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

    private MigrateService migrateServiceImpl;

    public void setMigrateService(final MigrateService migrateServiceImpl) {
        this.migrateServiceImpl = migrateServiceImpl;
    }

    private PersonService personService;

    public void setPersonService(final PersonService  personService) {
        this. personService =  personService;
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

    }
}