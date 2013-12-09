package dk.brics.jwig.boost.rendering.uicomponents;

import dk.brics.xact.AttrNode;
import dk.brics.xact.Element;
import dk.brics.xact.ToXMLable;
import dk.brics.xact.XML;

public class HTMLTag implements ToXMLable {

    private final String tagName;
    private Object content = XML.parseTemplate("");
    private AttributeStrategy attributeStrategy = new EmptyAttributeStrategy();

    public HTMLTag(String tagName) {
        this.tagName = tagName;
    }

    public HTMLTag(String tagName, Object content) {
        this(tagName);
        this.content = content;
    }

    public void setContent(Object content) {
        this.content = content;
    }

    public void setAttributeStrategy(AttributeStrategy attributeStrategy) {
        this.attributeStrategy = attributeStrategy;
    }

    @Override
    public XML toXML() {
        // Create tag with defaut namespace
        final String ns = XML.getNamespaceMap().get("");
        XML tag = XML.toXML(new Element(ns, tagName));

        // Set content
        tag = tag.getFirstElement().setContent(content);

        // Set attributes
        for (AttrNode attr : attributeStrategy.getAttributes()) {
            tag = tag.set(attr);
        }

        return tag;
    }
}
