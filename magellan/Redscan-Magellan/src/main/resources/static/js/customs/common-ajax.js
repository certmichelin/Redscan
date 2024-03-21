/**
 * Add the CSRF token for each ajax call.
 * Require jQuery Cookie plugin.
 */
$.ajaxSetup({
    beforeSend: function (xhr, settings) {
        xhr.setRequestHeader("X-CSRFToken", $.cookie("csrftoken"));
    },
    contentType: 'application/json'
});

/**
 * Convert array to JSON string.
 * 
 * @param {*} array Array to convert.
 */
function convertArrayToJSON(array) {
    var arrayJson = "[";
    if (array !== null) {
        var prefix = "";
        array.forEach(function (value) {
            arrayJson += prefix + value;
            prefix = ",";
        });
    }
    arrayJson += "]";
    return arrayJson;
}
