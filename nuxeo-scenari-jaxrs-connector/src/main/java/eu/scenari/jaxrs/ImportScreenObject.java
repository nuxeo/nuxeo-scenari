package eu.scenari.jaxrs;

import java.net.URI;
import java.net.URISyntaxException;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

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

import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ_WRITE;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 5.7
 */
public class ImportScreenObject extends DefaultObject {

    final CoreSession session;

    final DocumentModel doc;

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

    protected DocumentModelList getWritableWorkspaces() throws ClientException {
        DocumentModelList writableWorkspaces = new DocumentModelListImpl();
        CoreSession coreSession = getContext().getCoreSession();

        DocumentModelList docs = coreSession.query("SELECT * FROM Workspace WHERE ecm:isProxy = 0 AND "
                + "ecm:isCheckedInVersion = 0 AND ecm:currentLifeCycleState != 'deleted'");
        for (DocumentModel doc : docs) {
            if (coreSession.hasPermission(doc.getRef(), READ_WRITE)) {
                writableWorkspaces.add(doc);
            }
        }
        return writableWorkspaces;
    }

    @GET
    @Produces("text/html;charset=utf-8")
    public Object getImportScreen() throws ClientException {
        return getView("import_screen").arg("workspaces",
                getWritableWorkspaces());
    }

    @POST
    @Produces("text/html;charset=utf-8")
    public Object getExplodeZipScreen(@FormParam("workspaceRef")
    String workspaceRef) throws ClientException, URISyntaxException {

        DocumentModel workspace = session.getDocument(new IdRef(workspaceRef));
        DocumentModel newDoc = session.createDocumentModel(workspace.getPathAsString(),
                null, doc.getType());

        newDoc.copyContent(doc);
        newDoc = session.createDocument(newDoc);
        session.removeDocument(doc.getRef());

        String docUrl = getCodecManager().getUrlFromDocumentView(
                new DocumentViewImpl(newDoc), true, getContext().getBaseURL() + "/");

        return Response.created(new URI(docUrl)).build();
    }
}
