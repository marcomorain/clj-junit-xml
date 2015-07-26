(ns clj-junit-xml.core
  (:require
    [clojure.pprint :refer (pprint)]
    [clojure.test :as t]
    [clojure.xml :as xml]
    [clojure.string :as s]
    )

  )

(def test-case (atom nil))

(def ^:dynamic *test-cases* nil)


(defmulti report :type)

(defmethod report :default [m]
  (when-let [v (first t/*testing-vars*)]
;  (pprint m)
 ;  (pprint v)

    )

  )

(defmethod report :pass [m]
  (t/with-test-out
    (t/inc-report-counter :pass)))


(defn push-result [r]
  (swap! test-case update-in [:content] conj r))

(defmethod report :fail [m]
  (t/with-test-out
    (t/inc-report-counter :fail)
    (let [msg {:tag :failure
               :attrs {:message
                       (s/join "\n"
                               [(str "Expected " (:expected m))
                                (str "Actual   " (:actual m))
                                (str "at " (str (:file m)) ":" (:line m))
                                ])}}]
      (push-result msg))))


(defn- exception-message [m]
  (format "%s: %s" (:message m) (-> m :actual str)))

(defmethod report :error [m]
  (t/with-test-out
   (t/inc-report-counter :error)
    (pprint m)
    (let [msg {:tag :error
               :attrs {:message (exception-message m)}}]
      (push-result msg))))

(defmethod report :begin-test-var [m]
  (let [test-meta (-> m :var meta)]
    (swap! test-case (constantly {:tag :testcase
                                  :attrs {:file (str (:file test-meta))
                                          :time (System/nanoTime) }
                                  :content []}))))

(defn- duration-str [start-time end-time]
  (let [duration (- end-time start-time)]
    (if (pos? duration)
      (format "%f "(double (/ duration 1000 1000 1000)))
      "0")))

(defmethod report :end-test-var [m]
  (let [tc @test-case
        tc (update-in tc [:attrs :time] duration-str (System/nanoTime))]
   (swap! *test-cases* conj tc)))



(defn fixture [f]
  (binding [*test-cases* (atom '())
            t/report report]
    (let [start-time (System/nanoTime)
          _ (f)
          end-time (System/nanoTime)
          rc @t/*report-counters*
          report {:tag :testsuites
                  :attrs {:disabled 0
                          :name "a"
                          :errors (:error rc)
                          :failures (:fail rc)
                          :tests (:test rc)}
                  :content [{:tag :testsuite
                             :attrs {:name ""
                                     :tests (:test rc)
                                     :disabled 0
                                     :errors (:error rc)
                                     :failures (:fail rc)
                                     :skipped 0
                                     :time (duration-str start-time end-time)
                                     }


                             :content @*test-cases*}
                            ]
                  }]

      (println (xml/emit-element report)) ) ))

(defn foo
  "I don't do a whole lot."
  [x]
  (println x "Hello, World!"))
