package eu.scenari.jaxrs.utils;

import static java.nio.charset.Charset.defaultCharset;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.nuxeo.ecm.core.api.ClientException;

import com.phloc.commons.io.streams.StringInputStream;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 5.7
 */
public class SAXModifier {

    private final Document source;

    private final Document target;

    public SAXModifier(String source, String target) throws ClientException {
        try {
            SAXReader reader = new SAXReader();

            this.source = reader.read(new StringInputStream(source,
                    defaultCharset()));
            this.target = reader.read(new StringInputStream(target,
                    defaultCharset()));
            syncNamespaces();
        } catch (DocumentException e) {
            throw new ClientException(e);
        }
    }

    public SAXModifier(Document source, Document target) {
        this.source = source;
        this.target = target;
        syncNamespaces();
    }

    public void moveNodes(String xpathExpression) {
        SAXModifier.moveNodes(source, target, xpathExpression);
    }

    public void moveNode(String xpathExpression) {
        moveNode(source, target, xpathExpression, false);
    }

    public void moveNode(String xpathExpression, boolean deleteTargetNode) {
        SAXModifier.moveNode(source, target, xpathExpression, deleteTargetNode);
    }

    public void moveChildren(String xpathExpression) {
        SAXModifier.moveChildren(source, target, xpathExpression);
    }

    public String buildTargetAsXml() {
        SAXModifier.cleanNamespaces(target);
        return target.asXML();
    }

    protected void syncNamespaces() {
        SAXModifier.syncNamespaces(source, target);
    }

    public static void moveNodes(Document source, Document target,
            String xpathExpression) {
        List nodes = source.selectNodes(xpathExpression);
        if (nodes.size() > 0) {
            String targetPath = ((Node) nodes.get(0)).getParent().getPath();
            Element targetElement = (Element) target.selectSingleNode(targetPath);

            for (Object obj : nodes) {
                Node contribute = (Node) obj;
                targetElement.add(contribute.detach());
            }
        }
    }

    public static void syncNamespaces(Document source, Document target) {
        for(Object obj : source.getRootElement().additionalNamespaces()) {
            Namespace namespace = (Namespace) obj;
            target.getRootElement().add(namespace);
        }
    }

    public static void moveChildren(Document source, Document target, String xpathExpression) {
        Element sourceElt = (Element) source.selectSingleNode(xpathExpression);
        ((Element) target.selectSingleNode(xpathExpression)).appendContent(sourceElt);
    }

    public static void moveNode(Document source, Document target,
            String xpathExpression, boolean deleteTargetNode) {
        Node node = source.selectSingleNode(xpathExpression);

        if (deleteTargetNode) {
            Node tNode = target.selectSingleNode(xpathExpression);
            if (tNode != null) {
                tNode.detach();
            }
        }

        Node targetNode = target.selectSingleNode(node.getParent().getPath());

        if (targetNode.getNodeType() != Node.ELEMENT_NODE) {
            return;
        }

        ((Element) targetNode).add(node.detach());
    }

    public static void cleanNamespaces(Document source) {
        cleanNamespaces(source.getRootElement().content());
    }

    protected static void cleanNamespaces(List content) {
        for(Object obj : content) {
            if (obj instanceof Element) {
                Element elt = (Element) obj;

                for (Object nso : elt.declaredNamespaces()) {
                    Namespace ns = (Namespace) nso;
                    elt.remove(ns);
                }

                if (elt.hasContent()) {
                    cleanNamespaces(elt.content());
                }
            }
        }
    }
}
