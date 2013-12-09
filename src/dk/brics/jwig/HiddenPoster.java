package dk.brics.jwig;

public abstract class HiddenPoster extends SubmitHandler {
    private final String handlerLink;

    public HiddenPoster() {
        handlerLink = getHandlerIdentifier();
    }

    @Override
    public String toString() {
        return toStringNoReturn() + "return false;";
    }

    public String toStringReturnTrue() {
        return toStringNoReturn() + "return true;";
    }

    public String toStringNoReturn() {
        return "jwigboost.hiddenPost(this, '" + handlerLink + "');";
    }
}
