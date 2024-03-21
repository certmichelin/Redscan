/**
 * Go back to the previous page with reload.
 */
function goBackWithRefresh(event) {
    if ('referrer' in document) {
        window.location = document.referrer;
    } else {
        window.history.back();
    }
}

/**
 * Toggle all elements of a list.
 */
function toggle(source) {
    checkboxes = document.getElementsByName('selection');
    for (var i in checkboxes)
        checkboxes[i].checked = source.checked;
}

/**
 * Enable automatically all the tooltips.
 */
$(function () {
    $('[data-toggle="tooltip"]').tooltip();
})

// sleep time expects milliseconds
function sleep(time) {
    return new Promise((resolve) => setTimeout(resolve, time));
}