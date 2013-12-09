package dk.brics.jwig.boost.rendering.hierarchy;

import java.util.Map;

public interface ConfirmableFormContent extends FormContent {
    /**
     * Prompt the user for confirmation when certain actions are submitted.
     * 
     * @return A map containing mappings from regular expressions matching
     *         submit name, and confirm message to be shown.
     */
    public Map<String, String> confirmSubmit();
}
