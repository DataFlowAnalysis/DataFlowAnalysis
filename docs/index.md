---
# https://vitepress.dev/reference/default-theme-home-page
layout: home

hero:
  name: "DFA – The Karlsruhe Data Flow Diagram Analysis"
  text: "An extensible data flow analysis framework for information security"
  image:
    src: /dfa-artwork.png
    alt: DFA – The Karlsruhe Data Flow Diagram Analysis
  actions:
    - theme: brand
      text: Download
      link: /download/
    - theme: brand
      text: Online Editor
      link: https://editor.dataflowanalysis.org
    - theme: alt
      text: Getting Started
      link: /wiki/
    - theme: alt
      text: Examples
      link: /examples/

features:
  - icon: 
      dark: img/diagram-dark.svg
      light: img/diagram-light.svg
      alt: Easy-to-Learn Notation
    title: Easy-to-Learn Notation
    details: Based on the proven data flow diagram syntax, learn the basics in minutes and start modeling immediately.
  - icon: 
      dark: img/architecture-dark.svg
      light: img/architecture-light.svg
      alt: Architecture-Based Analysis
    title: Architecture-Based Analysis
    details: Integrates with the Palladio Software Architecture Simulator to support system-level design-time security analysis.
  - icon: 
      dark: img/puzzle-dark.svg
      light: img/puzzle-light.svg
      alt: Built for Extensibility
    title: Built for Extensibility
    details: An open-source framework with a simple analysis mechanism and stable interfaces to integrate third-party diagram notations.
  - icon: 
      dark: img/security-dark.svg
      light: img/security-light.svg
      alt: Identify Security Flaws
    title: Identify Security Flaws
    details: Analyze confidentiality, integrity, and other information flow properties by reusing simple yet versatile security annotations.
---

<div style="margin-bottom:30px;">&nbsp;</div>

<img src="/img/bigpicture-dark.png" v-if="isDark" />
<img src="/img/bigpicture-light.png" v-if="!isDark" />

**TODO: Summarize the DFD syntax and show more information from https://github.com/DataFlowAnalysis, refer to central ECSA paper.**

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';

const isDark = ref(null);
let observer = null;

const setDark = () => {
  isDark.value = document.documentElement.classList.contains('dark');
};

onMounted(() => {
  setDark();
  observer = new MutationObserver(setDark);
  observer.observe(document.documentElement, {
    attributes: true,
    attributeFilter: ['class'],
  });
});

onBeforeUnmount(() => {
  observer.disconnect();
});
</script>