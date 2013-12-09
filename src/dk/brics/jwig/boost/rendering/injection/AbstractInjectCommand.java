package dk.brics.jwig.boost.rendering.injection;



public abstract class AbstractInjectCommand implements InjectCommand {
    private final String gapName;

    public AbstractInjectCommand(String gapName) {
        this.gapName = gapName;
    }

    @Override
    public String getGapName() {
        return gapName;
    }

    
}
