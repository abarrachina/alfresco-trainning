/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * ConsoleApplication tool component.
 * 
 * @namespace Alfresco
 * @class Alfresco.ConsoleApplication
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
       Event = YAHOO.util.Event,
       Element = YAHOO.util.Element;
   
   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;

   /**
    * ConsoleApplication constructor.
    * 
    * @param {String} htmlId The HTML id �of the parent element
    * @return {Alfresco.ConsoleApplication} The new ConsoleApplication instance
    * @constructor
    */
   Alfresco.ConsoleUsers = function(htmlId)
   {
      this.name = "Alfresco.ConsoleUsers";
      Alfresco.ConsoleUsers.superclass.constructor.call(this, htmlId);
      
      /* Register this component */
      Alfresco.util.ComponentManager.register(this);
      
      /* Load YUI Components */
      Alfresco.util.YUILoaderHelper.require(["button", "container", "json", "history"], this.onComponentsLoaded, this);
      
      /* Define panel handlers */
      var parent = this;
      
      // NOTE: the panel registered first is considered the "default" view and is displayed first
      
      /* Options Panel Handler */
      OptionsPanelHandler = function OptionsPanelHandler_constructor()
      {
         OptionsPanelHandler.superclass.constructor.call(this, "options");
      };
      
      YAHOO.extend(OptionsPanelHandler, Alfresco.ConsolePanelHandler,
      {
         /**
          * Called by the ConsolePanelHandler when this panel shall be loaded
          *
          * @method onLoad
          */
         onLoad: function onLoad()
         {
            // Buttons
            parent.widgets.applyButton = Alfresco.util.createYUIButton(parent, "updateuser-save-button", function(){
            	
            	var olduser, newuser, me, sites, groups, content, comments, userhome, datauser, likes, favorites, workflows;
            	me = this;
            	var fnGetter = function(id)
                {
                   return Dom.get(me.id + id).value;
                };
                
                var fnCheck = function(id)
                {
                   if (Dom.get(me.id + id).checked)
                	   return Dom.get(me.id + id).value;
                   return 0;
                };
                
                olduser = fnGetter("-create-olduser");
                newuser = fnGetter("-create-newuser");                
                sites = fnCheck("-items-sites");
                groups = fnCheck("-items-groups");
                content = fnCheck("-items-content");
                comments = fnCheck("-items-comments");
                userhome = fnCheck("-items-userhome");
                datauser = fnCheck("-items-datauser");
                likes = fnCheck("-items-likes");
                favorites = fnCheck("-items-favorites");
                workflows = fnCheck("-items-workflows");                
                var getValues = "newuser="+encodeURIComponent(newuser)+"&olduser="+encodeURIComponent(olduser)+"&sites="+sites+"&groups="+groups+"&content="+content+"&userhome="+userhome+"&datauser="+datauser+"&likes="+likes+"&favorites="+favorites+"&workflows="+workflows; 
                
            	 Alfresco.util.Ajax.request(
	            {
	            	//?newuser={newuser}&amp;olduser={olduser}&amp;sites={sites}&amp;groups={groups}&amp;content={content}&amp;comments={comments}&amp;userhome={userhome}&amp;datauser={datauser}&amp;likes={likes}&amp;favorites={favorites}&amp;workflows={workflows}
	               url: Alfresco.constants.PROXY_URI + "api/migrateuser/?"+getValues ,
	               method: Alfresco.util.Ajax.POST, 
	               successCallback:
	               {
	                  fn:  parent.onSuccess,
	                  scope: parent
	               },
	               failureMessage: ""
	            });
            });
            
            parent.widgets.selectUserButton = Alfresco.util.createYUIButton(parent, "create-newuser-button", function(){
            	
            	 this.modules.searchPeopleFinder.clearResults();            	 
                 this.widgets.addUserPanel.show();
            });
              // Form definition
//            var form = new Alfresco.forms.Form(parent.id + "-options-form");
//            form.setSubmitElements([parent.widgets.applyButton]);
//            form.setSubmitAsJSON(true);
//            form.setAJAXSubmit(true,
//            {
//               successCallback:
//               {
//                  fn: this.onSuccess,
//                  scope: this
//               }
//            });
//            form.init();
            
            // Load in the People Finder component from the server
            Alfresco.util.Ajax.request(
            {
               url: Alfresco.constants.URL_SERVICECONTEXT + "components/people-finder/people-finder",
               dataObj:
               {
                  htmlid: parent.id + "-search-peoplefinder"
               },
               successCallback:
               {
                  fn: this.onPeopleFinderLoaded,
                  scope: parent
               },
               failureMessage: "Could not load People Finder component",
               execScripts: true
            });
            
            
         },
         
         /**
          * Called when the user has selected a person from the add user dialog.
          *
          * @method onPersonSelected
          * @param e DomEvent
          * @param args Event parameters (depends on event type)
          */
         onPersonSelected: function ConsoleGroups_SearchPanelHandler_onPersonSelected(e, args)
         {
            // This is a "global" event so we ensure the event is for the current panel by checking panel visibility.
        	 console.log("Persona seleccionada");
            if (this._visible)
            {
               var name = args[1].firstName + " " + args[1].lastName;
               this.widgets.addUserPanel.hide();
               this._addToGroup(
                     args[1].userName,
                     this._selectedParentGroupShortName,
                     parent._msg("message.adduser-success", name),
                     parent._msg("message.adduser-failure", name));
            }
         },
         
         /**
          * Called when the user clicks the add user icon in the column browser header
          *
          * @method onAddUserClick
          * @param columnInfo
          */
         onAddUserClick: function ConsoleGroups_SearchPanelHandler_onAddUserClick(columnInfo)
         {
            this._selectedParentGroupShortName = columnInfo.parent.shortName;
            this.modules.searchPeopleFinder.clearResults();
            this.widgets.addUserPanel.show();
         },
         
         /**
          * Called when the people finder template has been loaded.
          * Creates a dialog and inserts the people finder for choosing users to add.
          *
          * @method onPeopleFinderLoaded
          * @param response The server response
          */
         onPeopleFinderLoaded: function ConsoleGroups_SearchPanelHandler_onPeopleFinderLoaded(response)
         {
        	 
            // Inject the component from the XHR request into it's placeholder DIV element
            var finderDiv = Dom.get(parent.id + "-search-peoplefinder");
            finderDiv.innerHTML = response.serverResponse.responseText;
            // Create the Add User dialog
            this.widgets.addUserPanel = Alfresco.util.createYUIPanel(parent.id + "-peoplepicker");
            // Find the People Finder by container ID
            this.modules.searchPeopleFinder = Alfresco.util.ComponentManager.get(parent.id + "-search-peoplefinder");

            // Set the correct options for our use
            this.modules.searchPeopleFinder.setOptions(
            {
               singleSelectMode: true
            });
            
            // Make sure we listen for events when the user selects a person
            YAHOO.Bubbling.on("personSelected", function(e,args){            	
                    
                    console.log(args[1].userName);
                    this.widgets.addUserPanel.hide();
                    var fnSetter = function(id, val)
                    {
                       Dom.get(parent.id + id).value = val ? $html(val) : "";
                    };
                    fnSetter("-create-newuser", args[1].userName);
                
            }, this);
         },
         
         /**
          * Successfully applied options event handler
          *
          * @method onSuccess
          * @param response {object} Server response object
          */
         onSuccess: function OptionsPanel_onSuccess(response)
         {
            if (response && response.json)
            {
               if (response.json.success)
               {
                  // refresh the browser to force the themed components to reload
                  window.location.reload(true);
               }
               else if (response.json.message)
               {
                  Alfresco.util.PopupManager.displayPrompt(
                  {
                     text: response.json.message
                  });
               }
            }
            else
            {
               Alfresco.util.PopupManager.displayPrompt(
               {
                  text: Alfresco.util.message("message.failure")
               });
            }
         },
         
         /**
          * Upload button click handler
          *
          * @method onUpload
          * @param e {object} DomEvent
          * @param p_obj {object} Object passed back from addListener method
          */
         onUpload: function OptionsPanel_onUpload(e, p_obj)
         {
            
            Event.preventDefault(e);
         },
         
         /**
          * Reset button click handler
          *
          * @method onReset
          * @param e {object} DomEvent
          * @param p_obj {object} Object passed back from addListener method
          */
         onReset: function OptionsPanel_onReset(e, p_obj)
         {
            
         }
      });
      new OptionsPanelHandler();
      
      return this;
   };
   
   YAHOO.extend(Alfresco.ConsoleUsers, Alfresco.ConsoleTool,
   {
      
   });
})();