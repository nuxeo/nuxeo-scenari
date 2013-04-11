<@extends src="base.ftl">
  <@block name="header">You signed in as ${Context.principal}</@block>

  <@block name="content">

  <div style="margin: 10px">
    <form method="POST">
      <p>
        Archive .ZIP chargée avec succés. Veuillez choisir un workspace:
      </p>
      <select name="workspaceRef">
      <#list workspaces as workspace>
        <option value="${workspace.id}">${workspace.title}</option>
      </#list>
      </select>
      <input type="submit" value="Valider" />
    </form>
  </div>


  </@block>
</@extends>
