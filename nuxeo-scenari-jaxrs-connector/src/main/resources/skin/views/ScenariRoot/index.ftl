<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE pushPack [
<!ELEMENT pushPack (authentication, content, protocol)>
<!ELEMENT authentication (httpBasicAuthentication | cas | openID)>
<!ELEMENT httpBasicAuthentication EMPTY>
<!ATTLIST httpBasicAuthentication url  CDATA #REQUIRED>
<!ELEMENT content ((scorm12 | scorm2004 | polyDoc | pdf | openDoc)+)>
<!ELEMENT scorm12 EMPTY>
<!ELEMENT scorm2004 EMPTY>
<!ELEMENT polyDoc EMPTY>
<!ELEMENT pdf EMPTY>
<!ELEMENT openDoc EMPTY>
<!ELEMENT protocol (singleHttpRequest)>
<!ELEMENT singleHttpRequest (negotiation)>
<!ATTLIST singleHttpRequest method CDATA #REQUIRED
        mimeType    CDATA #REQUIRED
        url         CDATA #REQUIRED>
<!ELEMENT negotiation (frameWeb|httpResponseCode)>
<!ELEMENT frameWeb EMPTY>
<!ATTLIST frameWeb url  CDATA #REQUIRED>
<!ELEMENT httpResponseCode EMPTY>
]>
<pushPack>
    <authentication>
        <httpBasicAuthentication url="${This.moduleURL}"/>
    </authentication>
    <content>
        <polyDoc />
    </content>
    <protocol>
        <singleHttpRequest method="POST" mimeType="zib/binaries" url="${This.zipUploadUrl}">
            <negotiation>
                <httpResponseCode />
            </negotiation>
        </singleHttpRequest>
    </protocol>
</pushPack>
