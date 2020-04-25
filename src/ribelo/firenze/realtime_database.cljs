(ns ribelo.firenze.realtime-database
  (:refer-clojure :exclude [set update remove])
  (:require
   ["firebase/app" :as firebase]
   ["firebase/database"]
   [applied-science.js-interop :as j]
   [cljs-bean.core :as bean :refer [->clj ->js]]
   [ribelo.firenze.utils :as u]))

(defn database [] (j/call firebase :database))

(defn server-timestamp []
  (j/get-in firebase [:database :ServerValue :TIMESTAMP]))

(defn ref
  ([]
   (-> (database)
       (j/call :ref)))
  ([path]
   (-> (database)
       (j/call :ref (u/->path path)))))

(defn set
  ([path doc]
   (set path doc {}))
  ([path doc {:keys [on-success on-failure]}]
   (-> (ref path)
       (j/call :set (->js doc))
       (cond-> on-success
         (j/call :then on-success))
       (cond-> on-failure
         (j/call :catch #(on-failure %))))))

(defn push
  ([path]
   (-> (ref path)
       (j/call :push)
       (j/get :key)))
  ([path doc]
   (push path doc {}))
  ([path doc {:keys [on-success on-failure]}]
   (-> (ref path)
       (j/call :push (->js doc))
       (cond-> on-success
         (j/call :then on-success))
       (cond-> on-failure
         (j/call :catch #(on-failure %))))))

(defn update
  ([path doc]
   (update path doc {}))
  ([path doc {:keys [on-success on-failure]}]
   (-> (ref path)
       (j/call :update (->js doc))
       (cond-> on-success
         (j/call :then on-success))
       (cond-> on-failure
         (j/call :catch #(on-failure %))))))

(defn once
  ([path cb]
   (once path cb {}))
  ([path cb {:keys [on-failure]}]
   (-> (ref path)
       (j/call :once "value" (fn [snap] (cb (->clj (j/call snap :val)))))
       (cond-> on-failure
         (j/call :catch #(on-failure %))))))

(defn- -on [event path cb {:keys [on-failure]}]
  (-> (ref path)
      (j/call :on event
              (fn [snap & _] (cb (->clj (j/call snap :val)))))
      (cond-> on-failure
        (j/call :catch #(on-failure %)))))

(defmulti on (fn [event path cb opts] event))

(defmethod on :value
  [event path cb {:keys [on-failure]}]
  (-> (ref path)
      (j/call :on "value"
              (fn [snap] (cb (->clj (j/call snap :val)))))
      (cond-> on-failure
        (j/call :catch #(on-failure %)))))

(defmethod on :child-added
  [event path cb {:keys [on-failure] :as opts}]
  (-on "child_added" path cb opts))

(defmethod on :child-removed
  [event path cb {:keys [on-failure] :as opts}]
  (-on "child_removed" path cb opts))

(defmethod on :child-changed
  [event path cb {:keys [on-failure] :as opts}]
  (-on "child_changed" path cb opts))

(defn off [path]
  (-> (ref path)
      (j/call :off)))

(defn remove
  ([path]
   (remove path {}))
  ([path {:keys [on-success on-failure]}]
   (-> (ref path)
       (j/call :remove)
       (cond-> on-success
         (j/call :then on-success))
       (cond-> on-failure
         (j/call :catch #(on-failure %))))))

(defn transact
  ([path f]
   (transact path f {}))
  ([path f {:keys [on-success on-failure]}]
   (-> (ref path)
       (j/call :transaction f)
       (cond-> on-success
         (j/call :then on-success))
       (cond-> on-failure
         (j/call :catch #(on-failure %))))))
