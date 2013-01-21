package eu.scenari.automation;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import org.nuxeo.ecm.automation.core.annotations.Context;
import org.nuxeo.ecm.automation.core.annotations.Operation;
import org.nuxeo.ecm.automation.core.annotations.OperationMethod;
import org.nuxeo.ecm.automation.core.annotations.Param;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.blob.FileBlob;
import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
import org.nuxeo.ecm.core.api.model.PropertyException;
import org.nuxeo.ecm.platform.userworkspace.api.UserWorkspaceService;
import org.nuxeo.runtime.api.Framework;
import org.nuxeo.runtime.services.streaming.FileSource;
import org.nuxeo.runtime.services.streaming.StreamSource;

@Operation(id = PublishFolder.ID, category = "Scenari", label = "PublishFolder", description = "Publish a Scenari Document in Nuxeo")
public class PublishFolder {

	public static final String ID = "Scenari.PublishFolder";

	@Context
	protected CoreSession session;

	@Param(name = "file", required = false)
	protected Blob file;

	@Param(name = "parentID", required = false)
	protected String parentID;

	@Param(name = "parentPath", required = false)
	protected String parentPath;

	@Param(name = "documentID", required = false)
	protected String documentID;

	@OperationMethod
	public DocumentModel publishFolder(Blob blob) throws Exception {
		if (parentID != null) {
			DocumentModel parent = session.getDocument(new IdRef(parentID));
			parentPath = parent.getPathAsString();
		} else {
			if (parentPath == null) {
				UserWorkspaceService uwService = Framework
						.getLocalService(UserWorkspaceService.class);
				DocumentModel personalWorkspace = uwService
						.getCurrentUserPersonalWorkspace(session, null);
				parentPath = personalWorkspace.getPathAsString();
			}
		}
		DocumentModel doc;
		IdRef docRef = null;
		if (documentID != null) {
			docRef = new IdRef(documentID);
		}
		if (docRef == null || !session.exists(docRef)) {
			doc = session.createDocumentModel(parentPath, "scenari", "File");
			setProperties(blob, doc);
			doc = session.createDocument(doc);
		} else {
			doc = session.getDocument(new IdRef(documentID));
			setProperties(blob, doc);
			doc = session.saveDocument(doc);
		}

		return doc;
	}

	protected void setProperties(Blob blob, DocumentModel doc)
			throws PropertyException, ClientException, IOException {

		File file = ((FileBlob) blob.persist()).getFile();
		ZipFile zf = new ZipFile(file);
		Enumeration<? extends ZipEntry> entries = zf.entries();
		List<Map<String, Object>> blobs = new ArrayList<Map<String, Object>>();
		while (entries.hasMoreElements()) {
			ZipEntry nextEntry = entries.nextElement();
			String blobName = nextEntry.getName();
			if (blobName.endsWith(".scar")) {
				StreamingBlob scarBlob = StreamingBlob.createFromStream(
						zf.getInputStream(nextEntry), "application/scenari");
				scarBlob.setFilename(blobName);
				// TODO Use XML file to feed dc title...
				String title = blobName.substring(0,blobName.length() - 5);
				doc.setPropertyValue("dc:title", title);
				doc.setPropertyValue("file:content", (Serializable) scarBlob);
			} else {
				Blob subFileBlob = StreamingBlob.createFromStream(zf
						.getInputStream(nextEntry));
				subFileBlob.setFilename(blobName);
				Map<String, Object> map = new HashMap<String, Object>();
				map.put("file", subFileBlob);
				map.put("filename", blobName);
				blobs.add(map);
			}
		}
		doc.setPropertyValue("files:files", (Serializable) blobs.toArray());

	}

	@OperationMethod
	public DocumentModel publishFolder() throws Exception {
		return publishFolder(file);
	}

}
