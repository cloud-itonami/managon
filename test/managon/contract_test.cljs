(ns managon.contract-test
  "managon の面どうしの契約を固定する。

  managon は単一 actor の静的サイトで、業務ロジックをほとんど持たない ——
  この repo の実体は『複数の面が同じ actor について同じことを言っている』
  という合意そのものである:

    src/app.ts             edge handler（route 表・DID・fallback・描画される HTML）
    svelte/src/app.html    SvelteKit 側が配る HTML の外枠
    wrangler.jsonc         配備（main / routes / APP_* vars）
    kotodama.jsonld        actor identity 文書（@id / nanoid / capabilities）
    package.json           version
    CLAUDE.md              宣言された endpoint と『開示規則(CRITICAL)』

  どの面も他の面を import していないので、片方だけ直した drift は throw しない
  —— identity 文書と worker が別の DID を名乗っても、ビルドも配備も成功し、
  did:web を解決した相手と実際に喋る相手が別人になって初めて分かる。

  **開示規則をここが見る理由。** このページは実在の個人（三重弁護士会所属の
  弁護士）と実在の法律事務所についての、非公式な AI 生成ページである。
  repo 自身の CLAUDE.md が『Disclosure rules (CRITICAL)』として、visible な
  AI Agent 断り書き・bengo4.com の出典表示・noindex,nofollow を **MUST** と
  書いている。これを強制するコードはどこにも無く、規則は散文でしか存在して
  いなかった。散文は配備を止めない。

  抽出の床: 各抽出は見つからなければ throw する。『抽出できなかった』が
  『合意している』と同じ顔をしてはならない（superproject CLAUDE.md の 6 問）。

  ── ここが**見ていない**既知の drift（測ったが、直さずに記録する）─────────

  1. **配備される entry と、宣言された entry が違う。** `wrangler.jsonc` の
     `main` は `svelte/.svelte-kit/cloudflare/_worker.js` を指すが、
     `kotodama.jsonld` の `build.edge` は `src/app.ts`、`build.framework` は
     `ts-thin-edge`、CLAUDE.md は `ts-thin-edge (single-file Worker, no Svelte
     build)` と書く。実際 `svelte/src/routes/+page.svelte` は法律事務所の内容を
     1 文字も持たない scaffold の置き石で、`src/app.ts` だけが実ページである。
  2. `wrangler.jsonc` は**自分自身と矛盾する** —— `APP_TEMPLATE: ts-thin-edge`
     と `APP_FRAMEWORK: sveltekit-edge-bff` が同じ vars に並んでいる。
  3. `routes` に DID のホスト `managon.etzhayyim.com` が無い（nanoid ホストだけ）。
     CLAUDE.md は vanity route が在ると書く。

  1〜3 を assert しないのは、**どちらの向きが意図なのかをこの repo から決められ
  ない**ため。svelte 面には `xrpc/[...path]` の実 BFF proxy が在り、app.ts 面には
  実ページが在る —— 互いに相手が持たないものを持っている。当てずっぽうで
  `main` を書き換えると、検証できないまま配備の意味を変える。だから代わりに、
  **どちらが配備されても成り立たなければならない不変条件**（noindex）を両方の
  HTML 面に対して要求する。"
  (:require [clojure.string :as str]
            [clojure.test :refer [deftest is testing]]
            ["fs" :as fs]))

;; ─── 抽出（見つからなければ throw。沈黙して nil を返さない） ───────────

(defn- slurp-file [path] (fs/readFileSync path "utf8"))

(defn- extract-1
  "regex の group 1 を返す。当たらなければ、どのファイルの何を探していたかを
   名指しして throw する —— リファクタで形が変わったら、このテストは
   『合意が壊れた』ではなく『測れなくなった』と言って赤くなる。"
  [src re path what]
  (or (second (re-find re src))
      (throw (ex-info (str "extraction failed: " what " not found in " path)
                      {:path path :what what}))))

(defn- non-empty! [coll path what]
  (when (empty? coll)
    (throw (ex-info (str "extraction floor: 0 " what " extracted from " path
                         " — the surface moved; this is could-not-answer, not agreement")
                    {:path path :what what})))
  coll)

(defn- read-json [path]
  (js->clj (js/JSON.parse (slurp-file path)) :keywordize-keys true))

(defn- read-jsonc
  "行頭 // コメントだけ落として JSON として読む。`https://` を壊さないよう
   行単位で判定する（`//` の一括除去は URL を切る）。parse できなければ throw
   —— 読めない配備定義は『合意不明』であって『合意』ではない。"
  [path]
  (->> (str/split-lines (slurp-file path))
       (remove #(str/starts-with? (str/triml %) "//"))
       (str/join "\n")
       js/JSON.parse
       (#(js->clj % :keywordize-keys true))))

;; ─── 各面の読み取り ─────────────────────────────────────────────────────

(def app-ts (delay (slurp-file "src/app.ts")))
(def app-html (delay (slurp-file "svelte/src/app.html")))
(def claude-md (delay (slurp-file "CLAUDE.md")))
(def kotodama (delay (read-json "kotodama.jsonld")))
(def wrangler (delay (read-jsonc "wrangler.jsonc")))
(def pkg (delay (read-json "package.json")))

(def worker
  (delay
   {:actor-did       (extract-1 @app-ts #"const ACTOR_DID = \"([^\"]+)\"" "src/app.ts" "ACTOR_DID")
    :disclosure      (extract-1 @app-ts #"const DISCLOSURE =\s*\"([^\"]+)\"" "src/app.ts" "DISCLOSURE")
    :nanoid-fallback (extract-1 @app-ts #"env\.APP_NANOID \?\? \"([^\"]+)\"" "src/app.ts" "APP_NANOID fallback")
    :version-fallback (extract-1 @app-ts #"env\.APP_VERSION \?\? \"([^\"]+)\"" "src/app.ts" "APP_VERSION fallback")
    :display-name    (extract-1 @app-ts #"displayName: \"([^\"]+)\"" "src/app.ts" "displayName in /_app/meta")
    :source-data     (extract-1 @app-ts #"sourceData: \"([^\"]+)\"" "src/app.ts" "sourceData in /_app/meta")
    :paths           (->> (re-seq #"url\.pathname === \"([^\"]+)\"" @app-ts)
                          (map second)
                          (#(non-empty! % "src/app.ts" "handled pathnames"))
                          set)}))

(def home-html
  (delay (extract-1 @app-ts #"function renderHome\(\): string \{\s*return `([\s\S]*)`;" 
                    "src/app.ts" "renderHome template literal")))

;; ─── 不変条件 ───────────────────────────────────────────────────────────

(deftest disclosure-and-noindex-hold-on-every-html-surface
  ;; CLAUDE.md の `Disclosure rules (CRITICAL)`。実在の個人についての非公式な
  ;; AI 生成ページなので、断り書きと noindex は装飾ではなく境界である。
  (testing "DISCLOSURE は AI Agent であることと非公式であることを実際に述べている"
    (let [d (:disclosure @worker)]
      (is (str/includes? d "AI Agent")
          "断り書きが AI Agent と名乗っていない — 生成物であることが読者に伝わらない")
      (is (str/includes? d "unofficial")
          "断り書きが unofficial と言っていない — 事務所の公式サイトと誤読される")
      (is (str/includes? d "Masatoshi Manago")
          "断り書きが対象の実在人物を名指ししていない — 誰についての非公式ページか曖昧になる")))
  (testing "断り書きは画面に見える要素として描画される（コメントや meta だけではない）"
    (is (re-find #"<div class=\"disclosure\">[\s\S]{0,400}\$\{DISCLOSURE\}" @home-html)
        "visible な disclosure バナーが renderHome に無い — CLAUDE.md の MUST が守られていない"))
  (testing "/_app/meta も同じ断り書きを返す（CLAUDE.md の smoke がそう書いている）"
    (is (re-find #"disclaimer: DISCLOSURE" @app-ts)
        "/_app/meta に disclaimer が無い — API 経由の消費者には断り書きが届かない"))
  (testing "出典（bengo4.com の掲載ページ）がページ本文に表示されている"
    (is (str/includes? @home-html (:source-data @worker))
        "本文に出典 URL が無い — 事実の出所を読者が辿れない"))
  (testing "app.ts が配る HTML は noindex,nofollow を宣言する"
    (is (re-find #"<meta name=\"robots\" content=\"noindex,nofollow\"" @home-html)
        "robots meta が無い — 実事務所の掲載と検索結果で競合する（CLAUDE.md が deliberate と書いた設定）"))
  (testing "app.ts の HTML 応答は x-robots-tag も返す（meta を読まない crawler 向け）"
    (is (re-find #"\"x-robots-tag\": \"noindex, nofollow\"" @app-ts)
        "x-robots-tag ヘッダが無い — meta を実行しない crawler には noindex が届かない"))
  (testing "SvelteKit 側の外枠も noindex,nofollow を宣言する"
    ;; wrangler.jsonc の main が指すのはこちら側のビルド成果物である（冒頭の
    ;; 既知 drift 1）。どちらが配備されても noindex であることを要求する ——
    ;; entry の向きが決まるまで、この不変条件だけは両面で成り立たせる。
    (is (re-find #"<meta name=\"robots\" content=\"noindex,nofollow\"" @app-html)
        "svelte/src/app.html に robots meta が無い — wrangler の main はこちらを配るので、実際に配備される面が索引される")))

(deftest actor-identity-agrees-across-worker-kotodama-and-wrangler
  (testing "actor DID: worker == kotodama.jsonld の @id"
    (is (= (:actor-did @worker) (get @kotodama (keyword "@id")))
        "identity 文書と実際に応答する worker が別の DID を名乗ると、DID 解決した相手と喋る相手が別人になる"))
  (testing "nanoid: kotodama == wrangler APP_NANOID == worker fallback"
    (let [n (:nanoid @kotodama)]
      (is (= n (get-in @wrangler [:vars :APP_NANOID])))
      (is (= n (:nanoid-fallback @worker))
          "worker の fallback が identity の nanoid とずれると、vars 未設定の環境で別 actor を名乗る")))
  (testing "worker 名は kotodama-<nanoid> 規約"
    (is (= (str "kotodama-" (:nanoid @kotodama)) (:name @wrangler))
        "外れると kotodama fleet の逆引きから消える"))
  (testing "DID のホストは nanoid ホストと同じ zone に属する"
    (let [actor-host (str/replace (:actor-did @worker) #"^did:web:" "")]
      (is (str/ends-with? actor-host ".etzhayyim.com")
          "actor DID が etzhayyim.com の外を指している — 配備 zone と identity が別ドメインになる"))))

(deftest declared-version-agrees-across-package-wrangler-and-kotodama
  (let [v (:version @pkg)]
    (is (seq v) "package.json に version が無い")
    (is (= v (:version @kotodama))
        "identity 文書の version が package.json とずれると、discovery した版と実体が食い違う")
    (is (= v (get-in @wrangler [:vars :APP_VERSION]))
        "配備 vars の version がずれると、/_app/meta が嘘の版を返す")
    (is (= v (:version-fallback @worker))
        "worker の fallback がずれると、vars 未設定の環境だけ別の版を名乗る")))

(deftest declared-capabilities-agree-between-kotodama-and-wrangler
  (let [jsonld-caps (set (get-in @kotodama [:profile :capabilities]))
        wrangler-caps (set (js->clj (js/JSON.parse (get-in @wrangler [:vars :APP_CAPABILITIES]))))]
    (is (seq jsonld-caps) "kotodama.jsonld が capability を 1 つも名乗っていない")
    (is (= jsonld-caps wrangler-caps)
        "identity 文書と配備 vars で capability 表が食い違うと、discovery した能力と runtime が名乗る能力が別物になる")))

(deftest display-name-and-description-agree-across-surfaces
  (testing "displayName: worker の /_app/meta == wrangler == kotodama"
    (let [d (get-in @kotodama [:profile :displayName])]
      (is (= d (get-in @wrangler [:vars :APP_DISPLAY_NAME])))
      (is (= d (:display-name @worker))
          "worker が identity と別の表示名を返すと、一覧に出る名前と開いた先の名前が違う")))
  (testing "description: wrangler == kotodama"
    (is (= (get-in @kotodama [:profile :description])
           (get-in @wrangler [:vars :APP_DESCRIPTION])))))

(deftest served-paths-are-exactly-the-documented-endpoints
  ;; CLAUDE.md の Endpoints 行が正本。worker が黙って口を増やすと、
  ;; 文書化されていない面が配備される。
  (testing "worker が扱う path は /health /_app/meta / /index.html のちょうど 4 つ"
    (is (= #{"/health" "/_app/meta" "/" "/index.html"} (:paths @worker))))
  (testing "CLAUDE.md の Endpoints 行がその 3 endpoint を名乗っている"
    (let [row (extract-1 @claude-md #"\| Endpoints \| ([^|]+)\|" "CLAUDE.md" "Endpoints row")]
      (doseq [p ["/health" "/_app/meta"]]
        (is (str/includes? row p)
            (str "CLAUDE.md の Endpoints 行が " p " を載せていない")))))
  (testing "未知の path は 404 を返す（既定で通さない）"
    (is (re-find #"return new Response\(\"Not Found\", \{\s*status: 404" @app-ts)
        "fall-through が 404 でない — 未定義の path が別の応答を得る")))

(deftest source-attribution-is-one-url-everywhere
  (let [u (:source-data @worker)]
    (is (str/starts-with? u "https://www.bengo4.com/")
        "出典が bengo4.com の掲載ページでない")
    (is (str/includes? @claude-md u)
        "CLAUDE.md が引用する出典 URL と worker が返す sourceData が違う — どちらが本当の出所か決まらない")
    (is (str/includes? @home-html u)
        "本文に出る出典 URL と /_app/meta の sourceData が違う")))
