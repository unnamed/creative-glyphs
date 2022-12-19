package team.unnamed.emojis.resourcepack;

public class UrlAndHash {

    public final String url;
    public final String hash;

    public UrlAndHash(String url, String hash) {
        this.url = url;
        this.hash = hash;
    }

    public String url() {
        return url;
    }

    public String hash() {
        return hash;
    }

}
