package com.ixxus.ipm.migration.users;

import java.util.ArrayList;
import java.util.List;

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
public class MigrateServiceSites implements MigrateService{


    //Static properties
    public static final String KEY_ERROR_SITES = "Sites";

    private static Log logger = LogFactory.getLog(MigrateServiceSites.class);

    @Inject
    private AuthorityService authorityService;

    @Inject
    private SiteService siteService;

    private final List<NodeRef> notMigrate = new ArrayList<>();

    @SuppressWarnings("unchecked")
    @Override
    public ArrayList<NodeRef> getNotMigrate() {
        return (ArrayList<NodeRef>) notMigrate;
    }

    @Override
    public void migrate(final String olduser, final String newuser) {
        migrateSites(olduser, newuser);
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
    }
}