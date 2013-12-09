package dk.brics.jwig.boost.rendering;

import dk.brics.xact.XML;

public interface ExceptionWrapper {
    XML getErrorMessage(Exception e);
}
