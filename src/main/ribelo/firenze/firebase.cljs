(ns ribelo.firenze.firebase
  (:require
   ["firebase/app" :default firebase]
   [applied-science.js-interop :as j]
   [cljs-bean.core :as bean :refer [->js]]))


(defn initialize-app [firebase-app-info]
  (j/call firebase :initializeApp (->js firebase-app-info)))
