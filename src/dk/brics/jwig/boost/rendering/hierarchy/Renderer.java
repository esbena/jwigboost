package dk.brics.jwig.boost.rendering.hierarchy;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;

import org.json.JSONException;
import org.json.JSONStringer;
import org.json.JSONWriter;

import dk.brics.jwig.boost.rendering.MenuWrapper;
import dk.brics.jwig.boost.rendering.Resource;
import dk.brics.xact.XML;

/**
 * Boost {@link Renderable} objects. Transforms the content of renderable
 * objects by wrapping and inserting crucial elements.
 */
public class Renderer {

    /**
     * Boosts the {@link Renderable} object. Emits the transformed content of
     * the renderable object.
     * 
     * @param renderable
     *            {@link Renderable} to boost
     * @param menuwrapper
     * @return the {@link Rendered} {@link Renderable}
     */
    public static XML render(Renderable renderable) {
        XML xml = XML.parseTemplate("<div><[VOLATILE]><[CONTENT]></div>");
        if (renderable instanceof Content) {
            // the context is likely to depend on the side effect of formcontent
            // being rendered.
            final XML content = ((Content) renderable).getContent();
            if (renderable instanceof GeneralFormContent) {
                xml = xml.plug("CONTENT",
                        handleFormContent((GeneralFormContent) renderable));
            } else if (renderable instanceof Volatile) {
                xml = xml.plug("VOLATILE", getVolatileJS(null));
            }
            xml = xml.plug("CONTENT", content);
        }
        return xml;
    }

    public static XML render(Renderable renderable, MenuWrapper menuwrapper) {
        XML xml = render(renderable);
        XML title = null;
        if (renderable instanceof Titled) {
            title = ((Titled) renderable).getTitle();
        }
        if (!(renderable instanceof Standalone))
            xml = menuwrapper.menuwrap(xml, title);

        xml = addResources(xml, findResources(renderable, menuwrapper));
        return xml;
    }

    private static List<Resource> findResources(Renderable renderable,
            MenuWrapper menuwrapper) {
        final List<Resource> resources = new ArrayList<>(
                menuwrapper.getResources());

        if (renderable instanceof HasOwnResources) {
            resources.addAll(((HasOwnResources) renderable).getResources());
        }
        return resources;
    }

    private static XML addResources(XML xml, List<Resource> resources) {
        return xml.plug("BOOST_RESOURCES", XML.concat(resources));
    }

    private static XML handleFormContent(GeneralFormContent formContent) {
        XML xml = XML
                .parseTemplate("<[JSGUARD]><form enctype=\"multipart/form-data\" onsubmit=[ONSUBMIT] id=[FORMID] action=[HANDLER] method=\"post\"><[CONTENT]><[HIDDENFIELDS]></form>");

        int random = formContent.hashCode() + new Random().nextInt();
        String formId = "evenform" + random;
        xml = xml.plug("FORMID", formId);

        Object handler = null;
        if (formContent instanceof FormContent) {
            handler = ((FormContent) formContent).getHandler();
        } else if (formContent instanceof URLHandlerFormContent) {
            handler = ((URLHandlerFormContent) formContent).getHandler();
        }
        if (handler == null)
            throw new IllegalArgumentException(
                    "The handler can not be null for a form!");
        xml = xml.plug("HANDLER", handler);
        if (formContent instanceof ContextFormContent) {
            xml = handleContextFormContent((ContextFormContent) formContent,
                    xml);
        }
        if (formContent instanceof JSGuardFormContent) {
            xml = handleJSGuardFormContent((JSGuardFormContent) formContent,
                    xml);
        }
        if (formContent instanceof RemapEnterFormContent) {
            xml = handleRemapEnterFormContent(
                    (RemapEnterFormContent) formContent, xml, formId);
        }
        if (formContent instanceof ConfirmableFormContent) {
            xml = handleConfirmableFormContent(
                    (ConfirmableFormContent) formContent, xml, formId);
        }
        if (formContent instanceof FormContentAttributes) {
            xml = xml.plug("ONSUBMIT",
                    ((FormContentAttributes) formContent).onFormSubmit());
        }
        if (formContent instanceof Volatile) {
            xml = handleVolatile(xml, formId);
        }
        return xml;
    }

    private static XML handleJSGuardFormContent(
            JSGuardFormContent jsFormContent, XML xml) {
        XML js = XML
                .parseTemplate("<script type=\"text/javascript\"><[CODE]></script>");
        js = js.plug("CODE", jsFormContent.getJSGuard());
        return xml.plug("JSGUARD", js);
    }

    private static XML handleContextFormContent(
            ContextFormContent contextFormContent, XML xml) {
        Map<String, Object> context = (contextFormContent).getContext();
        XML template = XML
                .parseTemplate("<input type='hidden' name=[NAME] value=[VALUE]/>");
        List<XML> hiddenValues = new ArrayList<>();
        for (Entry<String, Object> entry : context.entrySet()) {
            final Object value = entry.getValue();
            if (value instanceof Collection<?>) {
                Collection<?> collection = (Collection<?>) value;
                for (Object collectionValue : collection) {
                    hiddenValues.add(template.plug("NAME", entry.getKey())
                            .plug("VALUE", collectionValue));
                }
            } else {
                hiddenValues.add(template.plug("NAME", entry.getKey()).plug(
                        "VALUE", value));
            }
        }
        return xml.plug("HIDDENFIELDS", XML.concat(hiddenValues));

    }

    private static XML handleRemapEnterFormContent(RemapEnterFormContent erfc,
            XML xml, String formId) {
        XML event = XML
                .parseTemplate("<script type=\"text/javascript\">$(document).ready(function() { <[ENTER]> });</script>");

        event = event.plug(
                "ENTER",
                XML.parseTemplate("SubmitHandlers.remapEnter('" + formId
                        + "', '" + erfc.remapEnter() + "');"));

        return XML.concat(event, xml);
    }

    private static XML handleConfirmableFormContent(ConfirmableFormContent cfc,
            XML xml, String formId) {
        XML event = XML
                .parseTemplate("<script type=\"text/javascript\">$(document).ready(function() { <[CONFIRM]> });</script>");

        String jsonConfirms = "";
        try {
            JSONWriter confirms = new JSONStringer().array();
            for (Entry<String, String> confirm : cfc.confirmSubmit().entrySet()) {
                confirms = confirms.object().key("match")
                        .value(confirm.getKey()).key("msg")
                        .value(confirm.getValue()).endObject();
            }
            jsonConfirms = confirms.endArray().toString();
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        event = event.plug(
                "CONFIRM",
                XML.parseTemplate("SubmitHandlers.confirmSubmit('" + formId
                        + "', " + jsonConfirms + ");"));

        return XML.concat(event, xml);
    }

    private static XML handleVolatile(XML xml, String formId) {
        return XML.concat(getVolatileJS(formId), xml);
    }

    private static XML getVolatileJS(String formId) {
        XML js = XML
                .parseTemplate("<script type=\"text/javascript\"><[CODE]></script>");
        js = js.plug(
                "CODE",
                XML.parseTemplate("$(document).ready(function() { SubmitHandlers.confirmLeaveAndDiscard("
                        + "'<[s_Confirmleavediscard]>' <[FORMID]>); } );"));

        if (formId != null)
            js = js.plug("FORMID", ", '" + formId + "'");

        return XML.concat(js);
    }
}
