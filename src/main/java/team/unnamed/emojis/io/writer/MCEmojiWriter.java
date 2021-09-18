package team.unnamed.emojis.io.writer;

import team.unnamed.emojis.Emoji;
import team.unnamed.emojis.io.MCEmojiFormat;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;

/**
 * Implementation of {@link EmojiWriter} for writing
 * emojis using the MCEmoji format
 * @author yusshu (Andre Roldan)
 */
public class MCEmojiWriter implements EmojiWriter {

    @Override
    public void write(
            OutputStream output,
            Collection<Emoji> emojis
    ) throws IOException {

        // not in a try-with-resources because this shouldn't close the
        // original output stream
        DataOutputStream dataOutput = new DataOutputStream(output);

        // write current MCEmoji format
        dataOutput.write(MCEmojiFormat.VERSION);

        // write emoji length
        dataOutput.writeShort(emojis.size());

        // write all emojis
        for (Emoji emoji : emojis) {

            String name = emoji.getName();
            String permission = emoji.getPermission();

            // write name
            dataOutput.writeByte(name.length());
            dataOutput.writeChars(name);

            // height, ascent and character
            dataOutput.writeShort(emoji.getHeight());
            dataOutput.writeShort(emoji.getAscent());
            dataOutput.writeChar(emoji.getCharacter());

            // write permission
            dataOutput.writeByte(permission.length());
            dataOutput.writeChars(permission);

            // image write
            dataOutput.writeShort(emoji.getDataLength());
            emoji.getData().transfer(dataOutput);
        }
    }

}
