package dk.brics.jwig.boost.rendering.uicomponents;

import dk.brics.jwig.boost.Localizer;
import dk.brics.jwig.boost.RootApp;

public class PlaceholderAttribute extends Attribute {
    public PlaceholderAttribute(String value) {
        super("placeholder", Localizer.get().localize(value, RootApp.getLang()));
    }

}
