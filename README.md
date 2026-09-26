# News Explorer

A searchable news frontend built with React + Vite. Runs entirely on mock
data via `src/services/newsService.js` until a real backend is connected.

## Run it

```bash
npm install
npm run dev
```

Then open the local URL Vite prints (usually http://localhost:5173).

## Where things live

- `src/data/articles.js` — sample article corpus
- `src/utils/textAlgorithms.js` — tokenization, inverted index, TF-IDF,
  cosine similarity, Damerau-Levenshtein fuzzy matching, KMP phrase search
- `src/services/newsService.js` — the API layer every page calls; swap the
  function bodies for real `fetch()` calls to connect a backend later
- `src/pages/` — one file per route
- `src/components/` — shared UI pieces
