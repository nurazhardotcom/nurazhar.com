(ns site.fabricate.dev
  (:require [clojure.java.io :as io]
            [clojure.string :as str]
            [clojure.java.shell :refer [sh]]
            [hiccup.core :refer [html]]))

;; ─── Configuration ────────────────────────────────────────────────

(def src-dir ".")
(def out-dir "public")
(def css-dir "src/site/fabricate/dev/templates")
(def base-url "https://nurazhar.com")

;; ─── Helpers ────────────────────────────────────────────────────────

(defn slugify
  "Convert a tag string to a URL-safe slug."
  [s]
  (-> s
      str/lower-case
      (str/replace #"[^a-z0-9]+" "-")
      (str/replace #"^-|-$" "")))

(defn tag-page-slug
  "Return the filename for a tag archive page."
  [tag]
  (str "tag-" (slugify tag) ".html"))

(defn post-url
  "Full URL for a blog post."
  [slug]
  (str base-url "/" slug ".html"))

;; ─── Frontmatter Parser ────────────────────────────────────────────

(defn parse-frontmatter
  "Parse key: value frontmatter from a markdown file.
   Format: fields at top, --- separator, then markdown body."
  [file-path]
  (let [content   (slurp file-path)
        lines     (str/split-lines content)
        sep-idx   (first (keep-indexed (fn [i line] 
                                         (when (= "---" (str/trim line)) i))
                                       lines))
        meta-lines (if sep-idx 
                     (take sep-idx lines) 
                     (take-while #(re-find #"^(\w[\w\s]*?)\s*:\s*(.*)" %) lines))
        body-lines (if sep-idx 
                     (drop (inc sep-idx) lines) 
                     (drop (count meta-lines) lines))
        body       (str/join "\n" body-lines)]
    (reduce (fn [acc line]
              (if-let [[_ k v] (re-find #"^(\w[\w\s]*?)\s*:\s*(.*)" line)]
                (assoc acc (keyword (str/lower-case (str/trim k)))
                       (str/trim v))
                acc))
            {:content body
             :slug    (-> file-path io/file .getName
                           (str/replace #"\.md$" ""))}
            meta-lines)))

;; ─── Markdown to HTML ──────────────────────────────────────────────

(defn markdown->html
  "Convert markdown string to HTML using pandoc."
  [markdown-content]
  (let [result (sh "pandoc" "-f" "markdown" "-t" "html5"
                   "--no-highlight"
                   :in markdown-content)]
    (if (= 0 (:exit result))
      (str/trim (:out result))
      (do
        (println "⚠️  Pandoc error:" (:err result))
        (str "<p>" (str/escape markdown-content {\< "&lt;" \> "&gt;" \& "&amp;"}) "</p>")))))

;; ─── Diagram Compilation ────────────────────────────────────────────

(defn compile-d2-diagram [slug idx d2-code]
  (let [diagrams-dir (io/file out-dir "diagrams")
        _ (.mkdirs diagrams-dir)
        filename (str slug "-diagram-" idx "-" (Math/abs (.hashCode d2-code)) ".svg")
        file (io/file diagrams-dir filename)]
    (when-not (.exists file)
      (let [escaped-d2 (str/replace d2-code #"(?<!\\)\$" "\\\\\\$")
            result (sh "d2" "--theme" "0" "--dark-theme" "0" "-l" "dagre" "-" (.getAbsolutePath file)
                       :in escaped-d2)]
        (when-not (= 0 (:exit result))
          (println "⚠️  D2 compilation failed for" filename ":" (:err result)))))
    (if (.exists file)
      (str "diagrams/" filename)
      nil)))

(defn process-d2-blocks [slug markdown-content]
  (if-not markdown-content
    ""
    (let [pattern #"```d2\s*([\s\S]*?)\n```"
          matches (re-seq pattern markdown-content)]
      (loop [content markdown-content
             idx 1
             [[full-match d2-code] & remaining] matches]
        (if-not full-match
          content
          (let [svg-path (compile-d2-diagram slug idx d2-code)
                replacement (if svg-path
                              (str "\n\n<div class=\"d2-diagram\"><img src=\"" svg-path "\" alt=\"Architecture Diagram\" /></div>\n\n")
                              "")]
            (recur (str/replace-first content full-match replacement)
                   (inc idx)
                   remaining)))))))

;; ─── Shared Head Snippets ───────────────────────────────────────────

(defn common-head
  "Common <head> elements shared between index and post pages."
  [title description url]
  [:head
   [:meta {:charset "UTF-8"}]
   [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
   [:title title]
   [:meta {:name "description" :content description}]
   [:link {:rel "canonical" :href url}]
   ;; Google Fonts Preconnect and loading (Eliminating CSS @import render-blocking chain)
   [:link {:rel "preconnect" :href "https://fonts.googleapis.com"}]
   [:link {:rel "preconnect" :href "https://fonts.gstatic.com" :crossorigin "anonymous"}]
   [:link {:rel "stylesheet" :href "https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=JetBrains+Mono:wght@400;500&display=swap"}]
   ;; Open Graph
   [:meta {:property "og:title" :content title}]
   [:meta {:property "og:description" :content description}]
   [:meta {:property "og:url" :content url}]
   [:meta {:property "og:type" :content "website"}]
   [:meta {:property "og:site_name" :content "nurazhar.com"}]
   [:meta {:property "og:image" :content (str base-url "/assets/og-cover.png")}]
   ;; Twitter Card
   [:meta {:name "twitter:card" :content "summary_large_image"}]
   [:meta {:name "twitter:title" :content title}]
   [:meta {:name "twitter:description" :content description}]
   [:meta {:name "twitter:image" :content (str base-url "/assets/og-cover.png")}]
   ;; CSS
   [:link {:rel "stylesheet" :href "styles.css"}]
   [:link {:rel "stylesheet" :href "d2-mobile.css"}]
   ;; Immediate Theme initialization script (Eliminates FOUC)
   [:script "var t=localStorage.getItem('theme')||(window.matchMedia('(prefers-color-scheme:dark)').matches?'dark':'light');document.documentElement.setAttribute('data-theme',t)"]
   ;; RSS
   [:link {:rel "alternate" :type "application/rss+xml" :title "nurazhar.com RSS Feed" :href (str base-url "/feed.xml")}]])

(defn escape-json-str
  "Escape a string for JSON."
  [s]
  (str/replace s "\"" "\\\""))

(defn json-ld-script
  "Generate a JSON-LD script tag from a map.
   Handles nested maps and sequences, stringifies all keys."
  [m]
  (letfn [(json-str [x]
            (cond
              (string? x) (str "\"" (escape-json-str x) "\"")
              (number? x) (str x)
              (boolean? x) (str x)
              (map? x) (let [pairs (for [[k v] x]
                                     (str "\"" (name k) "\":" (json-str v)))]
                         (str "{" (str/join "," pairs) "}"))
              (sequential? x) (str "[" (str/join "," (map json-str x)) "]")
              :else (str "\"" x "\"")))]
    (str "<script type=\"application/ld+json\">"
         (json-str (clojure.walk/stringify-keys m))
         "</script>")))

;; ─── Hiccup Template: Single Post ──────────────────────────────────

(defn render-post-html
  "Render a single blog post page as HTML."
  [{:keys [title date tags description content content-html slug] :as post}]
  (let [processed-content (process-d2-blocks slug content)
        body-html (or content-html (markdown->html processed-content))
        tag-list (when tags (map str/trim (str/split tags #",")))
        url (post-url slug)
        page-title (str title " — nurazhar.com")
        json-ld-data {"@context" "https://schema.org"
                      "@type" "Article"
                      "headline" title
                      "description" description
                      "datePublished" date
                      "author" [{"@type" "Person" "name" "Nur Azhar" "url" (str base-url "/")}]
                      "url" url}
        page-html (html
                    [:html {:lang "en"}
                     (common-head page-title description url)
                     ;; JSON-LD
                     (json-ld-script json-ld-data)
                     [:body
                      [:nav {:class "nav"}
                       [:a {:href "index.html" :class "nav-link"} "← Home"]
                       [:div {:style "display: flex; gap: 12px; align-items: center;"}
                        [:button {:class "theme-toggle" :id "theme-toggle" :onclick "toggleTheme()"
                                  :title "Toggle dark mode"
                                  :aria-label "Toggle theme"
                                  :aria-pressed "false"}
                         [:span {:class "icon-sun"} "Light"]
                         [:span {:class "icon-moon"} "Dark"]]
                        [:a {:href "https://www.linkedin.com/in/nur-azhar" :target "_blank" :rel "noopener noreferrer" :class "gitlab-link" :title "LinkedIn"}
                         "LinkedIn ↗"]
                        [:a {:href "https://gitlab.com/nurazhar" :target "_blank" :rel "noopener noreferrer" :class "gitlab-link" :title "GitLab"}
                         "GitLab ↗"]]]
                      [:article {:class "post"}
                       [:header {:class "post-header"}
                        [:h1 {:class "post-title"} title]
                        [:div {:class "post-meta"}
                         [:time {:datetime date} date]
                         (when (seq tag-list)
                           [:span {:class "post-tags"}
                            (for [t tag-list]
                              [:a {:href (tag-page-slug t) :class "post-tag"} t])])]]
                       [:div {:class "post-content"} "%%RAW_CONTENT%%"]]
                      [:footer {:class "post-footer"}
                       [:p "© 2026 nurazhar.com"]]
                      [:script "function toggleTheme(){var e=document.documentElement.getAttribute('data-theme');var n=e==='dark'?'light':'dark';document.documentElement.setAttribute('data-theme',n);localStorage.setItem('theme',n);updateThemeToggleAria(n)}function updateThemeToggleAria(t){var btn=document.getElementById('theme-toggle');if(btn){btn.setAttribute('aria-pressed',t==='dark'?'true':'false');btn.setAttribute('aria-label',t==='dark'?'Switch to light theme':'Switch to dark theme')}}updateThemeToggleAria(document.documentElement.getAttribute('data-theme'));"]]])]
    (let [[before after] (str/split page-html #"%%RAW_CONTENT%%" 2)]
      (str before body-html after))))

;; ─── Hiccup Template: Index Page ───────────────────────────────────

(defn render-index-html
  "Render the blog index page listing all posts."
  [posts]
  (let [title "Nur Azhar — Blog"
        description "Systems Automation, Identity Governance, and Security Infrastructure."
        url base-url
        json-ld-data [{"@context" "https://schema.org"
                       "@type" "Person"
                       "name" "Nur Azhar"
                       "url" url
                       "sameAs" ["https://gitlab.com/nurazhar" "https://www.linkedin.com/in/nur-azhar"]}
                      {"@context" "https://schema.org"
                       "@type" "Blog"
                       "name" "Nur Azhar — Blog"
                       "description" description
                        "url" url}]]
    (html
     [:html {:lang "en"}
      (common-head title description url)
      (json-ld-script json-ld-data)
      [:body
       [:nav {:class "nav"}
        [:div {:class "nav-left"}
         [:h1 "Nur Azhar"]
         [:p {:class "tagline"} "Systems Automation, Identity Governance, and Security Infrastructure."]]
        [:div {:style "display: flex; gap: 12px; align-items: center;"}
         [:button {:class "theme-toggle" :id "theme-toggle" :onclick "toggleTheme()"
                   :title "Toggle dark mode"
                   :aria-label "Toggle theme"
                   :aria-pressed "false"}
          [:span {:class "icon-sun"} "Light"]
          [:span {:class "icon-moon"} "Dark"]]
         [:a {:href "https://www.linkedin.com/in/nur-azhar" :target "_blank" :rel "noopener noreferrer" :class "gitlab-link" :title "LinkedIn"}
          "LinkedIn ↗"]
         [:a {:href "https://gitlab.com/nurazhar" :target "_blank" :rel "noopener noreferrer" :class "gitlab-link" :title "GitLab"}
          "GitLab ↗"]]]
       [:main {:class "post-list"}
        (for [{:keys [title date description slug]} posts]
          [:a {:href (str slug ".html") :class "post-card"}
           [:h2 {:class "post-card-title"} title]
           [:div {:class "post-card-meta"}
            [:time {:datetime date} date]]
           (when description
             [:p {:class "post-card-description"} description])])]
       [:footer {:class "site-footer"}
        [:p "© 2026 nurazhar.com"]]
       [:script "function toggleTheme(){var e=document.documentElement.getAttribute('data-theme');var n=e==='dark'?'light':'dark';document.documentElement.setAttribute('data-theme',n);localStorage.setItem('theme',n);updateThemeToggleAria(n)}function updateThemeToggleAria(t){var btn=document.getElementById('theme-toggle');if(btn){btn.setAttribute('aria-pressed',t==='dark'?'true':'false');btn.setAttribute('aria-label',t==='dark'?'Switch to light theme':'Switch to dark theme')}}updateThemeToggleAria(document.documentElement.getAttribute('data-theme'));"]]])))

;; ─── Hiccup Template: Tag Page ─────────────────────────────────────

(defn render-tag-html
  "Render a tag archive page listing all posts with a given tag."
  [tag posts]
  (let [title (str "Tag: " tag " — nurazhar.com")
        description (str "Posts tagged with \"" tag "\"")
        url (str base-url "/" (tag-page-slug tag))
        json-ld-data {"@context" "https://schema.org"
                      "@type" "CollectionPage"
                      "name" title
                      "description" description
                      "url" url}]
    (html
     [:html {:lang "en"}
      (common-head title description url)
      (json-ld-script json-ld-data)
      [:body
       [:nav {:class "nav"}
        [:a {:href "index.html" :class "nav-link"} "← Home"]
        [:div {:style "display: flex; gap: 12px; align-items: center;"}
         [:button {:class "theme-toggle" :id "theme-toggle" :onclick "toggleTheme()"
                   :title "Toggle dark mode"
                   :aria-label "Toggle theme"
                   :aria-pressed "false"}
          [:span {:class "icon-sun"} "Light"]
          [:span {:class "icon-moon"} "Dark"]]
         [:a {:href "https://www.linkedin.com/in/nur-azhar" :target "_blank" :rel "noopener noreferrer" :class "gitlab-link" :title "LinkedIn"}
          "LinkedIn ↗"]
         [:a {:href "https://gitlab.com/nurazhar" :target "_blank" :rel "noopener noreferrer" :class "gitlab-link" :title "GitLab"}
          "GitLab ↗"]]]
       [:main {:class "post-list"}
        [:h2 {:class "tag-header"} "Posts tagged: " tag]
        (for [{:keys [title date description slug]} posts]
          [:a {:href (str slug ".html") :class "post-card"}
           [:h3 {:class "post-card-title"} title]
           [:div {:class "post-card-meta"}
            [:time {:datetime date} date]]
           (when description
             [:p {:class "post-card-description"} description])])]
       [:footer {:class "site-footer"}
        [:p "© 2026 nurazhar.com"]]
       [:script "function toggleTheme(){var e=document.documentElement.getAttribute('data-theme');var n=e==='dark'?'light':'dark';document.documentElement.setAttribute('data-theme',n);localStorage.setItem('theme',n);updateThemeToggleAria(n)}function updateThemeToggleAria(t){var btn=document.getElementById('theme-toggle');if(btn){btn.setAttribute('aria-pressed',t==='dark'?'true':'false');btn.setAttribute('aria-label',t==='dark'?'Switch to light theme':'Switch to dark theme')}}updateThemeToggleAria(document.documentElement.getAttribute('data-theme'));"]]])))

;; ─── Asset Copying ─────────────────────────────────────────────────

(defn copy-assets
  "Copy static assets (CSS) from templates dir to public/."
  []
  (when (.isDirectory (io/file css-dir))
    (doseq [f (file-seq (io/file css-dir))
            :when (.isFile f)
            :let [name (.getName f)]
            :when (str/ends-with? name ".css")]
      (io/copy f (io/file out-dir name))
      (println "   📄 Copied" name))))

;; ─── Sitemap Generator ─────────────────────────────────────────────

(defn generate-sitemap
  "Generate sitemap.xml from all posts and tag pages."
  [posts tags]
  (let [xml-header "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n"
        xml-footer "\n</urlset>"
        homepage-url (str "  <url><loc>" base-url "/</loc><priority>1.0</priority></url>\n")
        tag-urls (for [tag tags]
                   (str "  <url><loc>" base-url "/" (tag-page-slug tag) "</loc><priority>0.5</priority></url>\n"))
        post-urls (for [{:keys [slug date]} posts]
                    (str "  <url><loc>" (post-url slug) "</loc><lastmod>" date "</lastmod><priority>0.8</priority></url>\n"))
        content (str xml-header homepage-url (apply str post-urls) (apply str tag-urls) xml-footer)]
    (spit (str out-dir "/sitemap.xml") content)
    (println "   🗺️  Sitemap generated")))

;; ─── Robots.txt ────────────────────────────────────────────────────

(defn generate-robots-txt
  "Generate robots.txt."
  []
  (let [content (str "User-agent: *\n"
                     "Allow: /\n"
                     "\n"
                     "Sitemap: " base-url "/sitemap.xml\n")]
    (spit (str out-dir "/robots.txt") content)
    (println "   🤖 robots.txt generated")))

;; ─── RSS Feed Generator ────────────────────────────────────────────

(defn generate-rss-feed
  "Generate an RSS 2.0 feed."
  [posts]
  (let [now "2026-06-28"
        xml-header "<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n"
        rss-header "<rss version=\"2.0\" xmlns:atom=\"http://www.w3.org/2005/Atom\">\n<channel>\n"
        rss-header2 (str "  <title>nurazhar.com</title>\n"
                         "  <link>" base-url "</link>\n"
                         "  <description>Systems Automation, Identity Governance, and Security Infrastructure.</description>\n"
                         "  <language>en</language>\n"
                         "  <atom:link href=\"" base-url "/feed.xml\" rel=\"self\" type=\"application/rss+xml\"/>\n")
        rss-footer "</channel>\n</rss>"
        items (for [{:keys [title date description slug]} posts]
                (str "  <item>\n"
                     "    <title>" title "</title>\n"
                     "    <link>" (post-url slug) "</link>\n"
                     "    <guid>" (post-url slug) "</guid>\n"
                     "    <pubDate>" date "</pubDate>\n"
                     "    <description>" description "</description>\n"
                     "  </item>\n"))
        content (str xml-header rss-header rss-header2 (apply str items) rss-footer)]
    (spit (str out-dir "/feed.xml") content)
    (println "   📡 RSS feed generated")))

;; ─── Tag Page Generator ────────────────────────────────────────────

(defn generate-tag-pages
  "Generate one tag archive page per unique tag."
  [posts]
  (let [tag-map (reduce (fn [acc {:keys [tags slug] :as post}]
                          (if tags
                            (let [tag-list (map str/trim (str/split tags #","))]
                              (reduce (fn [a t] (update a t (fnil conj []) post)) acc tag-list))
                            acc))
                        {} posts)
        sorted-tags (sort (keys tag-map))]
    (doseq [[tag tag-posts] (sort-by first tag-map)]
      (let [out-file (str out-dir "/" (tag-page-slug tag))]
        (println "   🏷️  " tag)
        (spit out-file (render-tag-html tag (sort-by :date (comp - compare) tag-posts)))))
    (println (str "   🏷️  Generated " (count tag-map) " tag pages"))
    sorted-tags))

;; ─── Build ─────────────────────────────────────────────────────────

(defn clear-output
  "Clean and recreate the output directory."
  []
  (let [d (io/file out-dir)]
    (when (.exists d)
      (println "🧹 Clearing" out-dir "...")
      (let [all (reverse (vec (file-seq d)))]
        (doseq [f all :when (.isFile f)]
          (io/delete-file f true))
        (doseq [f all :when (and (.isDirectory f) (not= f d))]
          (io/delete-file f true))))
    (.mkdirs d)
    (println "✅ Output directory ready.")))

(defn discover-posts
  "Find all .md files in the root directory (non-recursive),
   parse their frontmatter, return sorted list."
  []
  (let [root-dir (io/file src-dir)
        md-files (->> (.listFiles root-dir)
                      (filter #(and (.isFile %)
                                    (str/ends-with? (.getName %) ".md")
                                    (not (str/starts-with? (.getName %) "."))))
                      (map #(.getAbsolutePath %)))]
    (println (str "📄 Found " (count md-files) " markdown files."))
    (->> md-files
         (map parse-frontmatter)
         (sort-by :date)
         reverse)))

(defn build
  "Main build function: discover posts, render pages, write output."
  []
  (println "🏗️  Building site with Fabricate...")
  (clear-output)

  (let [posts (discover-posts)]
    ;; Generate individual post pages
    (doseq [post posts]
      (let [out-file (str out-dir "/" (:slug post) ".html")]
        (println "   ✍️ " (:slug post))
        (spit out-file (render-post-html post))))

    ;; Generate tag archive pages
    (println "   🏷️  Generating tag pages...")
    (let [tags (generate-tag-pages posts)]

      ;; Generate index page
      (println "   📋 Generating index...")
      (spit (str out-dir "/index.html") (render-index-html posts))

      ;; Generate SEO files
      (println "   🔍 Generating SEO files...")
      (generate-sitemap posts tags)
      (generate-robots-txt)
      (generate-rss-feed posts))

    ;; Copy static assets
    (println "📋 Copying assets...")
    (copy-assets))

  (println (str "✅ Site built! Output in " out-dir "/")))

;; ─── CLI Entry Point ───────────────────────────────────────────────

(defn -main
  "Entry point for 'bb build'"
  [& args]
  (build))

;; Allow running as script: bb -f dev.clj
(when (= *file* (System/getProperty "babashka.file"))
  (build))
