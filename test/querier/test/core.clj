(ns querier.test.core
  (:use [querier.core] :reload)
  (:use [clojure.test]))

(deftest test-sql-creation
  (testing "single table"
    (is (= "SELECT post.title AS post__title FROM post AS post" (sql (table :post :title))))
    (is (= "SELECT post.title AS post__title, post.body AS post__body FROM post AS post" 
           (sql (table :post :title :body))))
    (is (= "SELECT comment.body AS comment__body, comment.post_id AS comment__post_id FROM comment AS comment" 
           (sql (table :comment :body (fkey :post_id (table :post :id))))))))
