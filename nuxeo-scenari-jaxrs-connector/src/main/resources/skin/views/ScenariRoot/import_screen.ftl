<@extends src="base.ftl">
  <@block name="header">You signed in as ${Context.principal}</@block>

  <@block name="header_scripts">
  <script language="JavaScript">
    function disableSelect() {
      var replaceScar = document.getElementById("replaceScar");
      document.getElementById("workspaceRef").disabled = replaceScar.checked;
      document.getElementById("scarRef").disabled = !replaceScar.checked;
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
          <input id="replaceScar" type="checkbox" name="replaceScar" checked="checked" onchange="disableSelect()"/>
          <label for="replaceScar">Remplacer l'archive existante?</label>
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
        <option value="${workspace.id}">${workspace.title}</option>
      </#list>
      </select>
      <div>
        <input type="submit" value="Valider" />
      </div>
    </form>
  </div>


  </@block>
</@extends>
