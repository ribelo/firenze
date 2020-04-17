(ns ribelo.firenze.firestore
  (:require
   ["firebase/app" :as firebase]
   [applied-science.js-interop :as j]
   [cljs-bean.core :as bean :refer [->clj ->js]]
   [ribelo.firenze.utils :as u]))

(defn delete-field []
  (j/call-in firebase [:firestore :FieldValue :delete]))

(defn firestore []
  (j/call firebase :firestore))

(defn doc->clj [doc]
  (->clj (j/call doc :data)))

(defn collection->clj [coll]
  (mapv doc->clj (j/get coll :docs)))

(defn query->clj [coll]
  (collection->clj coll))

(defn on-doc-snapshot!
  ([path cb]
   (on-doc-snapshot! path cb {}))
  ([path cb {:keys [on-success on-failure]}]
   (-> (firestore)
       (j/call :doc (u/->path path))
       (j/call :onSnapshot #(cb (doc->clj %)))
       (cond-> on-success
         (j/call :then #(on-success %)))
       (cond-> on-failure
         (j/call :catch #(on-success %))))))

(defn on-coll-snapshot!
  ([path cb]
   (on-coll-snapshot! path cb {}))
  ([path cb {:keys [on-success on-failure]}]
   (-> (firestore)
       (j/call :collection (u/->path path))
       (j/call :onSnapshot (fn [snap]
                             (let [changes (->> (j/call snap :docChanges)
                                                (->clj)
                                                (mapv (fn [doc]
                                                        (-> doc
                                                            (update :doc doc->clj)
                                                            (update :type keyword)))))]
                               (cb changes))))
       (cond-> on-success
         (do
           (println :success)
           (j/call :then #(on-success %))))
       (cond-> on-failure
         (do
           (println :failure)
           (j/call :catch #(on-failure %)))))))

(defn unsubscribe [path]
  (-> (firestore)
      (j/call :doc (u/->path path))
      (j/call :onSnapshot (fn []))))

(defn unsubscribe-coll [path]
  (-> (firestore)
      (j/call :collection (u/->path path))
      (j/call :onSnapshot (fn []))))

(defn get-coll
  ([path]
   (get-coll path {}))
  ([path {:keys [on-success on-failure]}]
   (-> (firestore)
       (j/call :collection (u/->path path))
       (j/call :get)
       (cond-> on-success
         (j/call :then (fn [coll] (on-success (mapcat #(doc->clj %) coll)))))
       (cond-> on-failure
         (j/call :catch #(on-failure %))))))

(defn get-doc
  ([path]
   (get-doc path {}))
  ([path {:keys [on-success on-failure]}]
   (-> (firestore)
       (j/call :doc (u/->path path))
       (j/call :get)
       (cond-> on-success
         (j/call :then #(on-success (doc->clj %))))
       (cond-> on-failure
         (j/call :catch #(on-failure %))))))

(defn set-doc
  ([path doc]
   (set-doc path doc {}))
  ([path doc {:keys [on-success on-failure options] :or {options {:merge false}}}]
   (println options)
   (-> (firestore)
       (j/call :doc (u/->path path))
       (j/call :set (->js doc) (->js options))
       (cond-> on-success
         (j/call :then #(on-success %)))
       (cond-> on-failure
         (j/call :catch #(on-failure %))))))

(defn update-doc
  ([path doc]
   (update-doc path doc {}))
  ([path doc {:keys [on-success on-failure]}]
   (println :update-doc path doc)
   (-> (firestore)
       (j/call :doc (u/->path path))
       (j/call :update (->js doc))
       (cond-> on-success
         (j/call :then #(on-success %)))
       (cond-> on-failure
         (j/call :catch #(on-failure %))))))

(defn add-doc
  ([collection doc]
   (add-doc collection doc {}))
  ([collection doc {:keys [on-success on-failure]}]
   (-> (firestore)
       (j/call :collection (u/->path collection))
       (j/call :add (->js doc))
       (cond-> on-success
         (j/call :then #(on-success %)))
       (cond-> on-failure
         (j/call :catch #(on-failure %))))))

(defn del-doc
  ([path]
   (del-doc path {}))
  ([path {:keys [on-success on-failure]}]
   (-> (firestore)
       (j/call :doc (u/->path path))
       (j/call :delete)
       (cond-> on-success
         (j/call :then #(on-success %)))
       (cond-> on-failure
         (j/call :catch #(on-failure %))))))

(defn query [{:keys [collection where order-by limit
                     start-at start-after end-at end-before
                     on-success on-failure]}]
  (let [ref (-> (firestore)
                (j/call :collection (u/->path [collection])))]
    (-> (as-> ref $
          (if where
            (reduce
             (fn [$$ [path op value]] (j/call $$ :where (u/->path path) (->js op) (->js value)))
             $ where)
            $)
          (if order-by
            (reduce
             (fn [$$ order] (j/call $$ :orderBy (->js (nth order 0)) (->js (nth order 1 :asc))))
             $ order-by)
            $)
          (if limit (j/call $ :limit limit) $)
          (if start-at (j/apply $ :startAt (->js start-at)) $)
          (if start-after (j/apply $ :startAfter (->js start-after)) $)
          (if end-at (j/apply $ :endAt (->js end-at)) $)
          (if end-before (j/apply $ :endBefore (->js end-before)) $))
        (j/call :get)
        (cond-> on-success
          (j/call :then #(on-success %)))
        (cond-> on-failure
          (j/call :catch #(on-failure %))))))
