(ns goala.core-test
  (:require
    [clojure.test :refer [deftest is testing]]
    [goala.core :as goala]))

(deftest start-stop-restart
  (testing "start-app"   (is (= :started   (goala/start-app))))
  (testing "restart-app" (is (= :restarted (goala/restart-app))))
  (testing "stop-app"    (is (= :stopped   (goala/stop-app))))
  
  (testing "start-app"
    (is (= :started (goala/start-app)))

    (is (seq  @goala/system))
    (is (map? @goala/config))
    (is (seq  @goala/config))))

(deftest final-stop
  (testing "stop-app" (is (= :stopped (goala/stop-app)))))
