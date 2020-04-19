(ns ribelo.firenze.utils
  (:require
   [clojure.string :as str]
   [cljs-bean.core :as bean :refer [->js]]))

(defmulti ->path (fn [path] (type path)))

(defmethod ->path cljs.core/PersistentVector
  [path]
  (str/join "/" (mapv ->path path)))

(defmethod ->path cljs.core/Keyword
  [path]
  (munge (->js path)))

(defmethod ->path :default
  [path]
  (munge path))
