package ai.nexus.desktop;

import java.util.prefs.Preferences;

public class PreferencesCredentialStore implements CredentialStore {

    private final Preferences preferences = Preferences.userNodeForPackage(NexusDesktopApplication.class)
            .node("session");

    @Override
    public String get(String key) {
        return preferences.get(key, null);
    }

    @Override
    public void put(String key, String value) {
        preferences.put(key, value);
    }

    @Override
    public void remove(String key) {
        preferences.remove(key);
    }
}
