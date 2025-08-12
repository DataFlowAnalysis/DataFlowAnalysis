---
prev: false
next: false
aside: false
---

# Publications

This page lists selected publications that present the foundation, application, and extension of the xDECAF framework.
The extensible analysis framework has been presented in this premiere publication:

<PaperHighlight
  authors="N. Boltz and S. Hahner, et al."
  title="An Extensible Framework for Architecture-Based Data Flow Analysis
        for Information Security"
  reference="European Conference on Software Architecture (ECSA), Springer,
      2024"
  url="https://sebastianhahner.de/publications/2024/BoltzHahner2024_AnExtensibleFrameworkForArchitectureBasedDataFlowAnalysisForInformationSecurity.pdf"
  doi="10.1007/978-3-031-66326-0_21" />

## Analysis Framework

Further publications present various aspects of the analysis framework, e.g., the analysis algorithm, or the constraint formulation:

<ul>
    <BibEntryComponent v-for="e in sortEntries(xdcafBib)" :entry="e" />
</ul>

## Analysis Extensions

The data flow analysis framework has already been successfully extended to define additional analysis capabilities, e.g., to consider uncertainty or legal aspects. The following list shows a selection of projects and associated publications.

### ABUNAI – Architecture-Based and Uncertainty-Aware Confidentiality Analysis

ABUNAI supports the modeling and analysis of uncertainty and its impact on confidentiality.
By combining data flow analysis with architecture-based uncertainty propagation, predictions can be made on the interaction of uncertainty and confidentiality.
For further information, please visits [abunai.dev](https://abunai.dev).

<ul>
    <BibEntryComponent v-for="e in sortEntries(abunaiBib)" :entry="e" />
</ul>

### MDPA – Model-Based Data Protection Assessments

MDPA enables the model-based assessment of data protection. 
By incorporating legal information from the GDPR, experts can make statements about data privacy from a software architectural viewpoint.
For further information, please visits [github.com/Model-Based-Data-Protection-Assessments](https://github.com/Model-Based-Data-Protection-Assessments).

<ul>
    <BibEntryComponent v-for="e in sortEntries(mdpaBib)" :entry="e" />
</ul>

### ARCoViA – Automated Repair of Confidentiality Violations in Software Architectures

ARCoVIA assists software architects in automatically repearing confidentiality violations in software architectures.
For further information, please visits [github.com/arcovia-dev](https://github.com/arcovia-dev).

<ul>
    <BibEntryComponent v-for="e in sortEntries(arcoviaBib)" :entry="e" />
</ul>

<script setup lang="ts">
import PaperHighlight from '../PaperHighlight.vue'
import { ref } from 'vue';
import { bib } from "./bib.js";
import BibEntryComponent from './BibEntryComponent.vue'
import { BibEntry } from './BibEntry';
import xdcafBib from './bib/xdcaf.json'
import abunaiBib from './bib/abunai.json'
import mdpaBib from './bib/mdpa.json'
import arcoviaBib from './bib/arcovia.json'

function sortEntries(entries: BibEntry[]) {
    return entries.sort((a, b) => {
        const dateA = new Date(a.issued['date-parts'][0][0]);
        const dateB = new Date(b.issued['date-parts'][0][0]);
        return dateB - dateA; // Sort by date descending
    });
}
</script>
