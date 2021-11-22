const EmojiIO = (function () {

    /**
     * Creates a file containing all the emojis using
     * the MCEmoji format
     * @param {Map<string, Emoji>} emojis
     * @returns {Promise<Blob>} The resulting blob
     */
    async function write(emojis) {

        const data = [];

        // format version
        data.push(1);

        // emoji length
        data.push(emojis.size);

        for (const emoji of emojis.values()) {

            // emoji name
            data.push(emoji.name.length & 0xFF);
            for (let i = 0; i < emoji.name.length; i++) {
                const c = emoji.name.codePointAt(i);
                data.push(c >> 8, c & 0xFF);
            }

            const character = emoji.character.codePointAt(0);

            // height, ascent and character
            data.push(
                    emoji.height >> 8, emoji.height & 0xFF,
                    emoji.ascent >> 8, emoji.ascent & 0xFF,
                    character >> 8, character & 0xFF
            );

            // permission write
            data.push(emoji.permission.length & 0xFF);
            for (let i = 0; i < emoji.permission.length; i++) {
                const c = emoji.permission.codePointAt(i);
                data.push(c >> 8, c & 0xFF);
            }

            // image write
            const bin = window.atob(emoji.img.substring("data:image/png;base64,".length));
            const len = bin.length;

            data.push(len >> 8, len & 0xFF);
            for (let i = 0; i < len; i++) {
                data.push(bin.charCodeAt(i));
            }
        }

        return new Blob(
                [ new Uint8Array(data) ],
                { type: 'octet/stream' }
        );
    }

    /**
     *
     * @param {ArrayBuffer} buffer
     * @returns {Promise<Emoji[]>}
     */
    async function read(buffer) {
        const view = new Uint8Array(buffer);
        let cursor = 0;

        function readShort() {
            let byte1 = view[cursor++];
            let byte2 = view[cursor++];
            return (byte1 << 8) + byte2;
        }

        const version = view[cursor++];

        if (version !== 1) {
            throw new Error("Invalid format version");
        }

        const emojiLength = view[cursor++];
        const emojis = [];

        for (let i = 0; i < emojiLength; i++) {

            // read name
            const nameLength = view[cursor++];
            let name = "";

            for (let j = 0; j < nameLength; j++) {
                name += String.fromCharCode(readShort());
            }

            // height, ascent and character
            const height = readShort();
            const ascent = readShort();
            const character = String.fromCodePoint(readShort());

            // read permission
            const permissionLength = view[cursor++];
            let permission = "";

            for (let j = 0; j < permissionLength; j++) {
                permission += String.fromCharCode(readShort());
            }

            // image read
            const imageLength = readShort();
            let image = "";

            for (let j = 0; j < imageLength; j++) {
                image += String.fromCharCode(view[cursor++]);
            }

            const base64 = "data:image/png;base64," + window.btoa(image);

            emojis.push({
                name,
                height,
                ascent,
                character,
                permission,
                img: base64
            });
        }

        return emojis;
    }

    return { write, read };
})();