(ns ribelo.firenze.utils
  (:refer-clojure :exclude [munge demunge])
  (:require
   [cljs-bean.core :as bean :refer [->js]]
   [clojure.string :as str]
   [clojure.walk :refer [postwalk]]))

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

(defn munge-keys [m]
    (let [f (fn [[k v]] [(munge k) v])]
      (postwalk (fn [x] (if (map? x) (into {} (map f x)) x)) m)))

(defmulti ->path (fn [path] (type path)))

(defmethod ->path cljs.core/PersistentVector
  [path]
  (str/join "/" (mapv ->path path)))

(defmethod ->path cljs-bean.core/ArrayVector
  [path]
  (str/join "/" (mapv ->path path)))

(defmethod ->path cljs.core/Keyword
  [path]
  (munge path))

(defmethod ->path :default
  [path]
  (clojure.core/munge path))
