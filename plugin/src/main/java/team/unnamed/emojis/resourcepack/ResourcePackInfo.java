package team.unnamed.emojis.resourcepack;

import org.jetbrains.annotations.Nullable;

public class ResourcePackInfo extends UrlAndHash {

    private final boolean required;
    @Nullable private final String prompt;

    public ResourcePackInfo(
            String url,
            String hash,
            boolean required,
            @Nullable String prompt
    ) {
        super(url, hash);
        this.required = required;
        this.prompt = prompt;
    }

    public boolean required() {
        return required;
    }

    public @Nullable String prompt() {
        return prompt;
    }

    public ResourcePackInfo withLocation(String url, String hash) {
        return new ResourcePackInfo(url, hash, required, prompt);
    }

}
