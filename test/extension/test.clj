(ns extension.test
  (:require [clojure.test :refer :all]
            [extension.domain :as d]))

(deftest test111
  (is
   (=
    ["div.thread:not(.post-hidden)"
     {:title "span.subject"
      :body "blockquote.postMessage"
      :button "img.extButton.threadHideButton"}]
    (d/on-document-changed-begin))))

(deftest test121
  (is
   (=
    []
    (d/on-document-changed-end
     {:config {:exclude ["READ"]}}
     [{:node {:raw-node "node"}
       :title {:raw-node "title" :innerText "A"}
       :body {:raw-node "body" :innerText "B"}
       :button {:raw-node "button" :innerText "C"}}]))))

(deftest test122
  (is
   (=
    [[:click {:raw-node "button" :innerText "C"}]]
    (d/on-document-changed-end
     {:exclude ["READ"]}
     [{:node {:raw-node "node"}
       :title {:raw-node "title" :innerText "READ"}
       :body {:raw-node "body" :innerText "B"}
       :button {:raw-node "button" :innerText "C"}}]))))

(deftest test123
  (is
   (=
    [[:click {:raw-node "button" :innerText "C"}]]
    (d/on-document-changed-end
     {:exclude ["READ"]}
     [{:node {:raw-node "node"}
       :title {:raw-node "title" :innerText ""}
       :body {:raw-node "body" :innerText "READ"}
       :button {:raw-node "button" :innerText "C"}}]))))

(deftest test211
  (is (= ["#navtopright" {}] (d/add-user-menu-begin))))

(deftest test221
  (let [node (gensym)
        actual (d/add-user-menu-end {} [{:node node}])]
    (is
     (=
      [[:add-element
        {:target node
         :tag "A"
         :innerText "[FADE]"}]]
      (map
       (fn [[name params]] [name (dissoc params :onclick)])
       actual)))))

(deftest test231
  (is (=
       [[:db {:css-variables {"--ext_content_opacity" "0.05"}}]]
       (d/fade-clicked nil nil))))

(deftest test232
  (is (=
       [[:db {:css-variables {"--ext_content_opacity" "0.05"}}]]
       (d/fade-clicked {:css-variables {"--ext_content_opacity" "1.0"}} nil))))

(deftest test311
  (is (= ["video.expandedWebm:not(.ext-marked)" {}] (d/handle-media-changes-begin))))

(deftest test321
  (let [node (gensym)
        actual (d/handle-media-changes-end {} [{:node node}])]
    (is
     (=
      [[:add-class {:target node :class "ext-marked"}]
       [:add-element {:target {:type :parent :target node} :tag "DIV" :class "ext-hover"}]]
      (map
       (fn [[name params]] [name (dissoc params :onclick)])
       actual)))))

(deftest test331
  (let [parent (gensym) target (gensym)]
    (is (=
         [[:remove-node target]
          [:click {:type :selector, :target parent, :selector ".collapseWebm > a"}]]
         (d/media-clicked parent {:target target})))))
