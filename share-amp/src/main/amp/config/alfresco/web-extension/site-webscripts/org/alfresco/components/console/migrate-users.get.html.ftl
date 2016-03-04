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
      <div id="${el}-body" class="migrateusers">
      	 <div id="${el}-migrate">
            <div class="yui-g">
               <div class="yui-u first">
                  <div class="title"><label for="${el}-title-text">${msg("label.title-migrate")}</label></div>
               </div>
       		</div>
       	</div>
      </div>
   </@>
</@>

