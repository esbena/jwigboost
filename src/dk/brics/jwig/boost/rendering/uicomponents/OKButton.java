package dk.brics.jwig.boost.rendering.uicomponents;

import dk.brics.xact.ToXMLable;
import dk.brics.xact.XML;

public class OKButton implements ToXMLable {

    @Override
    public XML toXML() {
		return XML.parseTemplate("<input type='submit' name='ok' value=[sa_OK]/>");
    }

}
