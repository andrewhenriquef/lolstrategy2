(ns lolstrategy2.profile.subs
  (:require [re-frame.core :refer [reg-sub subscribe]]))


(reg-sub
  ::profile-nickname
  (fn [db _]
    (or (get-in db [:panel/profile :profile :profile/nickname]) "")))


(defn nickname
  []
  (subscribe [::profile-nickname]))


(reg-sub
  ::profile-id
  (fn [db _]
    (get-in db [:panel/profile :profile :profile/id])))


(defn profile-id
  []
  (subscribe [::profile-id]))


(reg-sub
  ::profile-name
  (fn [db _]
    (or (get-in db [:panel/profile :profile :profile/name]) "")))


(defn profile-name
  []
  (subscribe [::profile-name]))


(reg-sub
  ::profile-icon
  (fn [db _]
    (or (get-in db [:panel/profile :profile :profile/icon]) "")))


(defn profile-icon
  []
  (subscribe [::profile-icon]))


(reg-sub
  ::profile-summoner-level
  (fn [db _]
    (or (get-in db [:panel/profile :profile :profile/summonerLevel]) "")))


(defn profile-summoner-level
  []
  (subscribe [::profile-summoner-level]))

(reg-sub
  ::summary-info
  (fn [db _]
    (or (get-in db [:panel/profile :summary-info]) "")))


(defn summary-info
  []
  (subscribe [::summary-info]))

(reg-sub
  ::champions
  (fn [db _]
    (get-in db [:panel/profile :champions])))


(defn champions
  []
  (subscribe [::champions]))

(reg-sub
  ::top-champions
  (fn [db _]
    (get-in db [:panel/profile :top-champions])))


(defn top-champions
  []
  (subscribe [::top-champions]))
