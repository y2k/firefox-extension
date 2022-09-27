(ns extension.test
  (:require [clojure.test :refer :all]
            [extension.domain :as d]))

(deftest test1
  (is
   (=
    ["div.thread:not(.post-hidden)"
     {:title "span.subject"
      :body "blockquote.postMessage"
      :button "img.extButton.threadHideButton"}]
    (d/on-document-changed-begin))))

(deftest test21
  (is
   (=
    []
    (d/on-document-changed-end
     {:config {:exclude ["READ"]}}
     [{:node {:raw-node "node"}
       :title {:raw-node "title" :innerText "A"}
       :body {:raw-node "body" :innerText "B"}
       :button {:raw-node "button" :innerText "C"}}]))))

(deftest test22
  (is
   (=
    [[:click {:raw-node "button" :innerText "C"}]]
    (d/on-document-changed-end
     {:config {:exclude ["READ"]}}
     [{:node {:raw-node "node"}
       :title {:raw-node "title" :innerText "READ"}
       :body {:raw-node "body" :innerText "B"}
       :button {:raw-node "button" :innerText "C"}}]))))

(deftest test23
  (is
   (=
    [[:click {:raw-node "button" :innerText "C"}]]
    (d/on-document-changed-end
     {:config {:exclude ["READ"]}}
     [{:node {:raw-node "node"}
       :title {:raw-node "title" :innerText ""}
       :body {:raw-node "body" :innerText "READ"}
       :button {:raw-node "button" :innerText "C"}}]))))

(deftest test31
  (is (= ["#navtopright" {}] (d/add-user-menu-begin))))

(deftest test41
  (let [actual (d/add-user-menu-end {} [])]
    (is
     (=
      [[:add-element
        {:root nil
         :tag "A"
         :innerText "[FADE]"}]]
      (map
       (fn [[name params]]
         [name
          (dissoc params :onclick)]) actual)))))
