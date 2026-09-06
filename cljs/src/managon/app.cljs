(ns managon.app
  "managon appview — reagent + re-frame, view built from jp-go-dds
  (デジタル庁デザインシステム) hiccup.

  Faithful port of the previous SvelteKit scaffold's status page
  (`svelte/src/routes/+page.svelte`, ~84 lines): a static display of this
  Worker's own declared surface — title / project / kind, route count +
  list, wrangler var keys, an XRPC-enabled flag, and its own source path.
  Every field below mirrors the constant `app` object `+page.svelte` held
  in its <script> block; nothing here is invented and nothing is
  simplified away. `:app/route-count` (0), `:app/routes` ([]), and
  `:app/vars` ([]) are exactly what that frozen scaffold literal declared
  at extraction time — they are NOT recomputed from the current
  `wrangler.jsonc` (which does have a route and vars). Recomputing them
  would be inventing content this migration was not asked to add.

  This scaffold status page is unrelated to `src/app.ts`'s `renderHome()`,
  a separate backend-rendered page for Minoru Law Office
  (みのる法律事務所) carrying the CLAUDE.md-mandated AI-disclosure banner
  and noindex/x-robots-tag headers. That duplication predates this
  migration and this migration does not reconcile it (out of scope: pure
  frontend-scaffold port, `src/app.ts` untouched).

  Two fields ARE updated, not simplified, to stay honest about what this
  migration itself changed:

  - `:app/relative-path` now names this file, not the deleted Svelte one.
  - `:app/xrpc?` is now false. This migration's wrangler.jsonc drops
    `main` (see that file's header comment and the repo README): neither
    Worker source in this repo calls `env.ASSETS.fetch`, so the XRPC
    route this page used to advertise as enabled no longer deploys. The
    XRPC handler itself is preserved byte-for-byte at
    `src/xrpc-dispatcher.ts` (moved, not deleted, from
    `svelte/src/routes/xrpc/[...path]/+server.ts`) — backend Worker/XRPC
    code is out of scope for a frontend migration.

  `public/index.html`'s inlined <style> and the `<meta name=\"robots\">`
  tag were produced once, at authoring time, by running this exact
  JVM-free nbb command from this directory (`cljs/`) — regenerate the
  shell the same way if jp-go-dds's core components or ext-rules change,
  or if the meta/description/title need to change:

    R=/Users/junkawasaki/github/com-junkawasaki
    D=$R/orgs/kotoba-lang/jp-go-digital-design-system
    H=$R/orgs/kotoba-lang/html
    C=$R/orgs/kotoba-lang/css
    nbb --classpath \"$D/src:$D/resources:$H/src:$C/src\" -e '
    (ns g (:require [jp-go-dds.page :as page] [\"fs\" :as fs]))
    (def css (fs/readFileSync \"'\"$D\"'/resources/jp_go_dds/dds.css\" \"utf8\"))
    (fs/writeFileSync \"public/index.html\"
      (page/->page {:title \"etzhayyim-project-managon\" :lang \"ja\"
                    :description \"managon — Cloudflare surface appview (reagent + re-frame + jp-go-dds).\"
                    :css css
                    :head [[:meta {:name \"robots\" :content \"noindex,nofollow\"}]]}
                   [:div {:id \"app\"} \"etzhayyim-project-managon loading…\"]
                   [:script {:src \"js/app.js\"}]))'

  `:lang \"ja\"` and the title (`app.name`, not `app.title`) both match
  what `svelte/src/app.html` and `+page.svelte`'s <svelte:head> declared
  before deletion. The `noindex,nofollow` meta is carried over from that
  same `svelte/src/app.html` shell (it declared it at the SvelteKit-shell
  level, not per-page) — this repo's CLAUDE.md `Disclosure rules
  (CRITICAL)` says this app must not compete with the real firm's listing
  in search results, and this scaffold page is now the one actually
  served at `/` once wrangler.jsonc's `assets.directory` points at
  `cljs/public` with no `main` — so the same policy is carried forward
  onto the new shell rather than silently dropped.

  This namespace only requires `jp-go-dds.core` — the browser bundle does
  not need `jp-go-dds.page` or `html.core` at runtime; those are JVM-only
  tools used to author the static shell once."
  (:require [reagent.dom :as rdom]
            [re-frame.core :as rf]
            [jp-go-dds.core :as dds]))

;; -- db ------------------------------------------------------------------
;;
;; Same seven facts + own source path that `+page.svelte`'s `app` const
;; held (title/project/name/kind/routeCount/routes/vars/xrpc/relativePath).

(def default-db
  {:app/title "Ai etzhayyim Project Managon"
   :app/project "etzhayyim-project-managon"
   :app/name "etzhayyim-project-managon"
   :app/kind "cloudflare surface"
   :app/route-count 0
   :app/routes []
   :app/vars []
   :app/xrpc? false
   :app/relative-path "cljs/src/managon/app.cljs"})

(rf/reg-event-db
 :initialize-db
 (fn [_ _] default-db))

(rf/reg-sub :app/title (fn [db _] (:app/title db)))
(rf/reg-sub :app/project (fn [db _] (:app/project db)))
(rf/reg-sub :app/name (fn [db _] (:app/name db)))
(rf/reg-sub :app/kind (fn [db _] (:app/kind db)))
(rf/reg-sub :app/route-count (fn [db _] (:app/route-count db)))
(rf/reg-sub :app/routes (fn [db _] (:app/routes db)))
(rf/reg-sub :app/vars (fn [db _] (:app/vars db)))
(rf/reg-sub :app/xrpc? (fn [db _] (:app/xrpc? db)))
(rf/reg-sub :app/relative-path (fn [db _] (:app/relative-path db)))

;; -- view ------------------------------------------------------------------

(defn app-view []
  (let [title         @(rf/subscribe [:app/title])
        name          @(rf/subscribe [:app/name])
        kind          @(rf/subscribe [:app/kind])
        project       @(rf/subscribe [:app/project])
        route-count   @(rf/subscribe [:app/route-count])
        routes        @(rf/subscribe [:app/routes])
        vars          @(rf/subscribe [:app/vars])
        xrpc?         @(rf/subscribe [:app/xrpc?])
        relative-path @(rf/subscribe [:app/relative-path])]
    (dds/container

     [:section {:class "dds-ext-section"}
      [:p {:class "dds-ext-lead"} (str "Cloudflare " kind)]
      (dds/heading 1 title)
      [:span {:class "dads-u-mono-16N-150"} name]]

     [:section {:class "dds-ext-section"}
      (dds/grid {:min "12rem"}
        (dds/card [:p {:class "dds-ext-lead"} "Project"] [:strong project])
        (dds/card [:p {:class "dds-ext-lead"} "Routes"] [:strong (str route-count)])
        (dds/card [:p {:class "dds-ext-lead"} "XRPC"]
                  [:strong (if xrpc? "enabled" "not configured")]))]

     [:section {:class "dds-ext-section"}
      (dds/heading 2 "Public Routes" {:size "24"})
      (if (seq routes)
        (dds/card
         (into [:ul {:class "dds-ext-stack"}]
               (map (fn [r] [:li {:class "dads-u-mono-16N-150"} r]) routes)))
        [:p {:class "dds-ext-lead"} "No public route is declared next to this app surface."])]

     [:section {:class "dds-ext-section"}
      (dds/heading 2 "Runtime Bindings" {:size "24"})
      (if (seq vars)
        (into [:div {:class "dds-ext-row"}]
              (map (fn [v] (dds/chip-label v {:color "blue"})) vars))
        [:p {:class "dds-ext-lead"} "No public vars are declared in the nearest wrangler config."])]

     [:section {:class "dds-ext-section"}
      (dds/heading 2 "Source" {:size "24"})
      [:p {:class "dads-u-mono-16N-150"} relative-path]])))

;; -- mount -------------------------------------------------------------------

(defn render []
  (rdom/render [app-view] (.getElementById js/document "app")))

(defn ^:export main []
  (rf/dispatch-sync [:initialize-db])
  (render))
