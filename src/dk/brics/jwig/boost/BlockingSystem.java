package dk.brics.jwig.boost;

import java.util.HashSet;
import java.util.Set;

public class BlockingSystem {

    private final Set<String> blockedHosts = new HashSet<>();
    private static BlockingSystem instance = new BlockingSystem();

    private BlockingSystem() {
        //
    }

    public static BlockingSystem getInstance() {
        return instance;
    }

    public void blockHost(String hostname) {
        blockedHosts.add(hostname);
    }

    public boolean isBlocked(String hostname) {
        return blockedHosts.contains(hostname);
    }

    public void unBlock(String hostname) {
        blockedHosts.remove(hostname);
    }
}
