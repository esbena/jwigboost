package dk.brics.jwig.boost.rendering.uicomponents.dropdownboxwriter;

import java.util.ArrayList;
import java.util.List;

import dk.brics.jwig.boost.rendering.uicomponents.tablewriter.TableWriter;
import dk.brics.xact.XML;

/**
 * A drop down box where the user can select among entities. A primitive version
 * of {@link TableWriter}.
 */
public class DropDownBoxWriter<E> {

    private final DropdownFeature<? super E> feature;
    private final E selected;
    private final Iterable<E> items;
    private final String name;
    private String onchange;

    /**
     * @param feature
     *            as the features which decides how each item is rendered
     * @param items
     *            as the items to be rendered
     * @param selected
     *            as the item to be marked as selected
     * @param name
     *            as the name of the box (to be used when submitting a form)
     */
    public DropDownBoxWriter(DropdownFeature<? super E> feature,
            Iterable<E> items, E selected, String name) {
        this.feature = feature;
        this.items = items;
        this.selected = selected;
        this.name = name;
        this.onchange = null;
    }

    /**
     * Renders the box
     */
    public XML getBox() {
        XML xml = XML
                .parseTemplate("<select onchange=[ONCHANGE] name=[NAME]><[ROWS]></select>");
        List<XML> rowList = new ArrayList<>();
        for (E item : items) {
            XML row = XML
                    .parseTemplate(
                            "<option selected=[SELECTED] value=[ARG]><[VAL]></option>")
                    .plug("ARG", feature.serializeObject(item))
                    .plug("VAL", feature.renderObject(item));
            if ((item != null && item.equals(selected))
                    || (item == null && selected == null))
                row = row.plug("SELECTED", "selected");
            rowList.add(row);
        }
        xml = xml.plug("NAME", name);
        xml = xml.plug("ROWS", XML.concat(rowList));
        if (onchange != null)
            xml = xml.plug("ONCHANGE", onchange);
        return xml;
    }

    public void setTagAttribute_OnChange(String onchange) {
        this.onchange = onchange;
    }
}
