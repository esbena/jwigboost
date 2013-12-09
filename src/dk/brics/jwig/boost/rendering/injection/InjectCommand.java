package dk.brics.jwig.boost.rendering.injection;

/**
 * Wrapper class for XML pluggings above the cache - known as injections.s
 * 
 * @see InjectCommandFactory
 */
public interface InjectCommand {

    public String getGapName();

    public Object getValue();
}
