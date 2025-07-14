import { defineConfig } from 'vitepress'

// https://vitepress.dev/reference/site-config
export default defineConfig({
  title: "xDECAF 😆☕",
  head: [['link', { rel: 'icon', href: '/favicon.ico' }]],
  description: "xDECAF – An extensible data flow diagram constraint analysis framework for information security",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: 'Home', link: '/' },
      { text: 'Download', link: '/download/' },
      { text: 'Publications', link: '/publications/'},
      { text: 'Documentation', link: '/wiki/'},
      { text: 'Examples', link: '/examples/'},
    ],

    sidebar: [
      {
        text: 'Quick Links',
        items: [
          {
            text: 'Download',
            link: '/download/'
          },
          {
            text: 'Publications',
            link: '/publications/'
          },
          {
            text: 'Online Editor',
            link: 'https://editor.dataflowanalysis.org'
          },
          {
            text: 'GitHub Organization',
            link: 'https://github.com/DataFlowAnalysis'
          },
          {
            text: 'Eclipse Updatesite',
            link: 'https://dataflowanalysis.github.io/updatesite/'
          },
          {
            text: 'Helmholtz RSD',
            link: 'http://helmholtz.software/software/xdecaf'
          }
        ]
      },
      {
        text: 'Documentation',
        items: [
          {
            text: 'Overview',
            link: '/wiki/'
          },
          {
            text: 'Getting Started',
            link: '/wiki/gettingstarted.md'
          }
        ]
      },
      {
        text: 'Examples',
        items: [
          {
            text: 'Overview',
            link: '/examples/'
          }
        ]
      }
    ],

    socialLinks: [
      { icon: 'github', link: 'https://github.com/DataFlowAnalysis' }
    ],

    notFound: {
      quote: "This flow has no sink. Let's go back to the source and try again."
    },

    footer: {
      message: 'xDECAF – An extensible data flow diagram constraint analysis framework for information security. <a href="https://www.kit.edu/impressum.php">Imprint</a>, <a href="https://www.kit.edu/legals.php">Legals</a>, <a href="https://www.kit.edu/privacypolicy.php">Privacy Policy</a>.',
    }
  }
})
