(ns querier.rs
  (:use [rsmapper.core])
  (:require [clojure.set]))

(defn key-map [prefix ks]
  (apply hash-map (interleave (map #(keyword (str prefix "__" %)) ks) (map keyword ks))))

(defn map-rs [tbl tbl-prefix rs]
  (let [kmap (key-map tbl-prefix (:cols tbl))]
    (map #(clojure.set/rename-keys % kmap) rs)))
