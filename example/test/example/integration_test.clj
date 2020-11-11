(ns example.integration-test
  (:require [example.server :refer [service]]
            [io.pedestal.http :as server]
            [clj-http.client :as http]
            [clojure.test :refer [deftest is use-fixtures]]))

(defn- with-server [f]
  (let [server (-> service
                   (assoc ::server/port 8899)
                   server/create-server
                   server/start)]
    (try (f)
         (finally (server/stop server)))))

(use-fixtures :once with-server)

(def query
  "{
     one: pets {
       id
       details {
         age
         name
       }
       more_details: details {
         age
       }
     }

 ##   two: pets {
 ##      id
 ##      details {
 ##        age
 ##        name
 ##      }
 ##      more_details: details {
 ##        age
 ##      }
 ##    }
   }")

(deftest integration-test
  (is (= {:data
          {:one
           [{:id "abc-123",
             :details {:age 11, :name "Lyra"},
             :more_details {:age 11}}
            {:id "def-234",
             :details {:age 11, :name "Pantalaimon"},
             :more_details {:age 11}}
            {:id "ghi-345",
             :details {:age 41, :name "Iorek"},
             :more_details {:age 41}}]
           #_:two
           #_[{:id "abc-123",
             :details {:age 11, :name "Lyra"},
             :more_details {:age 11}}
            {:id "def-234",
             :details {:age 11, :name "Pantalaimon"},
             :more_details {:age 11}}
            {:id "ghi-345",
             :details {:age 41, :name "Iorek"},
             :more_details {:age 41}}]}}

         (:body (http/post "http://localhost:8899/graphql"
                           {:form-params {:query query}
                            :content-type :json
                            :as :json})))))

(deftest stress-test
  (dotimes [_i 50]
    (integration-test)))
