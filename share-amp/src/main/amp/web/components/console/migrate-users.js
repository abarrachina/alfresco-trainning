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
                
                olduser = fnGetter("-create-olduser");
                newuser = fnGetter("-create-newuser");                
                sites = fnGetter("-items-sites");
                groups = fnGetter("-items-groups");
                content = fnGetter("-items-content");
                comments = fnGetter("-items-comments");
                userhome = fnGetter("-items-userhome");
                datauser = fnGetter("-items-datauser");
                likes = fnGetter("-items-likes");
                favorites = fnGetter("-items-favorites");
                workflows = fnGetter("-items-workflows");                
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
            	 console.log("bbb");
            });
            
//            // Form definition
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