(ns querier.core
  (:require [clojure.string :as s]))

(def comma-list (partial s/join ", "))

(defn col-def [col-name-or-def]
  (if (seq? col-name-or-def)
    col-name-or-def
    (list 'col col-name-or-def)))

(defmacro table [tblname & cols]
  (let [tbl {:name (name tblname) :cols []}
        col-defs (map col-def cols)]
    (cons '-> (cons tbl col-defs)))) 

(defn col [tbl col-name]
  (update-in tbl [:cols] #(conj % (name col-name))))

(defn fkey [tbl col-name ref-tbl]
  (col tbl col-name))

(defn select-cols [tbl tbl-prefix]
  (comma-list (map #(str tbl-prefix "." %) (:cols tbl))))

(defn select-expr [tbl tbl-prefix]
  (str "SELECT " (select-cols tbl tbl-prefix)))

(defn from-expr [tbl tbl-prefix]
  (str "FROM " (:name tbl) " AS " tbl-prefix))

(defn sql [tbl]
  (str
    (select-expr tbl (:name tbl)) " "
    (from-expr tbl (:name tbl))))
