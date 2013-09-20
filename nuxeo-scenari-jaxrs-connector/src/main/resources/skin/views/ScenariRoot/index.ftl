<?xml version="1.0" encoding="UTF-8"?>
<cid:manifest xmlns:cid="http://www.kelis.fr/cid/v1/core">
  <cid:authentication>
    <cid:basicHttp testUrl="${This.moduleURL}"/>
  </cid:authentication>
  <cid:content>
    <scPolyDoc xmlns="http://www.scenari-platform.org/cid/v1/derivation" />
  </cid:content>
  <cid:transport>
    <cid:singleHttpRequest url="${This.zipUploadUrl}">
      <cid:post multipartFileField="upload"
        multipartTypeField="contentType" />
      <cid:negociation>
        <cid:frameWeb/>
      </cid:negociation>
    </cid:singleHttpRequest>
  </cid:transport>
</cid:manifest>
