package dk.brics.jwig.boost.rendering.injection;

/**
 * Factory interface for creating {@link InjectCommand}s. It is crucial that the
 * constructed injectcommand is instantiated with the context of the time of the
 * injection, rather than the context at the time of the instantiation of the
 * factory.
 * 
 * @see InjectCommand
 */
public interface InjectCommandFactory {

    public InjectCommand create();

}
