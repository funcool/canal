;; Copyright (c) 2014-2015, Andrey Antukh
;; Copyright (c) 2014-2015, Alejandro Gómez
;; All rights reserved.
;;
;; Redistribution and use in source and binary forms, with or without
;; modification, are permitted provided that the following conditions
;; are met:
;;
;; 1. Redistributions of source code must retain the above copyright
;;    notice, this list of conditions and the following disclaimer.
;; 2. Redistributions in binary form must reproduce the above copyright
;;    notice, this list of conditions and the following disclaimer in the
;;    documentation and/or other materials provided with the distribution.
;;
;; THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
;; IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
;; OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
;; IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
;; INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
;; NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
;; DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
;; THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
;; (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
;; THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

(ns cats.monad.channel
  #?(:cljs (:require-macros [cljs.core.async.macros :refer [go]]))
  (:require #?(:clj  [clojure.core.async :refer [chan go pipe put! take! <!]]
               :cljs [cljs.core.async :refer [chan pipe put! take! <!]])
            [clojure.core.async.impl.dispatch :as dispatch]
            [clojure.core.async.impl.protocols :as impl]
            [cats.context :as ctx]
            [cats.core :as m]
            [cats.protocols :as p]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; Monad definition
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(def ^{:no-doc true}
  channel-monad
  (reify
    (fmap [mn f mv]
      (let [ctx (ctx/get-current)
            channel (chan)]
        (take! mv (fn [v]
                    (put! channel
                          ;; Set double monad for handle properly
                          ;; monad transformers
                          (ctx/with-context ctx
                            (f v)))))
    p/Functor
        channel))

    p/Applicative
    (pure [_ v]
      (let [channel (chan)]
        (put! channel v)
        channel))

    (fapply [mn af av]
      (go
        (let [afv (<! af)]
          (<! (p/fmap mn afv av)))))

    p/Monad
    (mreturn [_ v]
      (let [channel (chan)]
        (put! channel v)
        channel))

    (mbind [_ mv f]
      (let [ctx (ctx/get-current)
            ch (chan)]
        (take! mv (fn [v]
                    (ctx/with-context ctx
                      (pipe (f v) ch))))
        ch))))

(extend-type #?(:clj  clojure.core.async.impl.channels.ManyToManyChannel
                :cljs cljs.core.async.impl.channels.ManyToManyChannel)
  p/Context
  (get-context [_] channel-monad))

(defn with-value
  "Simple helper that creates a channel and attach
  an value to it."
  ([value] (with-value value (chan)))
  ([value ch]
   (put! ch value)
   ch))
