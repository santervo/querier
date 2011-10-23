(ns querier.test.core
  (:use [querier.core] :reload)
  (:use [clojure.test]))

(deftest test-sql-creation
  (testing "single table"
    (is (= "SELECT post.title FROM post AS post" (sql (table :post :title))))
    (is (= "SELECT post.title, post.body FROM post AS post" (sql (table :post :title :body))))
    (is (= "SELECT comment.body, comment.post_id FROM comment AS comment" 
           (sql (table :comment :body (fkey :post_id (table :post :id))))))))
