#!/usr/bin/env nbb
;; run_tests.cljs — managon の面契約検査。
;;
;;   nbb --classpath test run_tests.cljs
;;
;; managon は単一 actor の静的サイトで、この repo の実体は『複数の面が同じ
;; actor について同じことを言っている』という合意である。どの面も他を import
;; しないので、drift は throw せずに配備まで通る。この runner は依存ゼロの
;; nbb + cljs.test で、その合意と、CLAUDE.md が CRITICAL と書いた開示規則を
;; 毎回確かめる。
;;
;; workspace 規則（superproject CLAUDE.md）: 新規の検証ハーネスは nbb で書く。
(ns run-tests
  (:require [clojure.test :as t]
            [managon.contract-test]))

(def green-marker
  "scripts/maturity-loop/mutations.edn の `:green-marker`。全部緑のときだけ出す ——
   出力に現れるかどうかで mutation が噛んだかを判定するので、緑でないときに
   印字してはならない。"
  "managon contract: all green")

(defmethod t/report [:cljs.test/default :end-run-tests] [m]
  (if (t/successful? m)
    (println (str "\n" green-marker))
    (do (println "\nmanagon contract: FAILED")
        (js/process.exit 1))))

(t/run-tests 'managon.contract-test)
