package dk.brics.jwig.boost.rendering.hierarchy;


public interface RemapEnterFormContent extends FormContent {
    /**
     * Specifies the submit value to submit when enter key is pressed in the
     * clients' browser.
     * 
     * Do not work in IE<9.
     * 
     * @return A string containing the submit value.
     */
    public String remapEnter();
}
