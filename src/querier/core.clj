(ns querier.core
  (:require [clojure.string :as s]))

(def comma-list (partial s/join ", "))

(def space-list (partial s/join " "))

(defn col-def [col-name-or-def]
  (if (seq? col-name-or-def)
    col-name-or-def
    (list 'col col-name-or-def)))

(defmacro table [tblname & cols]
  (let [tblname (name tblname)
        tbl {:name tblname :cols [] :fkeys []}
        col-defs (map col-def cols)]
    (cons '-> (cons tbl col-defs)))) 

(defn col [tbl col-name]
  (update-in tbl [:cols] #(conj % (name col-name))))

(defn fkey [tbl col-name ref-tbl]
  (-> tbl (col col-name)
    (update-in [:fkeys] #(conj % {:col (name col-name) :table ref-tbl}))))

(defn with [tbl other-tbl & opts]
  (let [opts (apply hash-map opts)
        opts (assoc opts :as (name (:as opts)))]
    (update-in tbl [:associations] #(conj % (assoc opts :table other-tbl)))))

(defmacro ljoin [tbl join-tbl & opts]
  (concat (list 'with tbl join-tbl :fetch :ljoin) opts))

(defn find-ref-col [tbl ref-tbl]
  (:col (first (filter #(= (:name ref-tbl) (:name (:table %))) (:fkeys tbl)))))

(defn find-pkey [tbl]
  (when (some #(= "id" %) (:cols tbl))
    "id"))

(defn join-table-alias [tbl-prefix as]
  (str tbl-prefix "__" as))

(defn assoc-alias [ass tbl-prefix]
  (join-table-alias tbl-prefix (:as ass)))

(defn col-expr [tbl-prefix col-name]
  (str tbl-prefix "." col-name))

(defn col-alias [tbl-prefix col-name]
  (str tbl-prefix "__" col-name))

(defn select-col [tbl-prefix col-name]
  (str (col-expr tbl-prefix col-name) " AS " (col-alias tbl-prefix col-name)))

(defn select-cols [tbl tbl-prefix]
  (let [cols (map #(select-col tbl-prefix %) (:cols tbl))
        assoc-cols (map (fn [ass] (select-cols (:table ass) (assoc-alias ass tbl-prefix))) (:associations tbl))]
    (concat cols (apply concat assoc-cols))))

(defn select-expr [tbl tbl-prefix]
  (str "SELECT " (comma-list (select-cols tbl tbl-prefix))))

(defn table-alias-expr [tbl tbl-prefix]
  (str (:name tbl) " AS " tbl-prefix))

(defn join-criteria [tbl tbl-prefix {as :as other-tbl :table}]
  (if-let [ref-col (find-ref-col other-tbl tbl)]
    (let [key-col (find-pkey tbl)]
      (str "ON(" (col-expr tbl-prefix key-col) " = " (col-expr (join-table-alias tbl-prefix as) ref-col) ")"))))

(defn join-expr [tbl tbl-prefix ass]
  (space-list ["LEFT OUTER JOIN" (table-alias-expr (:table ass) (assoc-alias ass tbl-prefix))
       (join-criteria tbl tbl-prefix ass)]))

(defn join-list-expr [tbl tbl-prefix]
  (map #(join-expr tbl tbl-prefix %) (:associations tbl)))

(defn from-expr [tbl tbl-prefix]
  (str "FROM " (space-list (cons (table-alias-expr tbl tbl-prefix) 
                                 (join-list-expr tbl tbl-prefix)))))

(defn sql [tbl]
  (str
    (select-expr tbl (:name tbl)) " "
    (from-expr tbl (:name tbl))))
