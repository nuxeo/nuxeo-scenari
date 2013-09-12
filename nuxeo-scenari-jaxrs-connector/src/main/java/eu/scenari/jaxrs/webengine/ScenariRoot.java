/*
 * (C) Copyright ${year} Nuxeo SA (http://nuxeo.com/) and contributors.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Lesser General Public License
 * (LGPL) version 2.1 which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/lgpl.html
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * Contributors:
 *     ogrisel
 */

package eu.scenari.jaxrs.webengine;

import static org.nuxeo.ecm.core.api.security.SecurityConstants.READ_WRITE;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.Principal;
import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.core.api.Blob;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.api.UnrestrictedSessionRunner;
import org.nuxeo.ecm.core.api.impl.blob.StreamingBlob;
import org.nuxeo.ecm.core.api.security.ACE;
import org.nuxeo.ecm.core.api.security.ACL;
import org.nuxeo.ecm.core.api.security.ACP;
import org.nuxeo.ecm.platform.web.common.vh.VirtualHostHelper;
import org.nuxeo.ecm.webengine.jaxrs.session.SessionFactory;
import org.nuxeo.ecm.webengine.model.WebObject;
import org.nuxeo.ecm.webengine.model.impl.ModuleRoot;

/**
 * HTTP API with Cross Origin Resource Sharing support to make it possible to
 * import blobs in temporary file and then redirect the client to a Web page to
 * let the user finish the import (select container workspace or folder, choose
 * to create as new document or updated existing document) and trigger
 * additional actions on the result.
 *
 * @author ogrisel
 */
@Path("/scenari")
@WebObject(type = "ScenariRoot")
public class ScenariRoot extends ModuleRoot {

    private static final Log log = LogFactory.getLog(ScenariRoot.class);

    protected final HttpHeaders headers;

    protected final Set<String> authorizedOrigins = new HashSet<String>();

    protected CoreSession session;

    public ScenariRoot(@Context
    HttpServletRequest request, @Context
    HttpHeaders headers) {
        this.headers = headers;

        // TODO: read framework property to configure authorized origins instead
        authorizedOrigins.add("*");

        try {
            session = SessionFactory.getSession(request);
        } catch (WebApplicationException e) {
            log.debug("Unable to instantiate CoreSession, in case of open url.");
        }
    }

    @OPTIONS
    public Response handleCorsPreflightOnManifest(@Context
    HttpHeaders headers) {
        return Response.ok().build();
    }

    @GET
    @Produces("application/xml")
    public Object getRoot() {
        log.info("Deprecated use /manifest instead.");
        return getManifest();
    }

    @GET
    @Produces("application/xml")
    @Path("/manifest")
    public Object getManifest() {
        ResponseBuilder builder = Response.ok(getView("index"));
        return builder.build();
    }

    /* ZIP Upload */
    public String getZipUploadUrl() {
        return String.format("%s/upload", getModuleURL());
    }

    @OPTIONS
    @Path("/upload")
    public Response handleCorsPreflightForUpload() {
        return Response.ok().build();
    }

    @POST
    @Path("/upload")
    public Object upload(InputStream input) throws URISyntaxException,
            ClientException, IOException {
        final Blob zipBlob = StreamingBlob.createFromStream(input).persist();
        if (zipBlob.getLength() < 1) {
            return Response.noContent().build();
        }

        ZipDocumentImporter importer = new ZipDocumentImporter(session, zipBlob);
        importer.runUnrestricted();
        ResponseBuilder builder = Response.created(getImportScreenUrl(
                session.getRepositoryName(), importer.documentRef));
        return builder.build();
    }

    /* Document Import UI */
    protected URI getImportScreenUrl(String repositoryName, DocumentRef ref)
            throws URISyntaxException {
        return new URI(String.format("%s/importscreen/%s/%s", getModuleURL(),
                repositoryName, ref.toString()));
    }

    public String getModuleURL() {
        return String.format("%s%s", VirtualHostHelper.getServerURL(request),
                getContext().getModulePath().substring(1));
    }

    @OPTIONS
    @Path("/importscreen")
    public Response handleCorsPreflightForImportScreen() {
        ResponseBuilder res = Response.ok();
        return res.build();
    }

    @Path("/importscreen/{repository}/{idref}")
    public Object importScreen(@PathParam("repository")
    String repository, @PathParam("idref")
    String idRef) throws ClientException {
        return new ImportScreenObject(getContext(), repository, idRef);
    }

    public static class ZipDocumentImporter extends UnrestrictedSessionRunner {

        protected Blob blob;

        public DocumentRef documentRef;

        protected final Principal principal;

        public ZipDocumentImporter(CoreSession session, Blob zipBlob) {
            super(session);
            this.principal = session.getPrincipal();
            this.blob = zipBlob;
        }

        @Override
        public void run() throws ClientException {
            DocumentModel zipBlobDoc = session.createDocumentModel("File");
            zipBlobDoc.setPropertyValue("file:content", (Serializable) blob);
            zipBlobDoc = session.createDocument(zipBlobDoc);
            ACP acp = zipBlobDoc.getACP();
            ACL acl = acp.getOrCreateACL();
            acl.add(new ACE(principal.getName(), READ_WRITE, true));
            acp.addACL(acl);
            documentRef = zipBlobDoc.getRef();
            session.setACP(documentRef, acp, true);
        }

    }

}
