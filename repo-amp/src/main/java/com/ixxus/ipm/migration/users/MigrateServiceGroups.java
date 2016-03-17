package com.ixxus.ipm.migration.users;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;

import org.alfresco.repo.security.authority.UnknownAuthorityException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AuthorityService;
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
public class MigrateServiceGroups implements MigrateService{


    //Static properties
    public static final String KEY_ERROR_GROUPS = "Groups";

    private static Log logger = LogFactory.getLog(MigrateServiceGroups.class);

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
        migrateGroups(olduser, newuser);
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
    }
}