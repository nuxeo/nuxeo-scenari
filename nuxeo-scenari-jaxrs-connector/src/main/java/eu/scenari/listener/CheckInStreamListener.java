package eu.scenari.listener;

import static org.nuxeo.ecm.core.api.event.DocumentEventTypes.DOCUMENT_CHECKEDIN;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.nuxeo.ecm.activity.Activity;
import org.nuxeo.ecm.activity.ActivityBuilder;
import org.nuxeo.ecm.activity.ActivityHelper;
import org.nuxeo.ecm.activity.ActivityStreamListener;
import org.nuxeo.ecm.core.api.ClientException;
import org.nuxeo.ecm.core.api.CoreSession;
import org.nuxeo.ecm.core.api.DocumentModel;
import org.nuxeo.ecm.core.api.DocumentRef;
import org.nuxeo.ecm.core.event.Event;
import org.nuxeo.ecm.core.event.impl.DocumentEventContext;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 */
public class CheckInStreamListener extends ActivityStreamListener {

    private Log log = LogFactory.getLog(CheckInStreamListener.class);

    @Override
    protected List<String> getHandledEventsName() {
        return Arrays.asList(DOCUMENT_CHECKEDIN);
    }

    @Override
    protected String getDocumentTitle(CoreSession session, DocumentRef docRef) {
        try {
            DocumentModel doc = session.getDocument(docRef);
            return getDocumentTitle(doc);
        } catch (ClientException e) {
            log.debug(e);
        }
        return docRef.toString();
    }

    protected String getDocumentTitle(DocumentModel doc) {
        String title = ActivityHelper.getDocumentTitle(doc);
        if (doc.isVersionable()) {
            return String.format("%s (%s)", title, doc.getVersionLabel());
        }
        return title;
    }

    @Override
    protected Activity toActivity(DocumentEventContext docEventContext,
                                  Event event, String context) {
        Principal principal = docEventContext.getPrincipal();
        DocumentModel doc = docEventContext.getSourceDocument();
        return new ActivityBuilder().actor(
                ActivityHelper.createUserActivityObject(principal)).displayActor(
                ActivityHelper.generateDisplayName(principal)).verb(
                event.getName()).object(
                ActivityHelper.createDocumentActivityObject(doc)).displayObject(
                getDocumentTitle(doc)).target(
                ActivityHelper.createDocumentActivityObject(
                        doc.getRepositoryName(), doc.getParentRef().toString())).displayTarget(
                getDocumentTitle(docEventContext.getCoreSession(),
                        doc.getParentRef())).context(context).build();
    }
}
