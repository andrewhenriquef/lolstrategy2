(ns lolstrategy2.profile.views
  (:require [re-frame.core :refer [subscribe dispatch]]
            [re-com.core :as re-com]
            [reagent.core :as reagent]
            [lolstrategy2.profile.subs :as subs]
            [lolstrategy2.profile.events :as events]
            [cljs.pprint :as pprint]))


;;keywordize
(defn transform [row]
  (keyword (str row)))


(defn get-champion-image [ranked]
  (let [r (transform ranked)
        champions @(subs/champions)]
    (if (contains? champions r)
      (get-in champions [r :image :full]))))

(defn get-champion-name [ranked]
  (let [r (transform ranked)
        champions @(subs/champions)]
    (if (contains? champions r)
      (get-in champions [r :name]))))


(defn top-champions []
  [re-com/v-box
   :style {:margin-left "120px"
           :margin-top  "30px"}
   :children
   [[:span {:style {:color       "#1f8ecd"
                    :font-family "Orbitron, sans-serif"
                    :font-size   "20px"
                    :text-align  :center}} "MELHORES CAMPEÕES"]
    [:pre {:style {:margin-top "10px"
                   :align      :center}}
     [:child
      [re-com/h-box
       :children [

                  (map (fn [row]
                         (let [top-image (str "http://ddragon.leagueoflegends.com/cdn/6.24.1/img/champion/" (get-champion-image (get-in row [:championId])))
                               score (str (:championPoints row))
                               lvl (str "/img/mastery" (:championLevel row) ".png")]
                           ^{:key (get-in row [:championId])}
                           [re-com/v-box
                            :align :center
                            :padding "7px"
                            :children [
                                       [:img {:src   top-image
                                              :style {:borderRadius "50%"
                                                      :width        "90px"
                                                      :height       "90px"}}]

                                       [:pre {:style {:padding    "7px"
                                                      :margin-top "5px"}}

                                        [:img {:src   lvl
                                               :style {:borderRadius "50%"
                                                       :width        "90px"
                                                       :height       "90px"}}]
                                        [re-com/h-box
                                         :align :center
                                         :children [[:img {:src   (str "http://ddragon.leagueoflegends.com/cdn/5.5.1/img/ui/score.png")
                                                           :style {
                                                                   :width  "25px"
                                                                   :height "25px"}}]

                                                    [:span {:style {:color       :red
                                                                    :font-family "Orbitron, sans-serif"
                                                                    :font-size   "14px"}} score]]]]]])) @(subs/top-champions))]]]
     ]]])


(defn table-summary []
  [re-com/v-box
   :width "300px"
   :style {:margin-top "30px"}
   :children
   [[:span {:style {:color       "#1f8ecd"
                    :font-family "Orbitron, sans-serif"
                    :font-size   "18px"
                    :text-align  :center}} "CAMPEÕES MAIS JOGADOS"]
    [:table.table.table-hover
     {:cell-spacing "0" :width "100%"}
     [:thead>tr
      [:th]
      [:th]
      [:th]
      [:th]]
     ;;TODO dont show if id equals 0 , order by win ratio or played matchs, show only x champions
     [:tbody
      (map (fn [row]
             (let [k (/ (get-in row [:stats :totalChampionKills]) (get-in row [:stats :totalSessionsPlayed]))
                   d (/ (get-in row [:stats :totalDeathsPerSession]) (get-in row [:stats :totalSessionsPlayed]))
                   a (/ (get-in row [:stats :totalAssists]) (get-in row [:stats :totalSessionsPlayed]))
                   cs (pprint/cl-format nil "~,1f" (/ (get-in row [:stats :totalMinionKills]) (get-in row [:stats :totalSessionsPlayed])))
                   kdaratio (pprint/cl-format nil "~,1f" (/ (+ k a) d))
                   kda (str (pprint/cl-format nil "~,1f" k) "/" (pprint/cl-format nil "~,1f" d) "/" (pprint/cl-format nil "~,1f" a))
                   champion-name (str (get-champion-name (get-in row [:id])))
                   champion-image (str "http://ddragon.leagueoflegends.com/cdn/6.24.1/img/champion/" (get-champion-image (get-in row [:id])))
                   win-ratio (str (Math/round (/ (* (get-in row [:stats :totalSessionsWon]) 100) (get-in row [:stats :totalSessionsPlayed]))) "%")
                   total-matchs (get-in row [:stats :totalSessionsPlayed])]

               ^{:key (get-in row [:id])}
               [:tr
                ;;champion-image
                [:td
                 [:img {:src   champion-image
                        :style {:borderRadius "50%"
                                :width        "50px"
                                :height       "50px"}}]]

                ;;champion-name cs
                [:td
                 [re-com/v-box
                  :align :center
                  :children
                  [[:span {:style {:font-family "Orbitron, sans-serif"
                                   :font-size   "14px"
                                   :font-weight :bold
                                   :margin-top  "10px"
                                   :align       :left}} champion-name]
                   [:span {:style {:font-family "Orbitron, sans-serif"
                                   :font-size   "12px"}} (str "CS: " cs)]]]]

                ;;kda
                [:td {:style {:padding "5px"}}
                 [re-com/v-box
                  :align :center
                  :children [[:span {:style {:color       "#1f8ecd"
                                             :font-family "Orbitron, sans-serif"
                                             :font-size   "18px"
                                             :margin-top  "7px"}} (str kdaratio ":1 AMA")]
                             [:span {:style {:font-family "Orbitron, sans-serif"
                                             :font-size   "18px"}} kda]]]]

                ;;win-ratio
                [:td
                 {:style {:padding "0px"}}
                 [re-com/v-box
                  :children
                  [[:span {:style {:color       :red
                                   :font-family "Orbitron, sans-serif"
                                   :font-size   "14px"
                                   :margin-top  "18px"}} win-ratio]
                   [:span {:style {:color       "#1f8ecd"
                                   :font-family "Orbitron, sans-serif"
                                   :font-size   "10px"}} (str total-matchs ":Jogos")]]]]

                ])) @(subs/summary-info))]]]])


(defn form []
  [re-com/h-box
   :justify :end
   :style {:margin-top "7px"}
   :children
   [[re-com/input-text
     :model @(subs/nickname)
     :on-change #(events/on-change-nickname %)
     :change-on-blur? true]

    [re-com/button
     :label "Find"
     :on-click #(do
                  (events/on-query-profile-basic-info)
                  (events/on-query-champions))]]])


(defn perfil-data []
  [re-com/v-box
   :style {:margin-top "20px"}
   :children
   [;;Elo realm image
    (when @(subs/profile-id)
      [re-com/h-box
       :justify :around
       :children
       [


        [re-com/title
         :label @(subs/profile-name)
         :style {:color       "#1f8ecd"
                 :font-family "Orbitron, sans-serif"
                 :font-size   "80px"
                 :font-style  "italic"}]

        [:div]

        [:img {:src   @(subs/profile-icon)
               :style {:borderRadius "50%"
                       :width        "170px"
                       :height       "100%"
                       :border       "6px solid"}}]

        ;;ranked info
        [re-com/v-box
         :align :center
         :children
         [
          ;;tier
          [re-com/title
           :label @(subs/ranked-league-tier)
           :style {:color       "#1f8ecd"
                   :font-family "Orbitron, sans-serif"
                   :font-size   "14px"
                   :font-style  "italic"}]
          ;;image realm
          [:img {:src   @(subs/ranked-league-img)
                 :style {:borderRadius "50%"
                         :width        "170px"
                         :height       "100%"}}]]]

        [:div]
        ]])



    ;player name



    ;;player lvl
    [re-com/v-box
     :children
     [[re-com/h-box
       :children
       [[re-com/label
         :label "Lvl"]
        [re-com/title
         :label @(subs/profile-summoner-level)
         :level :level3]]]]]

    ;;player id
    [re-com/v-box
     :children
     [[re-com/h-box
       :children
       [[re-com/label
         :label "ID:"]
        [re-com/title
         :label @(subs/profile-id)
         :level :level3]]]]]

    ]])


(defn main-panel []
  [re-com/v-box
   :children
   [[:nav.navbar.navbar-inverse
     {:style {:margin-bottom "0px"
              :border-radius "0px"}}
     [:div.navbar-header [:a.navbar-brand {:style {:color       :white
                                                   :font-family "Orbitron, sans-serif"
                                                   :font-size   "24px"}
                                           :href  "#/profile"} "LolStrategy"]]
     [form]]
    [re-com/v-box
     :style {:background-image "url(/img/elise2.jpg)"
             :text-align       :center
             :width            "100%"
             :height           "200px"}
     :children
     [[perfil-data]]]

    (when @(subs/profile-id)
      (do
        (events/on-query-top-champions)
        (events/on-query-summary-info)
        (events/on-query-ranked-league)
        (events/on-query-match-recent)
        [re-com/h-box
         :gap "13px"
         :children
         [[table-summary]
          [top-champions]]]))]])

