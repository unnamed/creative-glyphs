(function () {

    const $ = selectors => document.querySelector(selectors);
    const on = (element, events, listener) => events.forEach(event => element.addEventListener(event, listener));

    //#region Type Definitions
    /**
     * Represents an emoji for the emoji editor
     * @typedef {object} Emoji
     * @property {string} name The emoji name
     * @property {string} character The emoji character
     * @property {string} img The emoji data in Base64
     * @property {string} permission The emoji permission
     * @property {number} height The emoji height
     * @property {number} ascent The emoji ascent
     */
    //#endregion

    /**
     * A registry of emojis by their name
     * @type {Map<string, Emoji>}
     */
    const emojis = new Map();

    // anchor linking
    for (const anchor of document.getElementsByClassName("anchor")) {
        const id = anchor.dataset["for"];
        const target = document.getElementById(id);
        if (target) {
            anchor.addEventListener("click", () => target.scrollIntoView({
                behavior: "smooth",
                block: "end"
            }));
        }
    }

    const Dialog = (function () {

        const maxDialogs = 1;
        const dialogContainer = document.createElement("div");
        let dialogs = [];

        dialogContainer.classList.add("dialogs");
        document.body.appendChild(dialogContainer);

        /**
         * Shows a dialog with the given
         * heading and message
         * @param {string} heading Dialog title
         * @param {string} message Dialog message
         * @param {"info" | "error"} type Type of dialog
         */
        function add(heading, message, type = "info") {
            const close = document.createElement("button");
            const title = document.createElement("h3");
            const body = document.createElement("p");
            const element = document.createElement("div");
            element.classList.add("dialog");
            element.classList.add(type);

            title.innerText = heading;
            body.innerText = message;
            close.innerHTML = "&#10005;";

            element.append(title, close, body);
            dialogContainer.appendChild(element)
                    .animate([
                        { opacity: 0 },
                        { opacity: 1 }
                    ], {
                        duration: 500
                    });

            // remove last dialog if required
            if (dialogs.length >= maxDialogs) {
                const lastDialog = dialogs.shift();
                lastDialog.element.remove();
                clearTimeout(lastDialog.id);
            }

            // closing
            function remove() {
                element.remove();
                dialogs = dialogs.filter(e => e.id !== id);
            }
            const id = setTimeout(remove, 5000);
            close.addEventListener("click", () => {
                remove();
                clearTimeout(id);
            });

            dialogs.push({ id, element });
        }

        return { add };
    })();

    const EditorUI = (function () {

        const form = $(".file-input");

        on(form, ["dragover", "dragenter"], event => {
            event.preventDefault();
            event.stopPropagation();
            form.classList.add("is-dragover");
        });

        on(form, ["dragleave", "dragend"], event => {
            event.preventDefault();
            event.stopPropagation();
            form.classList.remove("is-dragover");
        });

        form.addEventListener("drop", event => {
            event.preventDefault();
            event.stopPropagation();

            /** @type FileList */
            const files = event.dataTransfer.files;
            for (let i = 0; i < files.length; i++) {
                const file = files[i];
                if (!file.name.endsWith(".png")) {
                    Dialog.add(
                            'Error loading emoji',
                            `Cannot load ${file.name}. Invalid extension`
                    );
                    return;
                }

                const reader = new FileReader();

                reader.addEventListener("load", ({target}) => {

                    const emoji = {
                        name: file.name.slice(0, -4), // remove .png extension
                        character: "",
                        img: target.result,
                        ascent: 8,
                        height: 9,
                        permission: ""
                    };

                    // if name is taken, use another name
                    while (emojis.has(emoji.name)) {
                        emoji.name = emoji.name + Math.floor(Math.random() * 1E5).toString(36);
                    }

                    emojis.set(emoji.name, emoji);
                    EditorUI.add(emoji);
                });
                reader.readAsDataURL(file);
            }
        });

        const container = $(".emojis");

        /**
         * @param {Emoji} emoji
         */
        function add(emoji) {
            function input(property, parse, validate, current) {
                const labelElement = document.createElement("label");
                labelElement.innerText = property;
                const element = document.createElement("input");
                element.type = "text";
                element.spellcheck = false;
                element.value = current;
                labelElement.appendChild(element);

                element.addEventListener("input", event => {
                    const value = event.target.value;
                    if (!validate(value)) {
                        element.classList.add("error");
                    } else {
                        element.classList.remove("error");
                        emojis.get(name)[property] = parse(value);
                    }
                });

                return {label: labelElement, input: element};
            }

            function regex(pattern) {
                return value => value.match(pattern);
            }

            const div = document.createElement("div");

            div.classList.add("emoji");

            const imgElement = document.createElement("img");
            const propertiesElement = document.createElement("div");

            propertiesElement.classList.add("properties");

            const nameElement = input("name", v => v, regex(/^[A-Za-z_]{1,14}$/g), emoji.name);
            const ascentElement = input("ascent", parseInt, regex(/^-?\d*$/g), emoji.ascent);
            const heightElement = input("height", parseInt, regex(/^-?\d*$/g), emoji.height);
            const permissionElement = input("permission", v => v, regex(/^[a-z0-9_.]+$/g), emoji.permission);
            const deleteElement = document.createElement("button");

            imgElement.src = emoji.img;
            deleteElement.innerText = "Remove";

            deleteElement.addEventListener("click", () => {
                div.remove();
                emojis.delete(name);
            });

            propertiesElement.append.apply(propertiesElement, [nameElement, ascentElement, heightElement, permissionElement].map(e => e.label));
            propertiesElement.appendChild(deleteElement);
            div.append(imgElement, propertiesElement);

            container.appendChild(div);
        }

        return { add };
    })();

    const EmojiIO = (function () {

        /**
         * Creates a file containing all the emojis using
         * the MCEmoji format
         * @returns {Promise<Blob>} The resulting blob
         */
        async function write() {

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

    $(".action-import").addEventListener("click", () => {
        const input = document.createElement("input");
        input.type = "file";
        input.addEventListener("change", ({target}) => {
            /** @type FileList */
            const files = target.files;
            for (let i = 0; i < files.length; i++) {
                const reader = new FileReader();
                reader.onload = e => {
                    EmojiIO.read(e.target.result).then(result => result.forEach(emoji => {
                        // if name is taken, use another name
                        while (emojis.has(emoji.name)) {
                            emoji.name = emoji.name + Math.floor(Math.random() * 1E5).toString(36);
                        }

                        emojis.set(emoji.name, emoji);
                        EditorUI.add(emoji);
                    }));
                };
                reader.readAsArrayBuffer(files[i]);
            }
        });
        document.body.appendChild(input);
        input.click();
        input.remove();
    });

    $(".action-export").addEventListener("click", () => {
        if (emojis.size < 1) {
            // no emojis, return
            Dialog.add(
                    "Error",
                    "No emojis to upload, add some emojis first!",
                    "error"
            );
            return;
        }
        EmojiIO.write()
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
                const command = `/emojis update ${id}`;

                navigator.clipboard.writeText(command).catch(console.error);

                Dialog.add(
                        "Uploaded & Copied Command!",
                        "Successfully uploaded emojis, execute the"
                        + `command (${command}) in your Minecraft server to load them.`,
                        "info"
                );
            });
    });

    $(".action-save").addEventListener("click", () => {
        if (emojis.size < 1) {
            // no emojis, return
            Dialog.add(
                    "Error",
                    "No emojis to save, add some emojis first!",
                    "error"
            );
            return;
        }
        EmojiIO.write().then(blob => {
            const a = document.createElement("a");
            a.setAttribute("href", URL.createObjectURL(blob));
            a.setAttribute("download", "emojis.mcemoji");
            document.body.appendChild(a);
            a.click();
            a.remove();
        });
    });

    // get github data
    (function () {
        const releasesContainer = $(".releases");
        const commitsContainer = $(".commits");
        const maxCommits = 10;

        fetch("https://api.github.com/repos/unnamed/emojis/releases")
                .then(response => response.json())
                .then(releases => releases.forEach(release => {
                    const { tag_name, prerelease, created_at, assets } = release;
                    if (assets.length === 0) {
                        return;
                    }

                    const date = new Date(Date.parse(created_at));

                    const { browser_download_url, download_count } = assets[0];
                    const element = document.createElement("div");
                    element.classList.add("release");

                    // TODO:
                    element.innerHTML = `
                        <h3>${tag_name} ${prerelease ? "(Prerelease)" : ""}</h3>
                        <p>${date.toLocaleString()}</p>
                        <p>${download_count} downloads</p>
                    `;

                    element.addEventListener("click", () => window.open(browser_download_url));
                    releasesContainer.appendChild(element);
                }));

        fetch("https://api.github.com/repos/unnamed/emojis/commits")
                .then(response => response.json())
                .then(commits => {
                    for (const commitJson of commits) {
                        if (commitsContainer.children.length > maxCommits) {
                            // limit
                            break;
                        }

                        let { sha, commit, html_url } = commitJson;
                        const message = commit.message;
                        sha = sha.substring(0, 6);

                        const element = document.createElement("span");
                        element.classList.add("commit");

                        // TODO:
                        element.innerHTML = `
                        <a href="${html_url}">${sha}</a> ${message}
                        `;

                        commitsContainer.appendChild(element);
                    }
                });
    })();

})();
