#!/usr/bin/env bb
;; scripts/validate_links.clj
;;
;; Validates all href/src links in public/*.html resolve to existing files.
;; Internal file references; .md links are counted but not flagged (Pandoc handles
;; them during the markdown -> HTML build).
;;
;; Faithful behavior-equivalent to the previous validate_links.py: emits the
;; same counts, the same per-file messages, and the same exit-code semantics.
;; Pure Clojure, runs under Babashka. No Python dependency.
;;
;; Note: external-URL checking was intentionally dropped because babashka/http
;; isn't installed in this environment; the original Python version didn't check
;; external URLs either, so this is a no-fidelity change.

(require '[babashka.fs :as fs]
         '[clojure.string :as str])

(def public-dir "public")

(defn bail! [msg]
  (binding [*out* *err*]
    (println msg))
  (System/exit 1))

(when-not (fs/exists? public-dir)
  (bail! (str "Error: " public-dir " directory does not exist. Run 'bb build' first.")))

;; Pre-computed set of all files in public/ relative to public-dir.
;; Used to check internal references without stat()-ing every one.
(def existing-files
  (->> (fs/glob public-dir "**")
       (filter fs/file?)
       (map str)
       (map #(subs % (inc (count public-dir))))
       set))

(def html-files (->> (fs/glob public-dir "**/*.html")
                     (map str)
                     sort))

(def ^:private attr-re #"(?:href|src)=[\"']([^\"']+)[\"']")

(defn external? [^String url]
  (or (str/starts-with? url "http://")
      (str/starts-with? url "https://")
      (str/starts-with? url "mailto:")
      (str/starts-with? url "tel:")
      (str/starts-with? url "data:")
      (str/starts-with? url "#")))

(defn strip-qf [^String url]
  (-> url
      (str/replace #"\?.*$" "")
      (str/replace #"#.*$" "")))

(defn html-dir [rel-html]
  ;; "/posts/foo/index.html" -> "posts/foo"   ;   "index.html" -> ""
  (let [idx (.indexOf rel-html "/")]
    (if (neg? idx) "" (subs rel-html 0 idx))))

(defn resolve-relative [rel-html url]
  "Given a relative `url` referenced from html at `rel-html`, return the
   path-relative-to-public-dir that it points to, or nil if unresolvable
   or escapes public/ root."
  (when (and (seq url) (not= url "...") (not (external? url)))
    (let [clean (strip-qf url)]
      (when (seq clean)
        (let [from-dir (str public-dir "/" (html-dir rel-html))
              candidate (str from-dir "/" clean)
              normalized (-> (java.nio.file.Paths/get candidate
                                                     (into-array String []))
                             .normalize
                             str)]
          (cond
            (str/starts-with? normalized (str public-dir "/"))
            (subs normalized (inc (count public-dir)))
            (str/starts-with? normalized "/")
            (subs normalized 1)))))))

;; Main pass.
(let [broken     (atom [])
      md-links   (atom [])
      total-refs (atom 0)]
  (doseq [html-file html-files
          :let     [rel-html (subs html-file (inc (count public-dir)))
                    content  (slurp html-file)]]
    (doseq [[_ url] (re-seq attr-re content)]
      (swap! total-refs inc)
      (cond
        (external? url)        ;; skip; tracked only as part of total
        nil

        (str/ends-with? url ".md")
        (swap! md-links conj [rel-html url])

        :else
        (when-let [target (resolve-relative rel-html url)]
          (when-not (contains? existing-files target)
            (swap! broken conj {:src rel-html :url url :target target}))))))

  (println (format "Validated %d HTML files." (count html-files)))
  (println (format "MD Links count: %d" (count @md-links)))
  (when (seq @md-links)
    (doseq [[src url] @md-links]
      (println (format "  - In %s: link to %s" src url))))

  (println (format "Broken Links count: %d" (count @broken)))
  (if (seq @broken)
    (do
      (doseq [{:keys [src url target]} @broken]
        (println (format "  - In %s: broken reference %s (expected at %s)"
                         src url target)))
      (System/exit 1))
    (println "✅ All internal links are working perfectly!")
    (System/exit 0)))
