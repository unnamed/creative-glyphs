(function () {

    const $ = selectors => document.querySelector(selectors);
    const on = (element, events, listener) => events.split(" ")
        .forEach(event => element.addEventListener(event, listener));
    const append = (element, children) => children
        .forEach(children => element.appendChild(children));

    const container = $(".emoji-container");
    const form = $(".file-input");
    const emojis = new Map();

    /**
     * Object representing an information dialog,
     * used to show information to the user
     */
    const dialog = (function () {
        const containerElement = $(".dialog-container");
        const titleElement = $(".dialog-title");
        const contentElement = $(".dialog-content");
        const closeButton = $(".dialog-close");
        let callback = () => true;

        closeButton.addEventListener("click", () => {
            if (callback()) {
                containerElement.classList.add("hidden");
            }
        });

        return {
            /**
             * Shows the dialog using the given title,
             * content and some extra options
             * @param {string} title Dialog title
             * @param {string} content Dialog content
             * @param {Function} cb Callback (executed when clicking
             * the submit button)
             * @param close The close button text
             */
            show(title, content, cb = (() => true), close = "Ok") {
                titleElement.innerText = title;
                contentElement.innerText = content;
                closeButton.innerText = close;
                containerElement.classList.remove("hidden");
                if (cb) {
                    callback = cb;
                }
            }
        };
    })();

    function addEmoji(name, img, ascent, height, permission) {

        if (emojis.has(name)) {
            // if the name is duplicated, use other name
            name = Math.floor(Math.random() * 1E10).toString(36);
        }

        function input(property, parse, validate) {
            const labelElement = document.createElement("label");
            labelElement.innerText = property;
            const element = document.createElement("input");
            labelElement.appendChild(element);

            element.addEventListener("input", event => {
                const value = event.target.value;
                if (!validate(value)) {
                    labelElement.classList.add("input-error");
                } else {
                    labelElement.classList.remove("input-error");
                    emojis.get(name)[property] = parse(value);
                }
            });

            return {label: labelElement, input: element};
        }

        function regex(pattern) {
            return value => value.match(pattern);
        }

        const div = document.createElement("div");

        div.classList.add("ghost", "emoji");

        const imgElement = document.createElement("img");
        const propertiesElement = document.createElement("div");
        const deleteButton = document.createElement("button");

        deleteButton.innerHTML = "&#x2715;";
        deleteButton.classList.add("delete-button");

        deleteButton.addEventListener("click", () => {
            // set to undefined and don't change the others emojis index
            emojis.delete(name);
            // remove the card
            container.removeChild(div);
        });

        propertiesElement.classList.add("properties");

        const nameElement = input("name", v => v, regex(/^[A-Za-z_]{1,14}$/g));
        const ascentElement = input("ascent", parseInt, regex(/^-?\d*$/g));
        const heightElement = input("height", parseInt, regex(/^-?\d*$/g));
        const permissionElement = input("permission", v => v, regex(/^[a-z0-9_.]+$/g));

        imgElement.src = img;
        nameElement.input.value = name;
        ascentElement.input.value = ascent;
        heightElement.input.value = height;
        permissionElement.input.value = permission;

        append(propertiesElement, [nameElement, ascentElement, heightElement, permissionElement].map(e => e.label));
        append(div, [deleteButton, imgElement, propertiesElement]);

        container.appendChild(div);

        emojis.set(name, {name, img, ascent, height, permission });
    }

    on(form, "dragover dragenter", event => {
        event.preventDefault();
        event.stopPropagation();
        form.classList.add("is-dragover");
    });

    on(form, "dragleave dragend drop", event => {
        event.preventDefault();
        event.stopPropagation();
        form.classList.remove("is-dragover");
    });

    on(form, "drop", event => {
        const errors = [];

        for (const file of event.dataTransfer.files) {
            const reader = new FileReader();
            const name = file.name;

            if (!name.endsWith(".png")) {
                errors.push(`Cannot load ${name}. Invalid extension.`);
                continue;
            }

            reader.addEventListener("load", event => addEmoji(
                name.slice(0, -4), // remove the .png extension
                event.target.result,
                8,
                9,
                ""
            ));
            reader.readAsDataURL(file);
        }

        if (errors.length > 0) {
            dialog.show(
                `${errors.length} errors occurred`,
                errors.join('\n')
            );
        }
    });

    /**
     * Creates a file containing all the emojis using
     * the MCEmoji format
     * @returns {Promise<Blob>} The resulting blob
     */
    async function createBlob() {

        const data = [];
        let char = 1 << 15;

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

            // height, ascent and character
            data.push(
                emoji.height >> 8, emoji.height & 0xFF,
                emoji.ascent >> 8, emoji.ascent & 0xFF,
                char >> 8, char & 0xFF
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

            char--;
        }

        return new Blob(
            [ new Uint8Array(data) ],
            { type: 'octet/stream' }
        );
    }

    async function load() {
        return new Promise((resolve, reject) => {
            const input = document.createElement("input");
            input.type = "file";
            input.addEventListener("change", event => {
                const file = event.target.files[0];
                const reader = new FileReader();

                reader.readAsArrayBuffer(file);
                reader.addEventListener("load", loadEvent => {
                    /** @type {ArrayBuffer} */
                    const buffer = loadEvent.target.result;
                    const view = new Uint8Array(buffer);
                    let cursor = 0;

                    function readShort() {
                        let byte1 = view[cursor++];
                        let byte2 = view[cursor++];
                        return (byte1 << 8) + byte2;
                    }

                    const version = view[cursor++];

                    if (version !== 1) {
                        reject(new Error("Invalid format version"));
                        return;
                    }

                    const emojiLength = view[cursor++];

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
                        const character = readShort();

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

                        addEmoji(name, base64, ascent, height, permission);
                    }
                    resolve();
                });
            });
            document.body.appendChild(input);
            input.click();
            input.remove();
        });
    }

    $(".import").addEventListener("click", () => {
        load()
            .catch(error => {
                dialog.show(
                    "Error",
                    error.message
                );
            });
    });

    $(".export").addEventListener("click", () => {
        if (emojis.size < 1) {
            // no emojis, return
            dialog.show(
                "Error",
                "No emojis to upload, first add some emojis!"
            );
            return;
        }
        createBlob()
            .then(blob => {
                const formData = new FormData();
                formData.set("file", blob);
                return fetch(
                    'https://artemis.unnamed.team/tempfiles/upload/',
                    {method: "POST", body: formData}
                );
            })
            .then(response => response.json())
            .then(response => {
                const { id } = response;
                dialog.show(
                    "Uploaded!",
                    "Successfully uploaded the emojis, execute this" +
                    " command in your Minecraft server to load them.",
                    () => {
                        navigator.clipboard.writeText(`/emojis update ${id}`)
                            .then(() => $(".dialog-container").classList.add("hidden"))
                            .catch(console.error);
                    },
                    "Copy Command"
                );
            });
    });

    $(".save").addEventListener("click", () => {
        if (emojis.size < 1) {
            // no emojis, return
            dialog.show(
                "Error",
                "No emojis to save, first add some emojis!"
            );
            return;
        }
        createBlob().then(blob => {
            const downloadElement = document.createElement("a");
            downloadElement.setAttribute("href", URL.createObjectURL(blob));
            downloadElement.setAttribute("download", "emojis.mcemoji");
            document.body.appendChild(downloadElement);
            downloadElement.click();
            downloadElement.remove();
        });
    });

})();
