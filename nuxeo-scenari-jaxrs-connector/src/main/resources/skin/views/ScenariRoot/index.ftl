<?xml version='1.0' encoding='UTF-8'?>
<cid:cid xmlns:cid='http://www.kelis.fr/cid/v1/core'>
  <cid:authentication>
    <cid:basicHttp testUrl="${This.moduleURL}"/>
  </cid:authentication>
  <cid:content>
    <cid:simpleContent mymetype='*/*'/>
  </cid:content>
  <cid:protocol>
    <cid:singleHttpRequest method='POST' multipartField='upload' url='${This.zipUploadUrl}'>
      <cid:negotiation>
        <cid:frameweb/>
      </cid:negotiation>
    </cid:singleHttpRequest>
  </cid:protocol>
</cid:cid>
