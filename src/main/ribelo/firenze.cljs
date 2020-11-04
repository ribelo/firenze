(ns ribelo.firenze
  (:require
   [datascript.core :as d]
   [ribelo.firenze.realtime-database :as rdb]
   [rieblo.firenze.datascript :as fd]))

(defn listen! [conn]
  (d/listen! conn :firebase/sync (partial fd/datascript->firebase conn)))

(defn sync-path! [conn path]
  (rdb/off path)
  (rdb/on :child-added   path #(fd/firebase-on-child-added conn %))
  (rdb/on :child-changed path #(fd/firebase-on-child-changed conn %))
  (rdb/on :child-removed path #(fd/firebase-on-child-removed conn %)))

(defn sync-paths! [conn paths]
  (doseq [path paths] (sync-path! conn path)))

(defn unsync-path [path]
  (rdb/off path))

(defn unsync-paths [paths]
  (doseq [path paths] (unsync-path path)))
