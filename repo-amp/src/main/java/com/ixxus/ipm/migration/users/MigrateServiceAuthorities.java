package com.ixxus.ipm.migration.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;

import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.site.SiteInfo;
import org.alfresco.service.cmr.site.SiteService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;


/***
 *
 * @author nazareth.jimenez@ixxus.com
 * Migrate Service Authorities
 *
 */
@Service
public class MigrateServiceAuthorities implements MigrateService{


    //Static properties
    public static final String KEY_ERROR_SITES = "Sites";
    public static final String KEY_ERROR_GROUPS = "Groups";

    private static Log logger = LogFactory.getLog(MigrateServiceAuthorities.class);

    @Inject
    private AuthorityService authorityService;

    @Inject
    private SiteService siteService;

    private final Map<String, ArrayList<NodeRef>> notMigrate = new HashMap<>();

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, ArrayList<NodeRef>> getNotMigrate() {
        return notMigrate;
    }

    @Override
    public void migrate(final String olduser, final String newuser) {
        migrateSites(olduser, newuser);
        migrateGroups(olduser, newuser);

    }

    private void migrateSites(final String olduser, final String newuser) {
        String authority = "";
        final List<SiteInfo> sites = siteService.listSites(olduser);
        final List<NodeRef> sitesNotMigrate =  new ArrayList<>();

        for (final SiteInfo site: sites){
            try{
                final String role = siteService.getMembersRole(site.getShortName(), olduser);
                authority = "GROUP_site_"+site.getShortName()+"_"+role;
                if (!authorityService.getAuthoritiesForUser(newuser).contains(authority)){
                    authorityService.addAuthority(authority, newuser);
                }
            }
            catch(final UnknownAuthorityException ex){
                sitesNotMigrate.add(site.getNodeRef());
                logger.error("The authority "+ authority + " not exists " + ex.getMessage(),ex);

            }
        }
        notMigrate.put(KEY_ERROR_SITES, (ArrayList<NodeRef>) sitesNotMigrate);
    }

    private void migrateGroups(final String olduser, final String newuser) {
        final Set<String> groups = authorityService.getAuthoritiesForUser(olduser);
        final List<NodeRef> groupsNotMigrate  = new ArrayList<>();

        for (final String group:groups){
            try{
                if (!authorityService.getAuthoritiesForUser(newuser).contains(group)){
                    authorityService.addAuthority(group, newuser);
                }
            }
            catch(final UnknownAuthorityException ex){
                groupsNotMigrate.add(authorityService.getAuthorityNodeRef(group));
                logger.error("The authority "+ group + " not exists " + ex.getMessage(), ex);
            }
        }
        notMigrate.put(KEY_ERROR_GROUPS, (ArrayList<NodeRef>) groupsNotMigrate);
    }
}