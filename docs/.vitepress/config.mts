import { withMermaid } from "vitepress-plugin-mermaid";

// https://vitepress.dev/reference/site-config
export default withMermaid({
  title: "DFA – The Karlsruhe Data Flow Diagram Analysis",
  description: "An extensible framework for data flow analysis",
  themeConfig: {
    // https://vitepress.dev/reference/default-theme-config
    nav: [
      { text: "Home", link: "/" },
      { text: "Getting started", link: "/wiki/quick-start" },
      { text: "Documentation", link: "/wiki/wiki.md" },
      { text: "Examples", link: "/examples/examples.md" },
    ],

    logo: "/dataflowanalysis-logo.png",
    siteTitle: "DFA - The Karlsruhe Data Flow Diagram Analysis",

    sidebar: [
      {
        text: "Wiki",
        link: "/wiki/wiki",
        items: [
          { text: "Quick Start Guide", link: "/wiki/quick-start" },
          {
            text: "DFA Web Editor",
            link: "/wiki/webeditor/intro",
            items: [
              { text: "Node Behavior", link: "/wiki/webeditor/assignments" },
            ],
          },
          {
            text: "Writing Analysis Constraints",
            link: "/wiki/dsl/intro",
            collapsed: true,
            items: [
              { text: "Source Selectors", link: "/wiki/dsl/source" },
              { text: "Destination Selectors", link: "/wiki/dsl/destination" },
              { text: "Variables", link: "/wiki/dsl/variables" },
              { text: "Conditional Selectors", link: "/wiki/dsl/conditional" },
            ],
          },
          { text: "Data Flow Diagrams (DFDs)", link: "/wiki/dfd/intro" },
          { text: "Palladio Component Model (PCM)", link: "/wiki/pcm/intro" },
          { text: "Command Line Interface (CLI)", link: "/wiki/cli/intro" },
          {
            text: "Developer Docs",
            items: [
              {
                text: "Development Setup in Eclipse",
                link: "/wiki/eclipse/intro",
              },
              {
                text: "Development Setup in IntelliJ",
                link: "/wiki/intellij/intro",
              },
            ],
          },
          { text: "Glossary", link: "/wiki/glossary" },
        ],
      },
    ],

    socialLinks: [
      { icon: "github", link: "https://github.com/vuejs/vitepress" },
    ],
  },
});
