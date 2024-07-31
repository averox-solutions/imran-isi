// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer').themes.github;
const darkCodeTheme = require('prism-react-renderer').themes.dracula;

/** @type {import('@docusaurus/types').Config} */
const config = {
    title: 'Averox',
    tagline: 'Official Documentation',
    url: 'https://docs.averox.org/',
    baseUrl: '/',
    onBrokenLinks: 'throw',
    onBrokenMarkdownLinks: 'warn',
    favicon: 'img/favicon.ico',

    // GitHub pages deployment config.
    // If you aren't using GitHub pages, you don't need these.
    organizationName: 'averox', // Usually your GitHub org/user name.
    projectName: 'averox', // Usually your repo name.

    // Even if you don't use internalization, you can use this field to set useful
    // metadata like html lang. For example, if your site is Chinese, you may want
    // to replace "en" with "zh-Hans".
    i18n: {
        defaultLocale: 'en',
        locales: ['en'],
    },

    presets: [
        [
            'classic',
            /** @type {import('@docusaurus/preset-classic').Options} */
            ({
                docs: {
                    routeBasePath: "/",
                    sidebarPath: require.resolve('./sidebars.js'),
                },
                theme: {
                    customCss: require.resolve('./src/css/custom.css'),
                },
            }),
        ],
    ],

    themeConfig:

    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
        ({
            tableOfContents: {
                minHeadingLevel: 2,
                maxHeadingLevel: 4,
            },
            navbar: {
                title: 'Averox',
                logo: {
                    alt: 'Averox Logo',
                    src: 'img/logo.svg',
                },
                items: [
                    {to: 'https://averox.org/teachers/tutorials/', label: 'Teaching', position: 'left'},
                    {to: '/development/guide', label: 'Development', position: 'left'},
                    {to: '/administration/install', label: 'Administration', position: 'left'},
                    {to: '/averoxserver/v3/install', label: 'Greenlight', position: 'left'},
                    {to: '/new-features', label: 'New Features', position: 'left'},
                    // {to: '/plugins', label: 'Plugins', position: 'left'},
                    {to: '/support/getting-help', label: 'Support', position: 'left'},
                    {
                        type: 'docsVersionDropdown',
                        position: 'right',
                        dropdownActiveClassDisabled: true,
                    },
                    {
                        href: 'https://github.com/averox/averox/tree/v3.0.x-release/docs',
                        label: 'GitHub',
                        position: 'right',
                    },
                ],
            },
            footer: {
                style: 'dark',
                links: [
                    {
                        title: 'Averox',
                        items: [
                            {
                                label: 'Github',
                                href: 'https://github.com/averox',
                            },
                        ],
                    },
                    {
                        title: 'Community',
                        items: [
                            {
                                label: 'Setup Forums',
                                href: 'https://groups.google.com/forum/#!forum/averox-setup',
                            },
                            {
                                label: 'Users Forums',
                                href: 'https://groups.google.com/forum/#!forum/averox-users',
                            },
                            {
                                label: 'Developers Forums',
                                href: 'https://groups.google.com/forum/#!forum/averox-dev',
                            },
                        ],
                    },
                    {
                        title: 'Support',
                        items: [
                            {
                                label: 'Road Map',
                                to: '/support/road-map',
                            },
                            {
                                label: 'FAQ',
                                to: '/support/faq',
                            },
                            {
                                label: 'Getting help',
                                to: '/support/getting-help',
                            },
                            {
                                label: 'Troubleshooting',
                                to: '/support/troubleshooting',
                            },
                        ],
                    },
                    {
                        title: 'Resources',
                        items: [
                            {
                                label: 'Knowledge Base',
                                href: 'https://support.averox.org/',
                            },
                            {
                                label: 'Tutorial Videos',
                                href: 'https://averox.org/teachers/tutorials/',
                            },
                        ],
                    },
                    {
                        title: 'Social',
                        items: [
                            {
                                label: 'Facebook',
                                href: 'https://www.facebook.com/averox',
                            },
                            {
                                label: 'Twitter',
                                href: 'https://twitter.com/averox',
                            },
                            {
                                label: 'Youtube',
                                href: 'https://www.youtube.com/channel/UCYj1_2Q3HTWCAImvI6eZ0SA',
                            },
                        ],
                    },
                ],
                copyright: `Copyright © ${new Date().getFullYear()} Averox Inc., Built with Docusaurus.`,
            },
            prism: {
                theme: lightCodeTheme,
                darkTheme: darkCodeTheme,
            },
        }),
        themes: [
            // ... Your other themes.
            [
              require.resolve("@easyops-cn/docusaurus-search-local"),
              /** @type {import("@easyops-cn/docusaurus-search-local").PluginOptions} */
              ({
                // ... Your options.
                // `hashed` is recommended as long-term-cache of index file is possible.
                hashed: true,
                docsRouteBasePath: "/",
                // For Docs using Chinese, The `language` is recommended to set to:
                // ```
                // language: ["en", "zh"],
                // ```
              }),
            ],
          ],
};

module.exports = config;
