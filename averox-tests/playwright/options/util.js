const e = require('../core/elements');
const defaultLocale = require('../../../averox-html5/public/locales/en.json');

async function openSettings(test) {
  await test.waitAndClick(e.optionsButton);
  await test.waitAndClick(e.settings);
}

async function getLocaleValues(elements, locale) {
  const currentValues = {};
  let currentLocale = {};
  try {
    currentLocale = require(`../../../averox-html5/public/locales/${locale.replace('-', '_')}.json`);
  } catch (err) { }

  for (const selector in elements) {
    const currentKey = elements[selector];
    currentValues[selector] = currentLocale[currentKey] ? currentLocale[currentKey] : getValueFromSecondaryLocale();

    function getValueFromSecondaryLocale() {
      const generalLocaleName = locale.split('-')[0];
      let generalLocale = {};
      try {
        generalLocale = require(`../../../averox-html5/public/locales/${generalLocaleName}.json`);
      } catch (err) { }
      return generalLocale[currentKey] ? generalLocale[currentKey] : defaultLocale[currentKey];
    }
  }
  return currentValues;
}

async function openAboutModal(test) {
  await test.waitAndClick(e.optionsButton);
  await test.waitAndClick(e.showAboutModalButton);
}

exports.openAboutModal = openAboutModal;
exports.openSettings = openSettings;
exports.getLocaleValues = getLocaleValues;
