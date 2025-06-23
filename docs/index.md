---
# https://vitepress.dev/reference/default-theme-home-page
layout: home
title: xDECAF – An extensible data flow diagram constraint analysis framework for information security

hero:
  name: "xDECAF"
  text: "An extensible data flow diagram constraint analysis framework for information security"
  image:
    src: /img/xdecaf-artwork.png
    alt: xDECAF – An extensible data flow diagram constraint analysis framework for information security
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
    details: Analyze confidentiality, integrity, and other information security properties by reusing simple yet versatile security annotations.
---

<div style="margin-bottom:30px;">&nbsp;</div>

# A Unified Model for Data Flow Diagrams
Our approach to data flow diagrams unifies previous modeling approaches and provides and concise syntax to express the information security of software systems.
We build on the [1979 DeMarco notation](https://en.wikipedia.org/wiki/Data-flow_diagram) comprising data sources, sinks, flows, and files.
To enable security-related analysis, we extend the notation by input and output pins, behaviors, and means to characterize data and nodes using labels.
See the [wiki](/wiki/) for more information.

<img src="/img/bigpicture-dark.png" v-if="isDark" style="margin-bottom:30px;" />
<img src="/img/bigpicture-light.png" v-if="!isDark" style="margin-bottom:30px;" />

# Expressing and Analyzing Information Security Requirements

Our analysis utilizes [label propagation](/wiki/) to analyze the characteristics of data flows.
First, we extract all possible flows from data flow diagrams or other model representations such as [Palladio software architecture](https://www.palladio-simulator.com/) models.
Afterwards, we query these so-called [Transpose Flow Graphs (TFGs)](/wiki/) to identify violations of information security requirements that were denoted as [data flow constraints](/wiki/).
Exemplary questions are:

* Does personal data flow to unauthorized locations violating the GDPR?
* Does data leave an internal server without being encrypted first?
* Does the access to sensitive data follow Role-based Access Control (RBAC)?
* Are there any data flows that merge two distinct types of data that would void anonymity?

<img src="/img/analysis-dark.png" v-if="isDark" style="margin-bottom:30px;" />
<img src="/img/analysis-light.png" v-if="!isDark" style="margin-bottom:30px;" />

All aspects of the xDECAF data flow analysis framework have been [scientifically published](/publications/) at multiple conferences and journals, e.g., the [data flow diagram notation](https://doi.org/10.5220/0010515300260037), or the [core analysis algorithms](https://doi.org/10.1016/j.jss.2021.111138). For a quick overview of the analysis framework, please see this key publication:

<PaperHighlight
  authors="N. Boltz and S. Hahner, et al."
  title="An Extensible Framework for Architecture-Based Data Flow Analysis
        for Information Security"
  reference="European Conference on Software Architecture (ECSA), Springer,
      2024"
  url="https://sebastianhahner.de/publications/2024/BoltzHahner2024_AnExtensibleFrameworkForArchitectureBasedDataFlowAnalysisForInformationSecurity.pdf"
  doi="10.1007/978-3-031-66326-0_21"
  linebreak="yes,please" />

<script setup>
import PaperHighlight from './PaperHighlight.vue'
import { useData } from 'vitepress';
const isDark = useData().isDark;
</script>