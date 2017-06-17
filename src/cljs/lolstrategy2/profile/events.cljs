(ns lolstrategy2.profile.events
  (:require [ajax.core :as ajax :refer [GET]]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-fx reg-event-db dispatch]]
            [lolstrategy2.profile.subs :as subs]
            [re-frame.events :as events]
            [clojure.string :as str]))



(defn treatment
  [string]
  (clojure.string/lower-case (clojure.string/replace string " " "")))


(reg-event-db
  ::on-change-nickname

  (fn [db [_ nickname]]
    (assoc-in db [:panel/profile :profile :profile/nickname] nickname)))


(defn on-change-nickname
  [nickname]
  (dispatch [::on-change-nickname nickname]))


;;=====================================================
;; GET ALL CHAMPIONS
;;=====================================================


(reg-event-db
  ::on-query-champions-success
  (fn [db [_ {:keys [type version data]} resp]]
    (assoc-in db [:panel/profile :champions] data)))


(reg-event-db
  ::on-query-champions-failure
  (fn [db [_ failure]]
    db))


(reg-event-fx
  ::on-query-champions
  (fn [_ db]
    {:http-xhrio {:method          :get
                  :uri             (str "https://br1.api.riotgames.com/lol/static-data/v3/champions?version=6.24.1&champListData=image&dataById=true&locale=pt_BR&api_key=RGAPI-5fca75ea-75dc-4c41-92ab-e79273937d79")
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::on-query-champions-success]
                  :on-failure      [::on-query-champions-failure]}}))


(defn on-query-champions
  []
  (dispatch [::on-query-champions]))


;;===============================================================
;; GET ranked-league
;;===============================================================

(reg-event-db
  ::on-query-ranked-league-success
  (fn [db [_ [{:keys [name tier queue entries]}] resp]]

    (if tier
      (-> db
         (assoc-in [:panel/profile :ranked-league :tier] tier)
         (assoc-in [:panel/profile :ranked-league :img] (str "/img/" (str/lower-case tier) ".png")))
      (-> db
          (assoc-in [:panel/profile :ranked-league :tier] "Unranked")
          (assoc-in [:panel/profile :ranked-league :img] (str "/img/provisional.png"))))))


(reg-event-db
  ::on-query-ranked-league-failure
  (fn [db [_ failure]]
    db))


(reg-event-fx
  ::on-query-ranked-league
  (fn [_ db]
    {:http-xhrio {:method          :get
                  :uri             (str "https://br1.api.riotgames.com/lol/league/v3/leagues/by-summoner/" @(subs/profile-id) "?api_key=RGAPI-5fca75ea-75dc-4c41-92ab-e79273937d79")
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::on-query-ranked-league-success]
                  :on-failure      [::on-query-ranked-league-failure]}}))


(defn on-query-ranked-league
  []
  (dispatch [::on-query-ranked-league]))


;;===============================================================
;; GET summary-info
;;===============================================================

;;take 8 champions and sort them by game played
(reg-event-db
  ::on-query-summary-info-success
  (fn [db [_ {:keys [summonerId modifyDate champions]} resp]]
    (assoc-in db [:panel/profile :summary-info] (rest (take 8 (sort-by (comp :totalSessionsPlayed :stats) > champions))))))

(reg-event-db
  ::on-query-summary-info-failure
  (fn [db [_ resp]]
    db))


(reg-event-fx
  ::on-query-summary-info
  (fn [_ db]
    {:http-xhrio {:method          :get
                  :uri             (str "https://br.api.riotgames.com/api/lol/BR/v1.3/stats/by-summoner/" @(subs/profile-id) "/ranked?season=SEASON2017&api_key=RGAPI-5fca75ea-75dc-4c41-92ab-e79273937d79")
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::on-query-summary-info-success]
                  :on-failure      [::on-query-summary-info-failure]}}))


(defn on-query-summary-info
  []
  (dispatch [::on-query-summary-info]))


;;===============================================================
;; GET Top-champions
;;===============================================================


(reg-event-db
  ::on-query-top-champions-success
  (fn [db [_ resp]]
        (assoc-in db [:panel/profile :top-champions] (take 3 resp))))


(reg-event-db
  ::on-query-top-champions-info-failure
  (fn [db [_ failure]]
    db))


(reg-event-fx
  ::on-query-top-champions
  (fn [_ db]
    {:http-xhrio {:method          :get
                  :uri             (str "https://br1.api.riotgames.com/lol/champion-mastery/v3/champion-masteries/by-summoner/"@(subs/profile-id) "?api_key=RGAPI-5fca75ea-75dc-4c41-92ab-e79273937d79")
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::on-query-top-champions-success]
                  :on-failure      [::on-query-top-champions-failure]}}))


(defn on-query-top-champions
  []
  (dispatch [::on-query-top-champions]))


;;===============================================================
;; GET match-recent
;;===============================================================

(reg-event-db
  ::on-query-match-recent-success
  (fn [db [_ {:keys [matches]} resp]]
    (-> db
        (assoc-in [:panel/profile :match-recent :matches] matches)
        (assoc-in [:panel/profile :match-recent :jungle] (/ (* (count (filter #(if (= (:lane %) "JUNGLE")
                                                                                 (:lane %)) matches)) 100) 20))
        (assoc-in [:panel/profile :match-recent :mid] (/ (* (count (filter #(if (= (:lane %) "MID")
                                                                                 (:lane %)) matches)) 100) 20))
        (assoc-in [:panel/profile :match-recent :top] (/ (* (count (filter #(if (= (:lane %) "TOP")
                                                                                 (:lane %)) matches)) 100) 20))
        (assoc-in [:panel/profile :match-recent :marksman](/ (* (count (filter #(and (= (:lane %) "BOTTOM") (= (:role %) "DUO_CARRY")
                                                                                  (:lane %)) matches)) 100) 20))
        (assoc-in [:panel/profile :match-recent :support](/ (* (count (filter #(and (= (:lane %) "BOTTOM") (= (:role %) "DUO_SUPPORT")
                                                                                     (:lane %)) matches)) 100) 20)))))


(reg-event-db
  ::on-query-match-recent-failure
  (fn [db [_ failure]]
    db))


(reg-event-fx
  ::on-query-match-recent
  (fn [_ db]
    {:http-xhrio {:method          :get
                  :uri             (str "https://br1.api.riotgames.com/lol/match/v3/matchlists/by-account/" (:profile/accountId @(subs/profile)) "/recent?api_key=RGAPI-5fca75ea-75dc-4c41-92ab-e79273937d79")
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::on-query-match-recent-success]
                  :on-failure      [::on-query-match-recent-failure]}}))


(defn on-query-match-recent
  []
  (dispatch [::on-query-match-recent]))


;;===============================================================
;; GET profile-info
;;===============================================================


(reg-event-db
  ::on-query-profile-basic-info-success
  (fn [db [_ {:keys [profileIconId name summonerLevel accountId id revisionDate]} resp]]

    (-> db
        (assoc-in [:panel/profile :profile :profile/accountId] accountId)
        (assoc-in [:panel/profile :profile :profile/id] id)
        (assoc-in [:panel/profile :profile :profile/name] name)
        (assoc-in [:panel/profile :profile :profile/icon] (str "http://ddragon.leagueoflegends.com/cdn/6.24.1/img/profileicon/" profileIconId ".png"))
        (assoc-in [:panel/profile :profile :profile/revisionDate] revisionDate)
        (assoc-in [:panel/profile :profile :profile/summonerLevel] summonerLevel))))


(reg-event-db
  ::on-query-profile-basic-info-failure
  (fn [db [_ failure]]
    db))


(reg-event-fx
  ::on-query-profile-basic-info
  (fn [_ db]
    {:http-xhrio {:method          :get
                  :uri             (str "https://br1.api.riotgames.com/lol/summoner/v3/summoners/by-name/" (treatment @(subs/nickname)) "?api_key=RGAPI-5fca75ea-75dc-4c41-92ab-e79273937d79")
                  :response-format (ajax/json-response-format {:keywords? true})
                  :on-success      [::on-query-profile-basic-info-success]
                  :on-failure      [::on-query-profile-basic-info-failure]}}))


(defn on-query-profile-basic-info
  []
  (dispatch [::on-query-profile-basic-info]))


