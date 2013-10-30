package eu.scenari;

import static junit.framework.Assert.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.net.URL;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nuxeo.ecm.core.api.ClientException;

import eu.scenari.jaxrs.utils.SAXModifier;

/**
 * @author <a href="mailto:ak@nuxeo.com">Arnaud Kervern</a>
 * @since 5.7
 */
public class SAXTester {

    Document orioai;

    Document scenari;

    @Before
    public void setUp() throws DocumentException {
        URL xml = this.getClass().getClassLoader().getResource("sample_ori.xml");
        assertNotNull(xml);

        SAXReader reader = new SAXReader();
        orioai = reader.read(xml);
        assertNotNull(orioai);

        xml = this.getClass().getClassLoader().getResource("scenari.xml");
        assertNotNull(xml);

        scenari = reader.read(xml);
        assertNotNull(scenari);
    }

    @Test
    public void testParser() throws DocumentException {
        int identifiers = scenari.selectNodes("//lom:identifier").size();

        List list = orioai.selectNodes("//lom:identifier");
        assertNotNull(list);
        assertFalse(list.isEmpty());

        for (Object aList : list) {
            Node node = (Node) aList;
            Element parent = node.getParent();

            Node scenariNode = scenari.selectSingleNode(parent.getPath());
            assertNotNull(scenariNode);

            ((Element) scenariNode).add(node.detach());
        }

        // check added nodes
        int newIdentifiers = scenari.selectNodes("//lom:identifier").size();
        Assert.assertTrue(newIdentifiers == identifiers + 2);
    }

    @Test
    public void testScenariMerging() {
        // - lom:general/lom:identifier
        assertNull(scenari.selectSingleNode("//lom:general/lom:identifier"));
        SAXModifier.moveNode(orioai, scenari, "//lom:general/lom:identifier",
                false);
        assertNotNull(scenari.selectSingleNode("//lom:general/lom:identifier"));

        // - lom:metadata override ?
        int sourceBeforeSize = orioai.selectNodes("//lom:metadataSchema").size();
        assertTrue(sourceBeforeSize > 0);
        SAXModifier.moveNode(orioai, scenari, "//lom:metaMetadata", true);
        int afterSize = scenari.selectNodes("//lom:metadataSchema").size();
        Assert.assertTrue(afterSize == sourceBeforeSize);

        // - lom:educational
        int targetBeforeSize = ((Element)scenari.selectSingleNode("//lom:educational")).content().size();
        sourceBeforeSize = ((Element)orioai.selectSingleNode("//lom:educational")).content().size();
        SAXModifier.moveChildren(orioai, scenari, "//lom:educational");
        assertEquals(targetBeforeSize + sourceBeforeSize, ((Element) scenari.selectSingleNode("//lom:educational")).content().size());

        // - lom:contribute(s)
        targetBeforeSize = scenari.selectNodes("//lom:contribute").size();
        sourceBeforeSize = orioai.selectNodes("//lom:contribute").size();
        assertTrue(sourceBeforeSize > 0);
        SAXModifier.moveNodes(orioai, scenari, "//lom:contribute");
        afterSize = scenari.selectNodes("//lom:contribute").size();
        Assert.assertTrue(afterSize == targetBeforeSize + sourceBeforeSize);

        // - lom:classification
        assertNull(scenari.selectSingleNode("//lom:classification"));
        SAXModifier.moveNode(orioai, scenari, "//lom:classification", false);
        assertNotNull(scenari.selectSingleNode("//lom:classification"));
    }

    @Test
    public void lomNamespace() {
        Element oriRoot = orioai.getRootElement();
        Element scenariRoot = scenari.getRootElement();
        for(Object obj : oriRoot.additionalNamespaces()) {
            Namespace namespace = (Namespace) obj;
            scenariRoot.add(namespace);
        }

        assertEquals(oriRoot.additionalNamespaces().size(), scenariRoot.additionalNamespaces().size());
    }

    @Test
    public void testSAXFromString() throws ClientException {
        SAXModifier modifier = new SAXModifier(orioai.asXML(), scenari.asXML());
        modifier.moveNode("//lom:general/lom:identifier");
        assertNotSame(scenari.asXML(), modifier.buildTargetAsXml());
    }
}
