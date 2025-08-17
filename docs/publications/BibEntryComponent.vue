<template>
  <li>
    {{ formatAuthors(props.entry.author) }}, "<a :href="url">{{
      entry.title
    }}</a
    >", {{ entry["container-title"] }}, {{ entry.publisher }},
    {{ entry.issued["date-parts"][0][0]
    }}<span v-if="entry.DOI"
      >, doi:
      <a :href="`https://doi.org/${entry.DOI}`">{{ entry.DOI }}</a></span
    >
  </li>
</template>

<script lang="ts" setup>
import { BibEntry, Author } from './BibEntry';

const props = defineProps({
  entry: {
    type: Object as () => BibEntry,
    required: true,
  },
});

function formatAuthors(authors: Author[]): string {
  const formatted = authors
    .slice(0, 3)
    .map((author) => {
      return `${author.given.charAt(0)}. ${author.family}`;
    })
    .join(", ");
  return formatted + (authors.length > 3 ? `, et al.` : "");
}
const url = props.entry.DOI
  ? `https://doi.org/${props.entry.DOI}`
  : props.entry.URL;


</script>
