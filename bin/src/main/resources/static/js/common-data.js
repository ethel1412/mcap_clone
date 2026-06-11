/**
 * Convert a form into a JSON object
 * @param {HTMLFormElement} form - The form element
 * @returns {Object} - Form data as key-value pairs
 */
function formToJSON(form) {
  const jsonData = {};
  const elements = form.querySelectorAll('input, select, textarea');

  elements.forEach(el => {
    if (!el.name || el.disabled) return; // skip unnamed/disabled

    let value;
    if (el.type === 'checkbox') {
      value = el.checked; // boolean
    } else if (el.type === 'radio') {
      if (!el.checked) return; // only take checked radio
      value = el.value;
    } else {
      value = (el.value || '').trim();
    }

    // Handle multiple fields with same name
    if (jsonData[el.name]) {
      if (!Array.isArray(jsonData[el.name])) {
        jsonData[el.name] = [jsonData[el.name]];
      }
      jsonData[el.name].push(value);
    } else {
      jsonData[el.name] = value;
    }
  });

  return jsonData;
}
