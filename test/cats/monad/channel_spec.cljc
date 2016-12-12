(ns cats.monad.channel-spec
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require #?@(:clj  [[clojure.core.async :refer [go chan put! take!
                                                   <! >! <!! >!!]]
                       [clojure.test :as t]]
                :cljs [[cljs-testrunners.node :as node]
                       [cljs.core.async :refer [chan put! take! <! >!]]
                       [cljs.test :as t]])
            [cats.core :as m]
            [cats.monad.channel :as c]
            [cats.monad.either :as either]
            [cats.protocols :as pt]))

#?(:clj
   (t/deftest channel-as-functor
     (let [ch (m/pure c/channel-monad 1)]
       (t/is (= 2 (<!! (m/fmap inc ch)))))))

#?(:clj
   (t/deftest channel-as-monad-1
     (let [ch (m/pure c/channel-monad 1)]
       (t/is (= 2 (<!! (m/>>= ch (fn [x] (m/return (inc x))))))))))

#?(:clj
   (t/deftest channel-as-monad-2
     (let [ch1 (chan 1)
           ch2 (chan 1)
           ch3 (chan 1)
           r   (m/mlet [x ch1
                        y ch2
                        z ch3]
                 (m/return (+ x y z)))]
       (go
         (>! ch1 1)
         (>! ch2 1)
         (>! ch3 1))
       (t/is (= 3 (<!! r))))))

#?(:cljs
   (t/deftest channel-as-functor
     (t/async done
              (go
                (let [ch (m/pure c/channel-monad 1)
                      rs (m/fmap inc ch)]
                  (t/is (= 2 (<! rs)))
                  (done))))))

#?(:cljs
   (t/deftest channel-as-monad-1
     (t/async done
              (go
                (let [ch (m/pure c/channel-monad 3)
                      rs (m/>>= ch (fn [x] (m/return (inc x))))]
                  (t/is (= 4 (<! rs)))
                  (done))))))

#?(:cljs
   (t/deftest channel-as-monad-2
     (t/async done
              (go
                (let [ch1 (chan 1)
                      ch2 (chan 1)
                      ch3 (chan 1)
                      r   (m/mlet [x ch1
                                   y ch2
                                   z ch3]
                            (m/return (+ x y z)))]
                  (>! ch1 1)
                  (>! ch2 1)
                  (>! ch3 1)
                  (t/is (= 3 (<! r))))
                (done)))))

#?(:clj
   (t/deftest first-monad-law-left-identity
     (let [ch1 (m/pure c/channel-monad 4)
           ch2 (m/pure c/channel-monad 4)
           vl  (m/>>= ch2 c/with-value)]
       (t/is (= (<!! ch1)
                (<!! vl)))))

   :cljs
   (t/deftest first-monad-law-left-identity
     (t/async done
              (go
                (let [ch1 (m/pure c/channel-monad 4)
                      ch2 (m/pure c/channel-monad 4)
                      vl  (m/>>= ch2 c/with-value)]
                  (t/is (= (<! ch1)
                           (<! vl)))
                  (done))))))

#?(:clj
   (t/deftest second-monad-law-right-identity
     (let [ch1 (c/with-value 2)
           rs  (m/>>= (c/with-value 2) m/return)]
       (t/is (= (<!! ch1) (<!! rs)))))

   :cljs
   (t/deftest second-monad-law-right-identity
     (t/async done
              (go
                (let [ch1 (c/with-value 2)
                      rs  (m/>>= (c/with-value 2) m/return)]
                  (t/is (= (<! ch1) (<! rs)))
                  (done))))))

#?(:clj
   (t/deftest third-monad-law-associativity
     (let [rs1 (m/>>= (m/mlet [x  (c/with-value 2)
                               y  (c/with-value (inc x))]
                        (m/return y))
                      (fn [y] (c/with-value (inc y))))
           rs2 (m/>>= (c/with-value 2)
                      (fn [x] (m/>>= (c/with-value (inc x))
                                    (fn [y] (c/with-value (inc y))))))]
       (t/is (= (<!! rs1) (<!! rs2)))))

   :cljs
   (t/deftest third-monad-law-associativity
     (t/async done
              (go
                (let [rs1 (m/>>= (m/mlet [x  (c/with-value 2)
                                          y  (c/with-value (inc x))]
                                   (m/return y))
                                 (fn [y] (c/with-value (inc y))))
                      rs2 (m/>>= (c/with-value 2)
                                 (fn [x] (m/>>= (c/with-value (inc x))
                                               (fn [y] (c/with-value (inc y))))))]
                  (t/is (= (<! rs1) (<! rs2)))
                  (done))))))

(def chaneither-m (either/either-transformer c/channel-monad))

#?(:clj
   (t/deftest channel-transformer-tests
     (t/testing "channel combination with either"
       (let [funcright (fn [x] (go (either/right x)))
             funcleft (fn [x] (go (either/left x)))
             r1 (m/with-monad chaneither-m
                  (m/mlet [x (funcright 1)
                           y (funcright 2)]
                    (m/return (+ x y))))

             r2 (m/with-monad chaneither-m
                  (m/mlet [x (funcright 1)
                           y (funcleft :foo)
                           z (funcright 2)]
                    (m/return (+ x y))))]

         (t/is (= (either/right 3) (<!! r1)))
         (t/is (= (either/left :foo) (<!! r2))))))

   :cljs
   (t/deftest channel-transformer-tests
     (t/async done
              (let [funcright #(c/with-value (either/right %))
                    funcleft #(c/with-value (either/left %))
                    r1 (m/with-monad chaneither-m
                         (m/mlet [x (funcright 1)
                                  y (funcright 2)]
                           (m/return (+ x y))))

                    r2 (m/with-monad chaneither-m
                         (m/mlet [x (funcright 1)
                                  y (funcleft :foo)
                                  z (funcright 2)]
                           (m/return (+ x y))))]
                (go
                  (t/is (= (either/right 3) (<! r1)))
                  (t/is (= (either/left :foo) (<! r2)))
                  (done))))))

#?(:cljs (defn main [] (node/run-tests)))

#? (:cljs (set! *main-cli-fn* main))
