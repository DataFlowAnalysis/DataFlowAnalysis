---
prev: false
next: false
aside: false
---

# Publications

This page lists selected publications that present the foundation, application, and extension of the Karlsruhe Data Flow Diagram Analysis.
The extensible analysis framework has been presented in this premiere publication:

<PaperHighlight
  authors="N. Boltz and S. Hahner, et al."
  title="An Extensible Framework for Architecture-Based Data Flow Analysis
        for Information Security"
  reference="European Conference on Software Architecture (ECSA), Springer,
      2024"
  url="https://sebastianhahner.de/publications/2024/BoltzHahner2024_AnExtensibleFrameworkForArchitectureBasedDataFlowAnalysisForInformationSecurity.pdf"
  doi="10.1007/978-3-031-66326-0_21" />

## Data Flow Analysis

Further publications present various aspects of the analysis framework, e.g., the analysis algorithm, or the constraint formulation:

<div v-html="bibDFA"></div>

## Analysis Extensions

The data flow analysis framework has already been successfully extended to define additional analysis capabilities, e.g., to consider uncertainty or legal aspects. The following list shows a selection of projects and associated publications.

### ABUNAI – Architecture-Based and Uncertainty-Aware Confidentiality Analysis

ABUNAI supports the modeling and analysis of uncertainty and its impact on confidentiality.
By combining data flow analysis with architecture-based uncertainty propagation, predictions can be made on the interaction of uncertainty and confidentiality.
For further information, please visits [abunai.dev](https://abunai.dev).

<div v-html="bibABUNAI"></div>

### MDPA – Model-Based Data Protection Assessments

MDPA enables the model-based assessment of data protection. 
By incorporating legal information from the GDPR, experts can make statements about data privacy from a software architectural viewpoint.
For further information, please visits [github.com/Model-Based-Data-Protection-Assessments](https://github.com/Model-Based-Data-Protection-Assessments).

<div v-html="bibMDPA"></div>

### ARCoViA – Automated Repair of Confidentiality Violations in Software Architectures

ARCoVIA assists software architects in automatically repearing confidentiality violations in software architectures.
For further information, please visits [github.com/arcovia-dev](https://github.com/arcovia-dev).

<div v-html="bibARCOVIA"></div>

<script setup>
import PaperHighlight from '../PaperHighlight.vue'
import { ref } from 'vue';
import { parseBibFile } from "bibtex";
import { bib } from "./bib.js";

const entries = parseBibFile(bib).entries_raw;
const bibDFA = ref(filterAndFormatEntries(entries, "dfa"));
const bibABUNAI = ref(filterAndFormatEntries(entries, "abunai"));
const bibMDPA = ref(filterAndFormatEntries(entries, "mdpa"));
const bibARCOVIA = ref(filterAndFormatEntries(entries, "arcovia"));

function filterAndFormatEntries(entries, tag) {
    const filteredEntries = entries.filter(entry => entry.getFieldAsString("tag") == tag);
    const formattedEntries = filteredEntries.map(entry => formatBibEntry(entry));

    return `<ul><li>${formattedEntries.join("</li><li>")}</li></ul>`;
}

function formatBibEntry(entry) {
    const title = entry.getFieldAsString("title");
    const author = entry.getFieldAsString("author");
    const date = entry.getFieldAsString("date");

    let url = entry.getFieldAsString("url");
    const doi = entry.getFieldAsString("doi");

    if(!url) {
        if(doi) {
            url = `https://doi.org/${doi}`;
        } else {
            url = "#";
        }
    }

    let venue = "";
    if(entry.type == "inproceedings") {
        venue = `${entry.getFieldAsString("booktitle")}, ${entry.getFieldAsString("publisher")}`
    } else if (entry.type == "article") {
        venue = `${entry.getFieldAsString("journaltitle")}, ${entry.getFieldAsString("publisher")}`
    } else if (entry.type == "misc") {
        venue = entry.getFieldAsString("publisher");
    } else if (entry.type == "thesis") {
        venue = `${entry.getFieldAsString("institution")}, ${entry.getFieldAsString("type")}`
    }

    const formattedAuthorList = formatBibtexAuthors(author);

    let formattedBibEntry = `${formattedAuthorList}, "<a href="${url}">${title}</a>", ${venue}, ${date}`;

    if(doi) {
        formattedBibEntry = formattedBibEntry + `, doi: <a href="https://doi.org/${doi}">${doi}</a>`;
    }

    return `${formattedBibEntry}.`;
}

function formatBibtexAuthors(bibtexAuthors) {
  const authors = bibtexAuthors.split(/\s+and\s+/);

  const formattedAuthors = authors.slice(0, 3).map(author => {
    const [last, first] = author.split(',').map(s => s.trim());
    const firstInitial = first ? first.charAt(0) + '.' : '';
    return `${firstInitial} ${last}`;
  });

  if (authors.length > 3) {
    formattedAuthors.push("et al.");
  }

  return formattedAuthors.join(', ');
}
</script>
