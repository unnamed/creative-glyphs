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