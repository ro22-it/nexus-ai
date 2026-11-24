package ai.nexus.desktop;

public final class PlatformCredentialStore {

    private PlatformCredentialStore() {
    }

    public static CredentialStore create() {
        if (System.getProperty("os.name", "").toLowerCase().contains("mac")) {
            return new MacKeychainCredentialStore();
        }
        return new PreferencesCredentialStore();
    }
}
