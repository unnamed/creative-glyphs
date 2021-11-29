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
    const emojisByChar = new Map();

    for (const anchor of document.getElementsByClassName("anchor")) {
        const id = anchor.dataset["for"];
        const target = document.getElementById(id);
        if (target) {
            anchor.addEventListener("click", () => target.scrollIntoView({
                behavior: "smooth"
            }));
        }
    }

    function generateCharacter() {
        let character = (1 << 15) - emojisByChar.size;
        while (emojisByChar.has(character)) {
            character--;
        }
        return String.fromCodePoint(character);
    }

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
                if (!file.name.toLowerCase().endsWith(".png")) {
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
                        character: generateCharacter(),
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
                    emojisByChar.set(emoji.character, emoji);
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
            const name = emoji.name;
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
            const characterElement = input("character", v => v, value => {
                const valid = value.match(/^.$/g);
                if (valid) {
                    // update
                    emojisByChar.delete(emoji.character);
                    emojisByChar.set(emoji.character, emoji);
                }
                return valid;
            }, emoji.character);
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

            propertiesElement.append.apply(propertiesElement, [nameElement, ascentElement, heightElement, permissionElement, characterElement].map(e => e.label));
            propertiesElement.appendChild(deleteElement);
            div.append(imgElement, propertiesElement);

            container.appendChild(div);
        }

        return { add };
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
                        if (emojisByChar.has(emoji.character)) {
                            emoji.character = generateCharacter();
                        }

                        emojis.set(emoji.name, emoji);
                        emojisByChar.set(emoji.character, emoji);
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
        EmojiIO.write(emojis)
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
        EmojiIO.write(emojis).then(blob => {
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
