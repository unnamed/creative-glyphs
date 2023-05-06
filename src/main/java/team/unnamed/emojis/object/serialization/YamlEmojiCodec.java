package team.unnamed.emojis.object.serialization;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.intellij.lang.annotations.Subst;
import team.unnamed.emojis.Emoji;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;

final class YamlEmojiCodec implements EmojiCodec {

    private final File texturesFolder;

    public YamlEmojiCodec(File texturesFolder) {
        this.texturesFolder = texturesFolder;
    }

    @Override
    public Collection<Emoji> read(InputStream input) throws IOException {

        Collection<Emoji> emojis = new HashSet<>();
        YamlConfiguration yaml = YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));

        for (@Subst("emoji") String name : yaml.getKeys(false)) {
            ConfigurationSection section = yaml.getConfigurationSection(name);

            if (section == null) {
                // impossible, but editor requires this to remove warning
                continue;
            }

            @Subst("emojis.emoji") String permission = section.getString("permission", "");
            String characterStr = section.getString("character", null);

            if (characterStr == null) {
                throw new IOException("Emoji '" + name + "' didn't specify a character");
            }

            int height = section.getInt("height");
            int ascent = section.getInt("ascent");
            int character = characterStr.codePointAt(0);

            if (Character.isBmpCodePoint(character)) {
                if (characterStr.length() != 1) {
                    throw new IOException("Emoji '" + name + "' has an invalid character: " + characterStr);
                }
            } else if (characterStr.length() != 2) {
                throw new IOException("Emoji '" + name + "' has an invalid character: " + characterStr);
            }

            // read texture
            File file = new File(texturesFolder, name + ".png");
            if (!file.exists()) {
                throw new IOException("Emoji '" + name + "' doesn't have a texture. Expected to be in: " + file);
            }

            emojis.add(
                    Emoji.builder()
                            .name(name)
                            .permission(permission)
                            .height(height)
                            .ascent(ascent)
                            .character(character)
                            .data(Files.readAllBytes(file.toPath()))
                            .addNameUsage()
                            .build()
            );
        }

        return emojis;
    }

    @Override
    public void write(OutputStream output, Collection<Emoji> emojis) throws IOException {

        YamlConfiguration yaml = new YamlConfiguration();

        for (Emoji emoji : emojis) {
            ConfigurationSection section = yaml.createSection(emoji.name());
            section.set("permission", emoji.permission());
            section.set("character", emoji.replacement()); // not emoji.character() because we need the string representation
            section.set("height", emoji.height());
            section.set("ascent", emoji.ascent());

            // write emoji texture
            File textureFile = new File(texturesFolder, emoji.name() + ".png");
            try (OutputStream textureOutput = new FileOutputStream(textureFile)) {
                emoji.data().write(textureOutput);
            }
        }

        // YamlConfiguration doesn't have a save method that accepts OutputStream :(
        Streams.writeUTF(output, yaml.saveToString());
    }

}
