package eu.scenari.jaxrs.utils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;

/**
 * 
 * 
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 5.7
 */
public class ZipExploder {

    protected ZipFile zip;

    protected Blob scarBlob;

    protected boolean exploded = false;

    protected final List<Map<String, Object>> otherBlobs = new ArrayList<Map<String, Object>>();

    private static final Log log = LogFactory.getLog(ZipExploder.class);

    public ZipExploder(Blob blob) throws IOException {
        try {
            File tmp = File.createTempFile("zip", "entry");
            blob.transferTo(tmp);

            this.zip = new ZipFile(tmp);
        } catch (ZipException e) {
            log.info("Blob is not a correctly unzipped: " + e.getMessage());
            log.debug(e);
        }
    }

    protected void explode() throws ClientException {
        // If blob wasn't a zip file or an error occured, do not try to explode
        // it as the blob will be stored to the new document
        if (zip == null || exploded) {
            return;
        }

        try {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry nextEntry = entries.nextElement();
                String blobName = nextEntry.getName();
                if (blobName.endsWith(".scar")) {
                    scarBlob = StreamingBlob.createFromStream(
                            zip.getInputStream(nextEntry),
                            "application/scenari");
                    scarBlob.setFilename(blobName);
                } else {
                    Blob subFileBlob = StreamingBlob.createFromStream(zip.getInputStream(nextEntry));
                    subFileBlob.setFilename(blobName);
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("file", subFileBlob);
                    map.put("filename", blobName);
                    otherBlobs.add(map);
                }
            }

            exploded = true;
        } catch (IOException e) {
            throw ClientException.wrap(e);
        }
    }

    public Blob findScar() throws ClientException {
        if (scarBlob == null) {
            explode();
        }
        return scarBlob;
    }

    public DocumentModel updateDocumentModel(DocumentModel parentLessDoc,
            DocumentModel targetDoc) throws ClientException {
        targetDoc.copyContent(parentLessDoc);
        if (zip == null) {
            return targetDoc;
        }
        explode();

        // if a scar file is found; put it as main blob
        if (scarBlob != null) {
            targetDoc.setPropertyValue("dc:title",
                    prepareScarFilename(scarBlob.getFilename()));
            targetDoc.setPropertyValue("file:content", (Serializable) scarBlob);
        }
        targetDoc.setPropertyValue("files:files", otherBlobs.toArray());

        return targetDoc;
    }

    public DocumentModel createDocumentModel(DocumentModel parentLessDoc,
            DocumentModel parentDocument) throws ClientException {
        CoreSession session = parentLessDoc.getCoreSession();

        DocumentModel doc = session.createDocumentModel(
                parentDocument.getPathAsString(), null, parentLessDoc.getType());
        return updateDocumentModel(parentLessDoc, doc);
    }

    public static String prepareScarFilename(String filename) {
        return filename.substring(0, filename.length() - 5);
    }
}
