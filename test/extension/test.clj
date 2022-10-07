(ns extension.test
  (:require [clojure.test :refer :all]
            [extension.domain :as d]))

(defn- remove-fn [node]
  (cond
    (or (seq? node) (vector? node)) (mapv remove-fn node)
    (map? node) (into {} (for [[k v] node] [k (remove-fn v)]))
    (or (fn? node) (var? node)) nil
    :else node))

(deftest test111
  (is (=
       ["div.thread:not(.post-hidden)"
        {:title "span.subject"
         :body "blockquote.postMessage"
         :button "img.extButton.threadHideButton"}]
       (d/on-document-changed-begin))))

(deftest test121
  (is (=
       []
       (d/on-document-changed-end
        {:config {:exclude ["READ"]}}
        [{:node {:raw-node "node"}
          :title {:raw-node "title" :innerText "A"}
          :body {:raw-node "body" :innerText "B"}
          :button {:raw-node "button" :innerText "C"}}]))))

(deftest test122
  (is (=
       [[:click {:raw-node "button" :innerText "C"}]]
       (d/on-document-changed-end
        {:exclude ["READ"]}
        [{:node {:raw-node "node"}
          :title {:raw-node "title" :innerText "READ"}
          :body {:raw-node "body" :innerText "B"}
          :button {:raw-node "button" :innerText "C"}}]))))

(deftest test123
  (is (=
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
  (let [target (gensym)]
    (is (=
         [[:add-node
           {:target target
            :node [:a {:innerText "[FADE]" :onclick nil}]}]]
         (remove-fn (d/add-user-menu-end {} [{:node target}]))))))

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
  (let [target (gensym)
        actual (remove-fn (d/handle-media-changes-end {} [{:node target}]))]
    (is (=
         [[:add-class {:target target :class "ext-marked"}]
          [:add-node {:target {:type :parent :target target} :node [:div {:class "ext-hover" :onclick nil}]}]]
         actual))))

(deftest test331
  (let [parent (gensym) target (gensym)]
    (is (=
         [[:remove-node target]
          [:click {:type :selector :target parent :selector ".collapseWebm > a"}]]
         (d/media-clicked parent {:target target})))))

(deftest test411
  (let [target (gensym)]
    (is (=
         [[:add-node
           {:target target
            :node
            [:div {}
             [:div {:className "panel-section panel-section-formElements"}
              [:div {:className "panel-formElements-item"}
               [:textarea {:onchange nil :value "" :className "browser-style text1"}]]]
             [:footer {:className "panel-section panel-section-footer"}
              [:button {:className "panel-section-footer-button" :innerText "Cancel" :onclick nil}]
              [:div {:className "panel-section-footer-separator"}]
              [:button {:className "panel-section-footer-button default" :innerText "Confirm" :onclick nil}]]]}]]
         (remove-fn (d/make-options (gensym) {} target))))))

(deftest test421
  (let [parent (gensym)
        exclude (str (gensym))]
    (is (=
         [[:set-value {:target {:type :selector :target parent :selector ".text1"}
                       :value (str "[\"" exclude "\"]")}]]
         (d/cancel-options parent {:exclude [exclude]})))))

(deftest test431
  (let [id (gensym)
        input (gensym)
        input-br (str "[" input "]")]
    (is (=
         [[:db {id {:input input-br}, :exclude [input]}]]
         (d/save-options [id] {id {:input input-br}})))))
