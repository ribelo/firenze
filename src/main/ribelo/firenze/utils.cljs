(ns ribelo.firenze.utils
  (:require
   [clojure.string :as str]
   [cljs-bean.core :refer [->js]]))

(defprotocol Path
  (->path [path]))

(extend-protocol Path
  cljs.core/PersistentVector
  (->path [path] (str/join "/" (mapv ->path path)))
  cljs-bean.core/ArrayVector
  (->path [path] (str/join "/" (mapv ->path path)))
  cljs.core/Keyword
  (->path [path] (-> (cljs.core/munge path) (str/replace #"\." "_DOT_")))
  number
  (->path [path] (cljs.core/munge path))
  string
  (->path [path] (-> (cljs.core/munge path) (str/replace #"\." "_DOT_"))))




