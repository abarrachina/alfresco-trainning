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
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.PersonService;
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

        final NodeRef newuserRef;
        NodeRef olduserRef;

        if((newuser == null) || (olduser == null)){
            throw new WebScriptException("Missing mandatory params");
        }

        if (personService.personExists(olduser)) {
            olduserRef = personService.getPerson(olduser);
        }

        return model;
    }
}