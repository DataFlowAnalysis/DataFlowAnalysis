import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "KDFDA - Karlsruhe Data Flow Diagram Analysis",
  description: "An extensible framework for data flow analysis",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Getting started', link: '/wiki/wiki.md'},
      { text: 'Documentation', link: '/wiki/wiki.md'},
      { text: 'Examples', link: '/examples/examples.md'}
    ],

    sidebar: [
      {
        text: 'Wiki',
        items: [
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/vuejs/vitepress' }
    ]
  }
})
