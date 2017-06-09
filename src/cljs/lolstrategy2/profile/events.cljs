(ns lolstrategy2.profile.events
  (:require [ajax.core :as ajax :refer [GET]]
            [day8.re-frame.http-fx]
            [re-frame.core :refer [reg-event-fx reg-event-db dispatch]]
            [lolstrategy2.profile.subs :as subs]
            [re-frame.events :as events]))



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
;; GET summary-info
;;===============================================================


(reg-event-db
  ::on-query-summary-info-success
  (fn [db [_ {:keys [summonerId modifyDate champions]} resp]]
    (assoc-in db [:panel/profile :summary-info] champions)))


(reg-event-db
  ::on-query-summary-info-failure
  (fn [db [_ resp]]
    (println "Failure" resp)))


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
;; GET profile-info
;;===============================================================


(reg-event-db
  ::on-query-profile-basic-info-success
  (fn [db [_ {:keys [profileIconId name summonerLevel accountId id revisionDate]} resp]]

    (-> db
        (dissoc :panel/profile)
        (assoc-in [:panel/profile :profile :profile/accountId] accountId)
        (assoc-in [:panel/profile :profile :profile/id] id)
        (assoc-in [:panel/profile :profile :profile/name] name)
        (assoc-in [:panel/profile :profile :profile/icon] (str "http://ddragon.leagueoflegends.com/cdn/6.24.1/img/profileicon/" profileIconId ".png"))
        (assoc-in [:panel/profile :profile :profile/revisionDate] revisionDate)
        (assoc-in [:panel/profile :profile :profile/summonerLevel] summonerLevel))))


(reg-event-db
  ::on-query-profile-basic-info-failure
  (fn [db [_ failure]]
    (println failure)))


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

