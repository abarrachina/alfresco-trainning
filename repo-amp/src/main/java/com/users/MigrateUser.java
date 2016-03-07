/*
Licensed to the Apache Software Foundation (ASF) under one or more
contributor license agreements.  See the NOTICE file distributed with
this work for additional information regarding copyright ownership.
The ASF licenses this file to You under the Apache License, Version 2.0
(the "License"); you may not use this file except in compliance with
the License.  You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.users;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.repo.security.authority.UnknownAuthorityException;
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
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;

/**
 * Java Migrate User Web Script.
 *
 * @author nazareth.jimenez@ixxus.com
 */
public class MigrateUser extends DeclarativeWebScript {

    private static Log logger = LogFactory.getLog(MigrateUser.class);

    private PersonService personService;
    private AuthorityService authorityService;
    private SiteService siteService;
    private NodeService nodeService;
    private SearchService searchService;
    private BehaviourFilter policyBehaviourFilter;

    public void setPersonService(final PersonService  personService) {
        this. personService =  personService;
    }

    public void setAuthorityService(final AuthorityService  authorityService) {
        this. authorityService =  authorityService;
    }

    public void setSiteService(final SiteService  siteService) {
        this. siteService =  siteService;
    }

    public void setPolicyBehaviourFilter(final BehaviourFilter policyBehaviourFilter) {
        this.policyBehaviourFilter = policyBehaviourFilter;
    }

    public void setNodeService(final NodeService  nodeService) {
        this. nodeService =  nodeService;
    }

    public void setSearchService(final SearchService  searchService) {
        this. searchService =  searchService;
    }

    @Override
    protected Map<String, Object> executeImpl(final WebScriptRequest req, final Status status, final Cache cache) {
        final Map<String, Object> model = new HashMap<String, Object>();
        model.put("code", 0);

        final String newuser = req.getParameter("newuser");
        final String olduser = req.getParameter("olduser");
        final String sites = req.getParameter("sites");
        final String groups = req.getParameter("groups");
        final String content = req.getParameter("content");
        final String comments = req.getParameter("comments");
        final String userhome = req.getParameter("userhome");
        final String datauser = req.getParameter("datauser");
        final String likes = req.getParameter("likes");
        final String favorites = req.getParameter("favorites");
        final String workflows = req.getParameter("workflows");

        if((newuser == null) || (olduser == null)){
            throw new WebScriptException("Missing mandatory params");
        }

        if (personService.personExists(newuser)) {
            if (sites.equalsIgnoreCase("true")){
                migrateSites(olduser, newuser);
            }
            if (groups.equalsIgnoreCase("true")){
                migrateGroups(olduser, newuser);
            }
            if (content.equalsIgnoreCase("true")){
                migrateContent(olduser, newuser);
                migrateFolder(olduser, newuser);
            }
        }

        return model;
    }

    /***
     * Migrate Site Roles from olduser to newuser
     *
     * @param olduser
     * @param newuser
     */
    private void migrateSites (final String olduser, final String newuser){

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

    /***
     * Migrate Groups from olduser to newuser
     *
     * @param olduser
     * @param newuser
     */
    private void migrateGroups (final String olduser, final String newuser){
        final Set<String> groups = authorityService.getAuthoritiesForUser(olduser);

        for (final String group:groups){
            if (!authorityService.getAuthoritiesForUser(newuser).contains(group)){
                authorityService.addAuthority(group, newuser);
            }
        }
    }

    /***
     * Migrate Content from olduser to newuser
     *
     * @param olduser
     * @param newuser
     */
    private void migrateContent (final String olduser, final String newuser){

        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}content\"";
        strQuery += " AND ";
        strQuery += "@cm\\:creator:\"" + olduser + "\"";
        changeCreator(strQuery, newuser);
    }

    /***
     * Migrate Folder from olduser to newuser
     *
     * @param olduser
     * @param newuser
     */
    private void migrateFolder (final String olduser, final String newuser){

        String strQuery="TYPE:\"{http://www.alfresco.org/model/content/1.0}folder\"";
        strQuery += " AND ";
        strQuery += "@cm\\:creator:\"" + olduser + "\"";
        changeCreator(strQuery, newuser);

    }

    /***
     * Change noderef's creator
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
                // Disable auditable aspect to allow change properties of cm:auditable aspect
                policyBehaviourFilter.disableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);

                // Update properties of cm:auditable aspect
                nodeService.setProperty(nodeRef, ContentModel.PROP_CREATOR, newuser);
                nodeService.setProperty(nodeRef, ContentModel.PROP_MODIFIER, newuser);

                // Enable auditable aspect
                policyBehaviourFilter.enableBehaviour(nodeRef, ContentModel.ASPECT_AUDITABLE);

            }
        }
        finally{
            if(results != null)
            {
                results.close();
            }
        }
    }
}