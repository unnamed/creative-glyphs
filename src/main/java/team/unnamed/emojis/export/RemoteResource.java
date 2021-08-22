package team.unnamed.emojis.export;

/**
 * Class that represents a remote resource,
 * consists on a URL and the resource hash
 * @author yusshu (Andre Roldan)
 */
public class RemoteResource {

    private final String url;
    private final byte[] hash;

    public RemoteResource(String url, byte[] hash) {
        this.url = url;
        this.hash = hash;
    }

    public String getUrl() {
        return url;
    }

    public byte[] getHash() {
        return hash;
    }

}
