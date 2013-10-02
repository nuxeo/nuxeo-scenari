<@extends src="base.ftl">
  <@block name="header">You signed in as ${Context.principal}</@block>

  <@block name="header_scripts">
  <script language="JavaScript">
    function disableReplace() {
      var replaceScar = document.getElementById("replaceScar");
      document.getElementById("workspaceRef").disabled = replaceScar.checked;
      document.getElementById("scarRef").disabled = !replaceScar.checked;
    }

    function disablePublish() {
      var publish = document.getElementById("createWorkflow");
      document.getElementById("workflow").disabled = !publish.checked;
      document.getElementById("publish").disabled = !publish.checked;
    }
  </script>
  </@block>

  <@block name="content">

  <div style="margin: 10px">
    <form method="POST">
      <#if sameScars?? >
        <p>
          Il existe des archives semblables. Souhaitez-vous en mettre à jour ?
        </p>
        <p>
          <input id="replaceScar" type="checkbox" name="replaceScar" checked="checked" onchange="disableReplace()"/>
          <label for="replaceScar">Remplacer l'archive existante ?</label>
        </p>
        <select id="scarRef" name="scarRef">
          <#list sameScars as scar>
            <option value="${scar.id}">${scar.parent.path}/${scar.title}</option>
          </#list>
        </select>
      </#if>
      <p>
        Archive .ZIP chargée avec succés. Veuillez choisir un workspace:
      </p>
      <select id="workspaceRef" name="workspaceRef"<#if sameScars??> disabled="disabled"</#if>>
      <#list workspaces as workspace>
        <option value="${workspace.id}">${workspace.path}/${workspace.title}</option>
      </#list>
      </select>
      <p>Workflow de publication:</p>
      <p>
        <input id="createWorkflow" type="checkbox" name="createWorkflow" onchange="disablePublish()"/>
        <label for="createWorkflow">Créer un nouveau workflow de publication ?</label>
      </p>
      <select id="workflow" name="workflowActionId" disabled="disabled">
        <#list metadataTypes as metadataType>
          <option value="${metadataType.id}">${metadataType.label}</option>
        </#list>
      </select>
      <hr />
      <div>
        <input id="submitForm" type="submit" value="Valider" />
      </div>
    </form>
  </div>


  </@block>
</@extends>
