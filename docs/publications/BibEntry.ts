export interface BibEntry {
  title: string;
  DOI?: string;
  URL: string;
  author: Author[];
  issued: {
    "date-parts": string[][];
  };
  publisher: string;
  "container-title": string;
}

export interface Author {
  family: string;
  given: string;
}