# cats-channels #

[![Clojars Project](http://clojars.org/cats/cats-channel/latest-version.svg)](http://clojars.org/cats/cats-channel)

A channel monad for [cats library](https://github.com/funcool/cats).


## Install ##

The simplest way to use _cats_ in a Clojure project is by including
it as a dependency in your *_project.clj_*:

```clojure
[cats/cats-channel "0.4.0-SNAPSHOT"]
```

## Getting Started ##

In asynchronous environments with clojure and clojurescript we tend to use core.async, because it
is a very powerfull abstraction.

It would be awesome to be able to work with channel as a monadic type, and combine it with error
monads for short-circuiting async computations that may fail.

Let's start using channel as a functor:

```clojure
(require '[cljs.core.async :refer [chan put! <!!]])
(require '[cats.monad.channel :as channel])

;; Declare arbitrary channel with initial value
(def mychan (channel/with-value 2))

;; Use channel as a functor
(<!! (m/fmap inc mychan))
;; => 3
```

The channel type also fulfills the monad abstraction, let see it in action:

```clojure
(def result (m/mlet [a (channel/with-value 2)
                     b (channel/with-value 3)]
              (m/return (+ a b))))
(<!! result)
;; => 5
```

But the best of all is coming: combine the channel monad with error monads. It allows to build very
concise and simple asynchronous APIs. Let see how you can use it your application:

```clojure
(require '[cats.monad.either :as either])

;; Declare a monad transformer
(def either-chan-m
  (either/either-transformer channel/channel-monad))

;; A success example
(<!! (m/with-monad either-chan-m
       (m/mlet [a (channel/with-value (either/right 2))
                b (channel/with-value (either/right 3))]
         (m/return (+ a b)))))
;; => #<Right [5]>
```

As you can see, the code looks very similar to the previos example, with the exception that
the value in a channel is not a simple plain value, is an either instance.

Let's see what happens if some computation fails in the mlet composition:

```clojure
(<!! (m/with-monad either-chan-m
       (m/mlet [a (channel/with-value (either/left "Some error"))
                b (channel/with-value (either/right 3))]
         (m/return (+ a b)))))
;; => #<Left [Some error]>
```

The result is the expected short-circuiting left, without unexpected nullpointer exceptions
or similar issues.

With this compositional power, you can model your asynchronous API with a complete
error handling using any error monad (in this case Either).


## Faq ##

**Why is not part of cats library directly?**

Because channel monad depends on core async and we do not want make core.async
as mandatory dependency.
