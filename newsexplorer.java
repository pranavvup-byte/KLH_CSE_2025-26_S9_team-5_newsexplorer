
public class NewsExplorer {

    // =========================================================================
    // Hand-built dynamic array (replaces java.util.ArrayList)
    // =========================================================================
    static class SimpleList<E> {
        private Object[] data = new Object[8];
        private int size = 0;

        void add(E e) {
            if (size == data.length) {
                Object[] bigger = new Object[data.length * 2];
                System.arraycopy(data, 0, bigger, 0, size);
                data = bigger;
            }
            data[size++] = e;
        }

        @SuppressWarnings("unchecked")
        E get(int i) {
            if (i < 0 || i >= size)
                throw new IndexOutOfBoundsException("Index " + i);
            return (E) data[i];
        }

        int size() {
            return size;
        }

        boolean isEmpty() {
            return size == 0;
        }
    }

    // =========================================================================
    // Hand-built hash map, open addressing with linear probing
    // (replaces java.util.HashMap; also used as a hash-set via Boolean values)
    // =========================================================================
    static class SimpleMap<K, V> {
        private Object[] keys;
        private Object[] values;
        private boolean[] used;
        private int capacity;
        private int size;

        SimpleMap() {
            this(16);
        }

        SimpleMap(int cap) {
            capacity = cap;
            keys = new Object[capacity];
            values = new Object[capacity];
            used = new boolean[capacity];
            size = 0;
        }

        private int indexFor(Object key, int cap) {
            int h = key.hashCode();
            h = h ^ (h >>> 16);
            int m = h % cap;
            return m < 0 ? m + cap : m;
        }

        void put(K key, V value) {
            if (size >= capacity * 0.7)
                resize();
            int idx = indexFor(key, capacity);
            while (used[idx] && !keys[idx].equals(key)) {
                idx = (idx + 1) % capacity;
            }
            if (!used[idx])
                size++;
            keys[idx] = key;
            values[idx] = value;
            used[idx] = true;
        }

        @SuppressWarnings("unchecked")
        V get(K key) {
            int idx = indexFor(key, capacity);
            int start = idx;
            while (used[idx]) {
                if (keys[idx].equals(key))
                    return (V) values[idx];
                idx = (idx + 1) % capacity;
                if (idx == start)
                    break;
            }
            return null;
        }

        boolean containsKey(K key) {
            return get(key) != null;
        }

        int size() {
            return size;
        }

        @SuppressWarnings("unchecked")
        SimpleList<K> keySet() {
            SimpleList<K> list = new SimpleList<>();
            for (int i = 0; i < capacity; i++) {
                if (used[i])
                    list.add((K) keys[i]);
            }
            return list;
        }

        @SuppressWarnings("unchecked")
        private void resize() {
            Object[] oldKeys = keys;
            Object[] oldValues = values;
            boolean[] oldUsed = used;
            int oldCapacity = capacity;

            capacity *= 2;
            size = 0;
            keys = new Object[capacity];
            values = new Object[capacity];
            used = new boolean[capacity];

            for (int i = 0; i < oldCapacity; i++) {
                if (oldUsed[i])
                    put((K) oldKeys[i], (V) oldValues[i]);
            }
        }
    }

    // =========================================================================
    // Article model
    // =========================================================================
    static class Article {
        int id;
        String title, content, topic, source, url;

        Article(int id, String title, String content, String topic, String source, String url) {
            this.id = id;
            this.title = title;
            this.content = content;
            this.topic = (topic == null || topic.isEmpty()) ? "General" : topic;
            this.source = source;
            this.url = url;
        }
    }

    // =========================================================================
    // Module 2/3: String algorithms + DP
    // =========================================================================
    static class Algorithms {

        static final String[] STOPWORDS = {
                "the", "a", "an", "and", "or", "but", "of", "in", "on", "for", "to",
                "is", "are", "was", "were", "be", "with", "as", "by", "at", "from",
                "this", "that", "it", "its", "he", "she", "they", "his", "her"
        };

        static boolean isStopword(String w) {
            for (String s : STOPWORDS)
                if (s.equals(w))
                    return true;
            return false;
        }

        /**
         * Manual tokenizer (no regex): lowercase words, punctuation stripped, stopwords
         * dropped.
         */
        static SimpleList<String> tokenize(String text) {
            SimpleList<String> tokens = new SimpleList<>();
            StringBuilder current = new StringBuilder();
            for (int i = 0; i <= text.length(); i++) {
                char c = (i < text.length()) ? text.charAt(i) : ' ';
                if (Character.isLetterOrDigit(c)) {
                    current.append(Character.toLowerCase(c));
                } else {
                    if (current.length() > 1) {
                        String word = current.toString();
                        if (!isStopword(word))
                            tokens.add(word);
                    }
                    current.setLength(0);
                }
            }
            return tokens;
        }

        /**
         * Knuth-Morris-Pratt exact pattern search. Returns match start indices. O(n +
         * m).
         */
        static SimpleList<Integer> kmpSearch(String text, String pattern) {
            SimpleList<Integer> matches = new SimpleList<>();
            if (pattern.isEmpty())
                return matches;

            String t = text.toLowerCase();
            String p = pattern.toLowerCase();

            int[] failure = new int[p.length()];
            int k = 0;
            for (int i = 1; i < p.length(); i++) {
                while (k > 0 && p.charAt(i) != p.charAt(k))
                    k = failure[k - 1];
                if (p.charAt(i) == p.charAt(k))
                    k++;
                failure[i] = k;
            }

            k = 0;
            for (int i = 0; i < t.length(); i++) {
                while (k > 0 && t.charAt(i) != p.charAt(k))
                    k = failure[k - 1];
                if (t.charAt(i) == p.charAt(k))
                    k++;
                if (k == p.length()) {
                    matches.add(i - k + 1);
                    k = failure[k - 1];
                }
            }
            return matches;
        }

        /** Wagner-Fischer DP for edit distance between two strings. */
        static int levenshteinDistance(String s1, String s2) {
            int n = s1.length(), m = s2.length();
            if (n == 0)
                return m;
            if (m == 0)
                return n;

            int[] prev = new int[m + 1];
            int[] curr = new int[m + 1];
            for (int j = 0; j <= m; j++)
                prev[j] = j;

            for (int i = 1; i <= n; i++) {
                curr[0] = i;
                for (int j = 1; j <= m; j++) {
                    int cost = (s1.charAt(i - 1) == s2.charAt(j - 1)) ? 0 : 1;
                    int del = prev[j] + 1;
                    int ins = curr[j - 1] + 1;
                    int sub = prev[j - 1] + cost;
                    curr[j] = Math.min(del, Math.min(ins, sub));
                }
                int[] tmp = prev;
                prev = curr;
                curr = tmp;
            }
            return prev[m];
        }

        /**
         * Returns up to topN vocabulary terms within maxDistance of queryTerm (fuzzy
         * match).
         */
        static SimpleList<String> closestTerms(String queryTerm, SimpleList<String> vocabulary, int maxDistance,
                int topN) {
            int n = vocabulary.size();
            int[] distances = new int[n];
            for (int i = 0; i < n; i++) {
                distances[i] = levenshteinDistance(queryTerm.toLowerCase(), vocabulary.get(i));
            }

            boolean[] taken = new boolean[n];
            SimpleList<String> result = new SimpleList<>();
            int limit = Math.min(topN, n);
            for (int pick = 0; pick < limit; pick++) {
                int bestIdx = -1, bestDist = Integer.MAX_VALUE;
                for (int i = 0; i < n; i++) {
                    if (!taken[i] && distances[i] <= maxDistance && distances[i] < bestDist) {
                        bestDist = distances[i];
                        bestIdx = i;
                    }
                }
                if (bestIdx == -1)
                    break;
                taken[bestIdx] = true;
                result.add(vocabulary.get(bestIdx));
            }
            return result;
        }
    }

    // =========================================================================
    // Inverted index + TF-IDF vectors + cosine similarity
    // =========================================================================
    static class SearchIndex {
        SimpleMap<String, SimpleList<Integer>> invertedIndex = new SimpleMap<>();
        SimpleMap<Integer, SimpleMap<String, Integer>> docTermFreq = new SimpleMap<>();
        SimpleMap<Integer, SimpleMap<String, Double>> docVectors = new SimpleMap<>();
        SimpleMap<String, Boolean> vocabulary = new SimpleMap<>();
        int numDocs = 0;

        static class ScoredDoc {
            int id;
            double score;

            ScoredDoc(int id, double score) {
                this.id = id;
                this.score = score;
            }
        }

        void build(SimpleList<Article> articles) {
            invertedIndex = new SimpleMap<>();
            docTermFreq = new SimpleMap<>();
            docVectors = new SimpleMap<>();
            vocabulary = new SimpleMap<>();
            numDocs = articles.size();

            SimpleMap<String, Integer> docFreq = new SimpleMap<>(); // term -> #docs containing it

            for (int a = 0; a < articles.size(); a++) {
                Article art = articles.get(a);
                SimpleList<String> tokens = Algorithms.tokenize(art.title + " " + art.content);
                SimpleMap<String, Integer> counts = new SimpleMap<>();

                for (int i = 0; i < tokens.size(); i++) {
                    String tok = tokens.get(i);
                    Integer c = counts.get(tok);
                    counts.put(tok, c == null ? 1 : c + 1);
                    vocabulary.put(tok, true);

                    SimpleList<Integer> postings = invertedIndex.get(tok);
                    if (postings == null) {
                        postings = new SimpleList<>();
                        invertedIndex.put(tok, postings);
                    }
                    boolean already = false;
                    for (int p = 0; p < postings.size(); p++) {
                        if (postings.get(p) == art.id) {
                            already = true;
                            break;
                        }
                    }
                    if (!already)
                        postings.add(art.id);
                }
                docTermFreq.put(art.id, counts);

                SimpleList<String> termsInDoc = counts.keySet();
                for (int i = 0; i < termsInDoc.size(); i++) {
                    String term = termsInDoc.get(i);
                    Integer df = docFreq.get(term);
                    docFreq.put(term, df == null ? 1 : df + 1);
                }
            }

            SimpleList<Integer> docIds = docTermFreq.keySet();
            for (int d = 0; d < docIds.size(); d++) {
                int docId = docIds.get(d);
                SimpleMap<String, Integer> counts = docTermFreq.get(docId);
                SimpleList<String> terms = counts.keySet();

                int totalTerms = 0;
                for (int i = 0; i < terms.size(); i++)
                    totalTerms += counts.get(terms.get(i));
                if (totalTerms == 0)
                    totalTerms = 1;

                SimpleMap<String, Double> vector = new SimpleMap<>();
                for (int i = 0; i < terms.size(); i++) {
                    String term = terms.get(i);
                    int count = counts.get(term);
                    double tf = (double) count / totalTerms;
                    int df = docFreq.get(term);
                    double idf = Math.log((double) (numDocs + 1) / (df + 1)) + 1;
                    vector.put(term, tf * idf);
                }
                docVectors.put(docId, vector);
            }
        }

        static double cosineSimilarity(SimpleMap<String, Double> a, SimpleMap<String, Double> b) {
            SimpleList<String> aKeys = a.keySet();
            SimpleList<String> bKeys = b.keySet();

            double dot = 0;
            for (int i = 0; i < aKeys.size(); i++) {
                String term = aKeys.get(i);
                Double bw = b.get(term);
                if (bw != null)
                    dot += a.get(term) * bw;
            }

            double normA = 0, normB = 0;
            for (int i = 0; i < aKeys.size(); i++) {
                double w = a.get(aKeys.get(i));
                normA += w * w;
            }
            for (int i = 0; i < bKeys.size(); i++) {
                double w = b.get(bKeys.get(i));
                normB += w * w;
            }
            normA = Math.sqrt(normA);
            normB = Math.sqrt(normB);

            if (normA == 0 || normB == 0)
                return 0.0;
            return dot / (normA * normB);
        }

        SimpleMap<String, Double> queryVector(String query) {
            SimpleList<String> tokens = Algorithms.tokenize(query);
            SimpleMap<String, Integer> counts = new SimpleMap<>();
            for (int i = 0; i < tokens.size(); i++) {
                String tok = tokens.get(i);
                Integer c = counts.get(tok);
                counts.put(tok, c == null ? 1 : c + 1);
            }
            SimpleList<String> terms = counts.keySet();
            int total = 0;
            for (int i = 0; i < terms.size(); i++)
                total += counts.get(terms.get(i));
            if (total == 0)
                total = 1;

            SimpleMap<String, Double> vec = new SimpleMap<>();
            for (int i = 0; i < terms.size(); i++) {
                String term = terms.get(i);
                vec.put(term, (double) counts.get(term) / total);
            }
            return vec;
        }

        SimpleList<ScoredDoc> rankByQuery(String query, int topN) {
            SimpleMap<String, Double> qVec = queryVector(query);
            SimpleList<Integer> docIds = docVectors.keySet();
            SimpleList<ScoredDoc> all = new SimpleList<>();
            for (int i = 0; i < docIds.size(); i++) {
                int docId = docIds.get(i);
                double score = cosineSimilarity(qVec, docVectors.get(docId));
                if (score > 0)
                    all.add(new ScoredDoc(docId, score));
            }
            return topByScore(all, topN);
        }

        SimpleList<ScoredDoc> similarTo(int articleId, int topN) {
            SimpleList<ScoredDoc> all = new SimpleList<>();
            SimpleMap<String, Double> baseVec = docVectors.get(articleId);
            if (baseVec == null)
                return all;

            SimpleList<Integer> docIds = docVectors.keySet();
            for (int i = 0; i < docIds.size(); i++) {
                int docId = docIds.get(i);
                if (docId == articleId)
                    continue;
                double score = cosineSimilarity(baseVec, docVectors.get(docId));
                if (score > 0)
                    all.add(new ScoredDoc(docId, score));
            }
            return topByScore(all, topN);
        }

        /** Selection-based top-N (fine at this demo/basic-prototype scale). */
        private SimpleList<ScoredDoc> topByScore(SimpleList<ScoredDoc> list, int topN) {
            int n = list.size();
            boolean[] taken = new boolean[n];
            SimpleList<ScoredDoc> result = new SimpleList<>();
            int limit = Math.min(topN, n);
            for (int pick = 0; pick < limit; pick++) {
                int bestIdx = -1;
                double bestScore = -1;
                for (int i = 0; i < n; i++) {
                    if (!taken[i] && list.get(i).score > bestScore) {
                        bestScore = list.get(i).score;
                        bestIdx = i;
                    }
                }
                if (bestIdx == -1)
                    break;
                taken[bestIdx] = true;
                result.add(list.get(bestIdx));
            }
            return result;
        }
    }

    // =========================================================================
    // In-memory repository (Article Ingestion Pipeline).
    // Swap for JDBC + SQLite (java.sql.*) by keeping these same method names.
    // =========================================================================
    static class ArticleRepository {
        SimpleList<Article> articles = new SimpleList<>();
        int nextId = 1;

        int insert(String title, String content, String topic, String source, String url) {
            Article art = new Article(nextId, title, content, topic, source, url);
            articles.add(art);
            nextId++;
            return art.id;
        }

        SimpleList<Article> fetchAll() {
            return articles;
        }

        Article fetchById(int id) {
            for (int i = 0; i < articles.size(); i++) {
                if (articles.get(i).id == id)
                    return articles.get(i);
            }
            return null;
        }
    }

    // =========================================================================
    // Basic keyword-set auto-categorization (stand-in for a trained classifier)
    // =========================================================================
    static final String[][] TOPIC_KEYWORDS = {
            { "Technology", "ai", "software", "app", "startup", "tech", "chip", "robot", "internet", "data" },
            { "Sports", "match", "tournament", "player", "goal", "team", "league", "cricket", "football" },
            { "Business", "market", "stock", "economy", "company", "revenue", "trade", "bank", "inflation" },
            { "Health", "health", "hospital", "vaccine", "disease", "doctor", "covid", "medicine" },
            { "Politics", "election", "government", "minister", "policy", "parliament", "vote", "law" },
    };

    static String autoCategorize(String content) {
        SimpleList<String> tokens = Algorithms.tokenize(content);
        SimpleMap<String, Boolean> tokenSet = new SimpleMap<>();
        for (int i = 0; i < tokens.size(); i++)
            tokenSet.put(tokens.get(i), true);

        String bestTopic = "General";
        int bestScore = 0;
        for (String[] row : TOPIC_KEYWORDS) {
            String topic = row[0];
            int score = 0;
            for (int i = 1; i < row.length; i++) {
                if (tokenSet.containsKey(row[i]))
                    score++;
            }
            if (score > bestScore) {
                bestScore = score;
                bestTopic = topic;
            }
        }
        return bestTopic;
    }

    // =========================================================================
    // Engine: ties repository + index into the four proposal functionalities
    // =========================================================================
    static class NewsExplorerEngine {
        ArticleRepository repo = new ArticleRepository();
        SearchIndex index = new SearchIndex();

        /**
         * Article Ingestion Pipeline. Each row: {title, content, topic-or-null, source,
         * url}.
         */
        void ingest(String[][] articleRows) {
            for (String[] row : articleRows) {
                String topic = row[2];
                if (topic == null || topic.isEmpty())
                    topic = autoCategorize(row[1]);
                repo.insert(row[0], row[1], topic, row[3], row[4]);
            }
            index.build(repo.fetchAll());
        }

        /** Keyword & Topic Search (ranked by TF-IDF cosine similarity). */
        SimpleList<SearchIndex.ScoredDoc> search(String query, int topN) {
            return index.rankByQuery(query, topN);
        }

        /**
         * Fuzzy suggestions ("did you mean") for query terms missing from the
         * vocabulary.
         */
        SimpleList<String> fuzzySuggestions(String query) {
            SimpleList<String> tokens = Algorithms.tokenize(query);
            SimpleList<String> vocab = index.vocabulary.keySet();
            SimpleList<String> suggestions = new SimpleList<>();
            for (int i = 0; i < tokens.size(); i++) {
                String term = tokens.get(i);
                if (!index.vocabulary.containsKey(term)) {
                    SimpleList<String> close = Algorithms.closestTerms(term, vocab, 2, 3);
                    for (int j = 0; j < close.size(); j++)
                        suggestions.add(term + " -> " + close.get(j));
                }
            }
            return suggestions;
        }

        /** Exact phrase search inside article bodies using KMP. */
        SimpleList<Article> phraseSearch(String phrase, SimpleList<SimpleList<Integer>> matchPositionsOut) {
            SimpleList<Article> matched = new SimpleList<>();
            SimpleList<Article> all = repo.fetchAll();
            for (int i = 0; i < all.size(); i++) {
                Article art = all.get(i);
                SimpleList<Integer> positions = Algorithms.kmpSearch(art.content, phrase);
                if (positions.size() > 0) {
                    matched.add(art);
                    matchPositionsOut.add(positions);
                }
            }
            return matched;
        }

        /** Automatic Organization: group all articles by topic. */
        SimpleMap<String, SimpleList<Article>> categories() {
            SimpleMap<String, SimpleList<Article>> grouped = new SimpleMap<>();
            SimpleList<Article> all = repo.fetchAll();
            for (int i = 0; i < all.size(); i++) {
                Article art = all.get(i);
                SimpleList<Article> bucket = grouped.get(art.topic);
                if (bucket == null) {
                    bucket = new SimpleList<>();
                    grouped.put(art.topic, bucket);
                }
                bucket.add(art);
            }
            return grouped;
        }

        /** Personalized Recommendations: articles most similar to a given article. */
        SimpleList<SearchIndex.ScoredDoc> recommend(int articleId, int topN) {
            return index.similarTo(articleId, topN);
        }
    }

    // =========================================================================
    // Sample corpus (8 articles across 5 topics) for the demo
    // =========================================================================
    static String[][] sampleArticles() {
        return new String[][] {
                { "AI Startup Raises Funding for New Chip Design",
                        "A Bengaluru based AI startup announced new funding to build a custom chip for machine learning workloads. The company says its software stack will speed up data center inference tasks.",
                        null, "TechDaily", "https://example.com/ai-startup-chip" },
                { "National Cricket Team Wins Tournament Final",
                        "The national cricket team won the tournament final after a thrilling match against their rivals. The player of the match scored the winning goal in extra time to lift the trophy.",
                        null, "SportsWire", "https://example.com/cricket-final" },
                { "Stock Market Rallies as Inflation Cools",
                        "The stock market rallied today after new data showed inflation cooling faster than expected. Major companies on the exchange saw revenue estimates revised upward by analysts.",
                        null, "BizToday", "https://example.com/market-rally" },
                { "New Vaccine Shows Promise in Hospital Trials",
                        "Doctors at a major hospital reported promising results from a new vaccine trial. Health officials say the medicine could help reduce disease spread during the next flu season.",
                        null, "HealthNow", "https://example.com/vaccine-trial" },
                { "Parliament Debates New Election Law",
                        "Government ministers debated a new election law in parliament today. The proposed policy would change how citizens vote in the next general election.",
                        null, "PolicyWatch", "https://example.com/election-law" },
                { "Tech Company Unveils Robot for Warehouses",
                        "A leading tech company unveiled a new robot designed for warehouse automation. The robot uses AI software to navigate aisles and sort packages faster than manual labor.",
                        null, "TechDaily", "https://example.com/warehouse-robot" },
                { "Central Bank Holds Interest Rates Steady",
                        "The central bank held interest rates steady this week, citing stable inflation and a resilient economy. Analysts expect the bank to revisit the policy at its next meeting.",
                        null, "BizToday", "https://example.com/bank-rates" },
                { "Football League Announces Expanded Tournament Format",
                        "The football league announced an expanded tournament format for next season, adding more teams and matches. Players and coaches welcomed the change ahead of the new league calendar.",
                        null, "SportsWire", "https://example.com/league-format" },
        };
    }

    // =========================================================================
    // Demo / interactive CLI
    // =========================================================================
    static void printArticles(SimpleList<Article> articles) {
        if (articles.isEmpty()) {
            System.out.println("  (no results)");
            return;
        }
        for (int i = 0; i < articles.size(); i++) {
            Article a = articles.get(i);
            System.out.printf(
                    "  #%-2d [%s] %s  %s%n",
                    a.id,
                    a.topic,
                    a.title,
                    a.url);
        }
    }

    static void printScored(NewsExplorerEngine engine, SimpleList<SearchIndex.ScoredDoc> scored, String label) {
        if (scored.isEmpty()) {
            System.out.println("  (no results)");
            return;
        }
        for (int i = 0; i < scored.size(); i++) {
            SearchIndex.ScoredDoc sd = scored.get(i);
            Article a = engine.repo.fetchById(sd.id);
            System.out.printf("  #%-2d [%s] %s  [%s: %.4f]  %s%n", a.id, a.topic, a.title, label, sd.score, a.url);
        }
    }

    static void runDemo() {
        NewsExplorerEngine engine = new NewsExplorerEngine();
        System.out.println("Ingesting sample corpus...");
        engine.ingest(sampleArticles());

        System.out.println("\n=== Automatic Organization: articles by topic ===");
        SimpleMap<String, SimpleList<Article>> grouped = engine.categories();
        SimpleList<String> topics = grouped.keySet();
        for (int i = 0; i < topics.size(); i++) {
            String topic = topics.get(i);
            SimpleList<Article> arts = grouped.get(topic);
            System.out.println("\n" + topic + " (" + arts.size() + ")");
            printArticles(arts);
        }

        System.out.println("\n=== Keyword Search: 'ai chip startup' ===");
        printScored(engine, engine.search("ai chip startup", 10), "relevance");

        System.out.println("\n=== Fuzzy Search: 'critcket tournment' (misspelled) ===");
        printScored(engine, engine.search("critcket tournment", 10), "relevance");
        SimpleList<String> suggestions = engine.fuzzySuggestions("critcket tournment");
        if (!suggestions.isEmpty()) {
            System.out.print("  Did you mean: ");
            for (int i = 0; i < suggestions.size(); i++)
                System.out.print(suggestions.get(i) + "  ");
            System.out.println();
        }

        System.out.println("\n=== Phrase Search (KMP): 'interest rates' ===");
        SimpleList<SimpleList<Integer>> positionsOut = new SimpleList<>();
        SimpleList<Article> phraseMatches = engine.phraseSearch("interest rates", positionsOut);
        for (int i = 0; i < phraseMatches.size(); i++) {
            Article a = phraseMatches.get(i);
            SimpleList<Integer> positions = positionsOut.get(i);
            StringBuilder sb = new StringBuilder();
            for (int p = 0; p < positions.size(); p++) {
                sb.append(positions.get(p));
                if (p < positions.size() - 1)
                    sb.append(", ");
            }
            System.out.println("  #" + a.id + " " + a.title + " -> matched at char positions [" + sb + "]");
        }

        System.out.println("\n=== Recommendations for article #1 ===");
        Article base = engine.repo.fetchById(1);
        System.out.println("Base article: #" + base.id + " " + base.title);
        printScored(engine, engine.recommend(1, 5), "similarity");
    }

    static void runInteractive() {
        NewsExplorerEngine engine = new NewsExplorerEngine();
        engine.ingest(sampleArticles());

        java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(System.in));
        System.out.println("News Explorer CLI. Commands: search <query> | topics | recommend <id> | quit");
        try {
            String line;
            System.out.print("> ");
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    System.out.print("> ");
                    continue;
                }
                if (line.equals("quit") || line.equals("exit"))
                    break;

                if (line.equals("topics")) {
                    SimpleMap<String, SimpleList<Article>> grouped = engine.categories();
                    SimpleList<String> topics = grouped.keySet();
                    for (int i = 0; i < topics.size(); i++) {
                        String topic = topics.get(i);
                        System.out.println(topic + " (" + grouped.get(topic).size() + ")");
                        printArticles(grouped.get(topic));
                    }
                } else if (line.startsWith("search ")) {
                    String query = line.substring("search ".length());
                    printScored(engine, engine.search(query, 10), "relevance");
                    SimpleList<String> suggestions = engine.fuzzySuggestions(query);
                    if (!suggestions.isEmpty()) {
                        System.out.print("Did you mean: ");
                        for (int i = 0; i < suggestions.size(); i++)
                            System.out.print(suggestions.get(i) + "  ");
                        System.out.println();
                    }
                } else if (line.startsWith("recommend ")) {
                    try {
                        int id = Integer.parseInt(line.substring("recommend ".length()).trim());
                        printScored(engine, engine.recommend(id, 5), "similarity");
                    } catch (NumberFormatException e) {
                        System.out.println("Usage: recommend <article_id>");
                    }
                } else {
                    System.out.println("Unknown command. Try: search <query> | topics | recommend <id> | quit");
                }
                System.out.print("> ");
            }
        } catch (java.io.IOException e) {
            System.out.println("Input error: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length > 0 && args[0].equals("demo")) {
            runDemo();
        } else {
            runInteractive();
        }
    }
}
