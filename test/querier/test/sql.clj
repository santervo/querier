(ns querier.test.sql
  (:use [querier.core] :reload)
  (:require [querier.sql :as sql] :reload)
  (:use [clojure.test]))

(deftest test-sql-creation
  (testing "single table"
    (is (= "SELECT post.title AS post__title FROM post AS post" 
           (sql/generate (table :post :title))))
    (is (= "SELECT post.title AS post__title, post.body AS post__body FROM post AS post" 
           (sql/generate (table :post :title :body))))
    (is (= "SELECT comment.body AS comment__body, comment.post_id AS comment__post_id FROM comment AS comment" 
           (sql/generate (table :comment :body (fkey :post_id (table :post :id)))))))
  (testing "joined tables"
    (let [posts (table :post :id :title :body)
          comments (table :comment :body (fkey :post_id posts))]
      (is (= (str "SELECT post.id AS post__id, post.title AS post__title, "
                  "post.body AS post__body, post__comments.body AS post__comments__body, "
                  "post__comments.post_id AS post__comments__post_id "
                  "FROM post AS post LEFT OUTER JOIN comment AS post__comments "
                  "ON(post.id = post__comments.post_id)")
             (sql/generate (-> posts (ljoin comments :as :comments))))))))

