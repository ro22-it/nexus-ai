package ai.nexus.desktop;

public interface CredentialStore {

    String get(String key);

    void put(String key, String value);

    void remove(String key);
}
