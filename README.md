nuxeo-scenari
=============

## Content of this repository

This project is a Nuxeo Platform plugin that is used to connect Scenari and Nuxeo with the PushBack method.

This project contains 2 modules :

 - nuxeo-scenari-jaxrs-connector : *the main java plugin for Nuxeo Platform that contains the differents end points to allow Scenari to push documents on the Nuxeo server.*
 - nuxeo-scenari-package : *a Nuxeo Marketplace that contains the needed bundles and their dependencies*

## How to build your own Marketplace package

    $ mvn clean install -Pmarketplace

Then you'll have a built package under nuxeo-scenari-package/target that could be installed on any Nuxeo 5.6 server. For more informations you can read the page about [Marketplace package](http://doc.nuxeo.com/x/q4RH) on [Nuxeo documentation](http://doc.nuxeo.com).

## Endpoints description

### PushPack manifest:

    GET http://localhost:8080/nuxeo/site/
    GET http://localhost:8080/nuxeo/site/marketplace
    
### Upload an archive, authentification is needed:

	POST http://localhost:8080/nuxeo/site/upload
	
The file is uploaded into Nuxeo, and the response contains an url to the form to edit the archive. The response looks like:

    HTTP/1.1 201 Created
    Server: Apache-Coyote/1.1
    Location: http://localhost:8080/nuxeo/site/scenari/importscreen/default/{UID}
    Content-Length: 0
    
Then, the form alow you to override or to move the file in any writable Workspace. Submitting the form answered the permalink of the file:

    HTTP/1.1 201 Created
    Server: Apache-Coyote/1.1
    Location: http://localhost:8080/nxdoc/default/{UID}/view_documents
    Content-Length: 0
    
## Usefull scripts

There is some usefull scripts under *script* folder to simulate user action, and test http responses.

**01_upload_file.sh**: Upload an archive inside Nuxeo

    $ script/01_upload_file.sh test.zip

**02_display_form.sh**: After getting the uploaded archive UID, assuming it's *318fbc10-14c7-4923-a173-121f45b05265*:

    $ script/02_display_form.sh 318fbc10-14c7-4923-a173-121f45b05265

**03_move_archive.sh**: Submiting the form to the expected workspace, assuming the workspace UID is *588dbe57-7bc4-42b1-8303-3f1b616cb7c0*

    $ script/03_move_archive.sh 318fbc10-14c7-4923-a173-121f45b05265 588dbe57-7bc4-42b1-8303-3f1b616cb7c0