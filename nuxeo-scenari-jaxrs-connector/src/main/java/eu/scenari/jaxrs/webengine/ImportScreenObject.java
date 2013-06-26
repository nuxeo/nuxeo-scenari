package eu.scenari.jaxrs.webengine;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentModelList;
import org.nuxeo.ecm.core.api.IdRef;
import org.nuxeo.ecm.core.api.impl.DocumentModelListImpl;
import org.nuxeo.ecm.platform.url.DocumentViewImpl;
import org.nuxeo.ecm.platform.url.api.DocumentViewCodecManager;
import org.nuxeo.ecm.webengine.model.ResourceType;
import org.nuxeo.ecm.webengine.model.WebContext;
import org.nuxeo.ecm.webengine.model.impl.DefaultObject;
import org.nuxeo.runtime.api.Framework;

import eu.scenari.jaxrs.utils.ZipExploder;

import static org.nuxeo.ecm.core.api.VersioningOption.MAJOR;
import static org.nuxeo.ecm.core.api.VersioningOption.MINOR;
import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ_WRITE;
import static org.nuxeo.ecm.core.versioning.VersioningService.VERSIONING_OPTION;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 5.7
 */
public class ImportScreenObject extends DefaultObject {

    private static final Log log = LogFactory.getLog(ImportScreenObject.class);

    final CoreSession session;

    final DocumentModel doc;

    final String QUERY_FIND_SCAR = "SELECT * FROM Document WHERE dc:title ILIKE '%s' AND ecm:isCheckedInVersion = 0 "
            + "AND ecm:currentLifeCycleState != 'deleted'";

    final String QUERY_WORKSPACES = "SELECT * FROM Workspace WHERE ecm:isProxy = 0 AND ecm:isCheckedInVersion = 0 AND"
            + " ecm:currentLifeCycleState != 'deleted'";

    public ImportScreenObject(WebContext ctx, String repository, String docRef)
            throws ClientException {
        ResourceType resType = ctx.getModule().getType("ScenariRoot");
        initialize(ctx, resType);

        session = getContext().getCoreSession();
        doc = session.getDocument(new IdRef(docRef));
    }

    protected static DocumentViewCodecManager getCodecManager() {
        return Framework.getLocalService(DocumentViewCodecManager.class);
    }

    protected ZipExploder getZipExplorer() throws ClientException, IOException {
        return new ZipExploder((Blob) doc.getPropertyValue("file:content"));
    }

    protected DocumentModelList filterOnPermission(DocumentModelList docs,
            String permission) throws ClientException {
        DocumentModelList filtered = new DocumentModelListImpl();
        for (DocumentModel doc : docs) {
            if (session.hasPermission(doc.getRef(), permission)) {
                filtered.add(doc);
            }
        }
        return filtered;
    }

    protected DocumentModelList getWritableWorkspaces() throws ClientException {
        DocumentModelList workspaces = session.query(QUERY_WORKSPACES);
        return filterOnPermission(workspaces, READ_WRITE);
    }

    protected DocumentModelList findSameArchivedScars() throws ClientException {
        try {
            Blob blob = getZipExplorer().findScar();
            if (blob == null || StringUtils.isBlank(blob.getFilename())) {
                return null;
            }

            DocumentModelList docs = doc.getCoreSession().query(
                    String.format(QUERY_FIND_SCAR,
                            ZipExploder.prepareScarFilename(blob.getFilename())));
            return filterOnPermission(docs, READ_WRITE);
        } catch (IOException e) {
            throw new ClientException(e);
        }
    }

    @GET
    @Produces("text/html;charset=utf-8")
    public Object getImportScreen() throws ClientException, IOException {
        Map<String, Object> args = new HashMap<String, Object>();
        args.put("workspaces", getWritableWorkspaces());
        args.put("sameScars", findSameArchivedScars());

        return getView("import_screen").args(args);
    }

    @POST
    public Object getExplodeZipScreen(@FormParam("workspaceRef")
    String workspaceRef, @FormParam("scarRef")
    String scarRef) throws ClientException, URISyntaxException, IOException {
        if (StringUtils.isBlank(workspaceRef + scarRef)) {
            log.error("Trying to confirm scar archive without any parameters.");
            return Response.notModified().build();
        }

        DocumentModel newDoc;
        if (!StringUtils.isBlank(scarRef)) {
            newDoc = updateExistingScar(scarRef);
        } else {
            newDoc = createDocumentModel(workspaceRef);
        }

        String docUrl = getCodecManager().getUrlFromDocumentView(
                new DocumentViewImpl(newDoc), true,
                getContext().getBaseURL() + "/");
        return Response.created(new URI(docUrl)).build();
    }

    protected DocumentModel updateExistingScar(String scarRef)
            throws ClientException, IOException {
        DocumentModel oldScar = session.getDocument(new IdRef(scarRef));
        if (oldScar == null) {
            throw new ClientException(
                    "Try to update a non existing Scar archive.");
        }

        ZipExploder zipExplorer = getZipExplorer();
        if (!oldScar.getTitle().equals(
                ZipExploder.prepareScarFilename(zipExplorer.findScar().getFilename()))) {
            throw new ClientException("Trying to update another document.");
        }

        if (oldScar.isCheckedOut()) {
            session.checkIn(oldScar.getRef(), MINOR,
                    "Automatic versionning before updating SCAR.");
        }
        DocumentModel updatedDoc = zipExplorer.updateDocumentModel(doc, oldScar);
        updatedDoc.putContextData(VERSIONING_OPTION, MAJOR);
        return session.saveDocument(updatedDoc);
    }

    protected DocumentModel createDocumentModel(String workspaceRef)
            throws ClientException, IOException {
        DocumentModel workspace = session.getDocument(new IdRef(workspaceRef));

        ZipExploder ze = getZipExplorer();
        DocumentModel newDoc = session.createDocument(ze.createDocumentModel(
                doc, workspace));
        session.checkIn(newDoc.getRef(), MAJOR, null);
        session.removeDocument(doc.getRef());
        return newDoc;
    }
}
