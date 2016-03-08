<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link href="${url.context}/res/components/console/migrateusers.css" group="console"/>   
</@>

<@markup id="js" >
   <#-- JavaScript Dependencies -->
   <@script src="${url.context}/res/components/console/consoletool.js" group="console"/>
   <@script src="${url.context}/res/components/console/migrate-users.js" group="console"/>   
</@>

<@markup id="widgets">
   <@createWidgets group="console"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <!--[if IE]>
      <iframe id="yui-history-iframe" src="${url.context}/res/yui/history/assets/blank.html"></iframe> 
      <![endif]-->
      <input id="yui-history-field" type="hidden" />
      
      <#assign el=args.htmlid?html>
      <div id="${el}-body" class="users">
      
      	<form  id="${el}-options-form" action="${url.service}" method="get">
         <!-- Search panel -->
         <div id="${el}-search" class="hidden">
            <div class="yui-g">
               <div class="yui-u first">
                  <div class="title"><label for="${el}-search-text">${msg("label.title")}</label></div>
               </div>
               <div class="yui-u align-right">                  
               </div>
            </div>           
         </div>
         
         <div class="header-bar">${msg("label.usernames")}</div>
         <div id="${el}-view-main" class="">
     		 <div class="field-row">
                  <span class="crud-label">${msg("label.olduser")}:&nbsp;*</span>
             </div>
             <div class="field-row">
                <input class="crud-input" id="${el}-create-olduser" type="text" maxlength="256" />
             </div>
         </div>
         
         <div id="${el}-view-main" class="">
     		 <div class="field-row">
                  <span class="crud-label">${msg("label.newuser")}:&nbsp;*</span>
             </div>
             <div class="field-row">
                <input class="crud-input" id="${el}-create-newuser" type="text" maxlength="256" />
             </div>
         </div>
         
         <div class="header-bar">${msg("label.itemstomigrate")}</div>
         
         <div class="field-row">
         	<input type="checkbox" class="crud-input" id="${el}-items-sites" type="text" maxlength="256" />
         	<label class="crud-label" for="${el}-items-sites">${msg("label.items.sites")}</label>
         </div>
         
         <div class="field-row">
 			<input type="checkbox" class="crud-input" id="${el}-items-groups" type="text" maxlength="256" />
         	<label class="crud-label" for="${el}-items-groups">${msg("label.items.groups")}</label>
         </div>
         
         <div class="field-row">
 			<input type="checkbox" class="crud-input" id="${el}-items-content" type="text" maxlength="256" />
         	<label class="crud-label" for="${el}-items-content">${msg("label.items.content")}</label>
         </div>
         
         <div class="field-row">
 			<input type="checkbox" class="crud-input" id="${el}-items-comments" type="text" maxlength="256" />
         	<label class="crud-label" for="${el}-items-comments">${msg("label.items.comments")}</label>
         </div>
         
         <div class="field-row">
 			<input type="checkbox" class="crud-input" id="${el}-items-userhome" type="text" maxlength="256" />
         	<label class="crud-label" for="${el}-items-userhome">${msg("label.items.userhome")}</label>
         </div>
         
         <div class="field-row">
 			<input type="checkbox" class="crud-input" id="${el}-items-datauser" type="text" maxlength="256" />
         	<label class="crud-label" for="${el}-items-datauser">${msg("label.items.datauser")}</label>
         </div>
         
         <div class="field-row">
 			<input type="checkbox" class="crud-input" id="${el}-items-likes" type="text" maxlength="256" />
         	<label class="crud-label" for="${el}-items-likes">${msg("label.items.likes")}</label>
         </div>
         
         <div class="field-row">
 			<input type="checkbox" class="crud-input" id="${el}-items-favorites" type="text" maxlength="256" />
         	<label class="crud-label" for="${el}-items-favorites">${msg("label.items.favorites")}</label>
         </div>
         
         <div class="field-row">
 			<input type="checkbox" class="crud-input" id="${el}-items-workflows" type="text" maxlength="256" />
         	<label class="crud-label" for="${el}-items-workflows">${msg("label.items.workflows")}</label>
         </div>
         
         <div>
               <div class="updateuser-save-button left">
                  <span class="yui-button yui-push-button" id="${el}-updateuser-save-button">
                     <span class="first-child"><button>${msg("button.save")}</button></span>
                  </span>
               </div>              
            </div>
         </form>
      </div>
   </@>
</@>

