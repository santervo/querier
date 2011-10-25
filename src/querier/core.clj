(ns querier.core)

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

