(function () {

    const $ = selectors => document.querySelector(selectors);
    const on = (element, events, listener) => events.split(" ")
        .forEach(event => element.addEventListener(event, listener));
    const append = (element, children) => children
        .forEach(children => element.appendChild(children));

    const container = $(".emojis");
    const form = $(".file-input");
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

            append(element, [ title, close, body ]);
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

    function addEmoji(name, img, ascent, height, permission) {

        if (emojis.has(name)) {
            // if the name is duplicated, use other name
            name = Math.floor(Math.random() * 1E10).toString(36);
        }

        function input(property, parse, validate) {
            const labelElement = document.createElement("label");
            labelElement.innerText = property;
            const element = document.createElement("input");
            element.type = "text";
            element.spellcheck = false;
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

        const nameElement = input("name", v => v, regex(/^[A-Za-z_]{1,14}$/g));
        const ascentElement = input("ascent", parseInt, regex(/^-?\d*$/g));
        const heightElement = input("height", parseInt, regex(/^-?\d*$/g));
        const permissionElement = input("permission", v => v, regex(/^[a-z0-9_.]+$/g));
        const deleteElement = document.createElement("button");

        imgElement.src = img;
        nameElement.input.value = name;
        ascentElement.input.value = ascent;
        heightElement.input.value = height;
        permissionElement.input.value = permission;
        deleteElement.innerText = "Remove";

        deleteElement.addEventListener("click", () => {
            div.remove();
            emojis.delete(name);
        });

        append(propertiesElement, [nameElement, ascentElement, heightElement, permissionElement].map(e => e.label));
        propertiesElement.appendChild(deleteElement);
        append(div, [imgElement, propertiesElement]);

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
            Dialog.add(
                    `${errors.length} error(s) occurred`,
                    errors.join('\n'),
                    "error"
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

    $(".action-import").addEventListener("click", () => {
        load().catch(error =>
                Dialog.add("Error", error.message, "error"));
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
                const command = `/emojis update ${id}`;

                navigator.clipboard.writeText(command).catch(console.error);

                Dialog.add(
                        "Uploaded & Copied Command!",
                        `Successfully uploaded emojis, execute the"
                        + "command (${command}) in your Minecraft server to load them.`,
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
        createBlob().then(blob => {
            const downloadElement = document.createElement("a");
            downloadElement.setAttribute("href", URL.createObjectURL(blob));
            downloadElement.setAttribute("download", "emojis.mcemoji");
            document.body.appendChild(downloadElement);
            downloadElement.click();
            downloadElement.remove();
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
