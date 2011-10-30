(ns querier.test.rs
  (:use [querier.core] :reload)
  (:require [querier.rs :as rs] :reload)
  (:use [clojure.test]))

(def posts (table :post :id :title :body))

(deftest test-mapping
  (testing "simple mapping"
    (is (= [{:id 1 :title "Post title" :body "Post body"}]
           (rs/map-rs posts "post" [{:post__id 1 :post__title "Post title" :post__body "Post body"}])))))
