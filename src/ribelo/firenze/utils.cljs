(ns ribelo.firenze.utils
  (:refer-clojure :exclude [munge demunge])
  (:require
   [clojure.string :as str]
   [cljs-bean.core :as bean :refer [->js]]))

(defn munge [s]
  (-> s
      (->js)
      (str/replace "." "_DOT_")
      (clojure.core/munge)))

(defn demunge [s]
  (-> s
      (str/replace "_DOT_" ".")
      (clojure.core/demunge)
      (keyword)))

(defmulti ->path (fn [path] (type path)))

(defmethod ->path cljs.core/PersistentVector
  [path]
  (str/join "/" (mapv ->path path)))

(defmethod ->path cljs-bean.core/ArrayVector
  [path]
  (str/join "/" (mapv ->path path)))

(defmethod ->path cljs.core/Keyword
  [path]
  (munge (->js path)))

(defmethod ->path :default
  [path]
  (munge path))
