(ns ribelo.firenze.utils
  (:require
   [clojure.string :as str]))

(defmulti ->path (fn [path] (type path)))

(defmethod ->path cljs.core/PersistentVector
  [path]
  (str/join "/" (mapv munge path)))

(defmethod ->path cljs.core/Keyword
  [path]
  (munge path))

(defmethod ->path js/String
  [path]
  (munge path))
