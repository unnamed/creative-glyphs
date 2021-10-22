package team.unnamed.emojis.resourcepack;

public class ResourcePack extends UrlAndHash {

    private final boolean required;
    private final String prompt;

    public ResourcePack(
            String url,
            String hash,
            boolean required,
            String prompt
    ) {
        super(url, hash);
        this.required = required;
        this.prompt = prompt;
    }

    public boolean isRequired() {
        return required;
    }

    public String getPrompt() {
        return prompt;
    }

    public ResourcePack withLocation(String url, String hash) {
        return new ResourcePack(url, hash, required, prompt);
    }

}
