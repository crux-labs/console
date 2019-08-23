(ns juxt.crux-ui.frontend.views.query.time-controls
  (:require [garden.core :as garden]
            [juxt.crux-ui.frontend.views.query.datepicker-native :as ndt]
            [juxt.crux-ui.frontend.views.query.datepicker-slider :as sdt]
            [re-frame.core :as rf]
            [reagent.core :as r]
            [juxt.crux-ui.frontend.functions :as f]))

(def -sub-time (rf/subscribe [:subs.query/time]))
(def -sub-vt (rf/subscribe [:subs.query.time/vt]))
(def -sub-tt (rf/subscribe [:subs.query.time/tt]))

(defn on-time-commit [^keyword time-type ^js/Date time]
  (rf/dispatch [:evt.ui.query/time-commit time-type time]))

(def on-time-commit-debounced
  (f/debounce on-time-commit 1000))


(defn on-vt-change [d]
  (println :on-vt-change d)
  (on-time-commit-debounced :time/vt d)
  (rf/dispatch [:evt.ui.query/time-change :time/vt d]))

(defn on-vt-commit [d]
  (println :on-vt-commit d)
  (on-time-commit :time/vt d))

(defn on-tt-commit [d]
  (on-time-commit :time/tt d))

(defn on-tt-change [d]
  (on-time-commit-debounced :time/tt d)
  (rf/dispatch [:evt.ui.query/time-change :time/tt d]))


(def ^:private time-controls-styles
  [:style
   (garden/css
     [:.time-controls
      {:display         :flex
       :flex-direction  :column
       :justify-content :space-between
       :padding         "24px 24px"}
      [:&__item
       {:margin-bottom :32px}]])])

(defn native-pickers []
  [:<>
   [:div.time-controls__item
    [ndt/picker
     {:label "Valid time"
      :value @-sub-vt
      :on-change on-vt-change}]]
   [:div.time-controls__item
    [ndt/picker
     {:label "Transaction Time"
      :value @-sub-tt
      :on-change on-tt-change}]]])

(defn range-pickers []
  (let [active-picker (r/atom ::pickers-vt)
        toggle {::pickers-vt ::pickers-tt
                ::pickers-tt ::pickers-vt}
        toggle-time #(swap! active-picker toggle)]
    (fn []
      (if (= ::pickers-vt @active-picker)
       [:<>
        [sdt/root
         {:label "Valid time"
          :value-sub -sub-vt
          :on-change-complete on-vt-commit
          :on-change on-vt-change}]
        [:label {:on-click toggle-time} "Tx time: " (str @-sub-tt)]]
       [:<>
        [sdt/root
         {:label "Tx time"
          :value-sub -sub-tt
          :on-change-complete on-tt-commit
          :on-change on-tt-change}]
        [:label {:on-click toggle-time} "Valid time: " (str @-sub-vt)]]))))



(defn root []
  [:div.time-controls
   sdt/style
   ndt/style
   time-controls-styles
   #_[native-pickers]
   [range-pickers]])

