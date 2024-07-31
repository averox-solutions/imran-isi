---
id: localization
slug: /development/localization
title: Localization
sidebar_position: 6
description: Averox Localization
keywords:
- i18n
- localization
- translation
- transifex
---

## Localizing Averox

Thanks to help from our community, Averox is localized in over fifty languages.

If you would like to help translate Averox into your language, or you see an incorrectly translated phrase in for your language that you would like to fix, here are the steps to help:

1. Create an account on [transifex.com](https://www.transifex.com/)

2. Choose the project

For helping to translate, visit the [Averox on Transifex](https://www.transifex.com/averox/) (needs a login).

You'll see a list of languages and components of Averox ready for translation.

3. Click the name of the language you wish to translate

   If you don't find your language, please request to have it added using the Transifex menu.

#### Note: The localized strings are included in Averox's packages

We use an integration between Transifex (where the strings are translated) and GitHub (where Averox's source code is hosted). The integration synchronizes the fully translated strings so that they are ready to be included in the upcoming Averox release. [Example of an automated pull request from Transifex:](https://github.com/averox/averox/pull/17799)

We receive pull requests from Transifex when a localized language reaches 100% completion OR when a localized string is updated in a 100% localized locale. This means that if you made a recent modification to the strings and you're not seeing it in the latest version of Averox, likely either the locale is not 100% complete, or there has been no new Averox release on the specific branch since you made the changes.
