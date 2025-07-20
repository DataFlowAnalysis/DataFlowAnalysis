import { withMermaid } from "vitepress-plugin-mermaid";

// https://vitepress.dev/reference/site-config
export default withMermaid({
  title: "xDECAF 😆☕",
  head: [["link", { rel: "icon", href: "/favicon.ico" }]],
  description:
    "xDECAF – An extensible data flow diagram constraint analysis framework for information security",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: "Home", link: "/" },
      { text: "Download", link: "/download/" },
      { text: "Publications", link: "/publications/" },
      { text: "Documentation", link: "/wiki/gettingstarted" },
      { text: "Examples", link: "/examples/" },
    ],

    logo: "/dataflowanalysis-logo.png",
    siteTitle: "xDECAF 😆☕",

    search: {
      provider: "local",
    },

    sidebar: [
      {
        text: "Quick Links",
        items: [
          {
            text: "Download",
            link: "/download/",
          },
          {
            text: "Publications",
            link: "/publications/",
          },
          {
            text: "Online Editor",
            link: "https://editor.dataflowanalysis.org",
          },
          {
            text: "GitHub Organization",
            link: "https://github.com/DataFlowAnalysis",
          },
          {
            text: "Eclipse Updatesite",
            link: "https://dataflowanalysis.github.io/updatesite/",
          },
          {
            text: "Helmholtz RSD",
            link: "http://helmholtz.software/software/dfa",
          },
        ],
      },
      {
        text: "Documentation",
        items: [
          { text: "Getting Started", link: "/wiki/gettingstarted" },
          {
            text: "Tooling",
            link: "/wiki/tooling",
            items: [
              {
                text: "Online Editor",
                link: "/wiki/onlineeditor/",
                items: [
                  {
                    text: "Node Behavior",
                    link: "/wiki/onlineeditor/assignments",
                  },
                  {
                    text: "Writing Analysis Constraints",
                    link: "/wiki/dsl/",
                    collapsed: true,
                    items: [
                      { text: "Source Selectors", link: "/wiki/dsl/source" },
                      {
                        text: "Destination Selectors",
                        link: "/wiki/dsl/destination",
                      },
                      { text: "Variables", link: "/wiki/dsl/variables" },
                      {
                        text: "Conditional Selectors",
                        link: "/wiki/dsl/conditional",
                      },
                    ],
                  },
                ],
              },
              {
                text: "Command Line Interface (CLI)",
                link: "/wiki/cli/",
              },
            ],
          },
          {
            text: "Knowledge",
            link: "/wiki/knowledge",
            items: [
              { text: "Data Flow Diagrams (DFDs)", link: "/wiki/dfd/" },
              { text: "Palladio Component Model (PCM)", link: "/wiki/pcm/" },
            ],
          },
          {
            text: "Developer Docs",
            link: "/wiki/developer",
            items: [
              {
                text: "Development Setup in Eclipse",
                link: "/wiki/development/eclipse",
              },
              {
                text: "Development Setup in IntelliJ",
                link: "/wiki/development/intellij",
              },
              {
                text: "Running Locally",
                link: "/wiki/development/running-locally",
              },
              {
                text: "Style Guide",
                link: "/wiki/development/style",
              },
            ],
          },
          { text: "Glossary", link: "/wiki/glossary" },
        ],
      },
      {
        text: "Examples",
        items: [
          {
            text: "Overview",
            link: "/examples/",
          },
        ],
      },
    ],

    socialLinks: [
      { icon: "github", link: "https://github.com/DataFlowAnalysis" },
    ],

    notFound: {
      quote:
        "This flow has no sink. Let's go back to the source and try again.",
    },

    footer: {
      message:
        'xDECAF – An extensible data flow diagram constraint analysis framework for information security. <a href="https://www.kit.edu/impressum.php">Imprint</a>, <a href="https://www.kit.edu/legals.php">Legals</a>, <a href="https://www.kit.edu/privacypolicy.php">Privacy Policy</a>.',
    },
  },
});
