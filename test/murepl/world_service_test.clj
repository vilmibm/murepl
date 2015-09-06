(ns murepl.world-service-test
  (:require [clojure.test :refer :all]
            [puppetlabs.trapperkeeper.testutils.bootstrap :refer [with-app-with-config]]
            [puppetlabs.trapperkeeper.app :as tka]
            [puppetlabs.trapperkeeper.services :as tks]
            [murepl.testutils :refer [test-db reset-db!]]
            [schema.test :refer [validate-schemas]]
            [murepl.testutils :refer [services]]
            [murepl.user :as u]
            [murepl.world :as w]))

(use-fixtures :once validate-schemas)
(use-fixtures :each (fn [f] (reset-db! test-db) (f)))


(def borges {:name "borges" :password "labyrinth" :data {}})
(def kafka {:name "kafka" :password "castle" :data {}})

(deftest placing-users
  (with-app-with-config app services {}
    (let [world-svc (tka/get-service app :WorldService)
          borges (u/new! borges test-db)
          kafka (u/new! kafka test-db)
          lobby (w/create-room! world-svc {:name "Lobby"
                                           :exits {}
                                           :description "A windowless room."})
          library (w/create-room! world-svc {:name "Library"
                                             :exits {}
                                             :description "A small room. Low
          bookshelves are lined with color-coordinated paperbacks."})
          {:keys [user->room rooms room->users]} (tks/service-context world-svc)]
      (testing "when placing a user"
        (w/place-user! world-svc borges lobby)
        (testing "we see the user in there"
          (is (= (:id lobby) (get @user->room (:name borges))))
          (is (= #{"borges"} (get @room->users (:id lobby))))))
      (testing "when placing a second user"
        (w/place-user! world-svc kafka lobby)
        (testing "we see both users"
          (is (= (:id lobby) (get @user->room (:name borges))))
          (is (= (:id lobby) (get @user->room (:name kafka))))
          (is (= #{"borges" "kafka"} (get @room->users (:id lobby))))))
      (testing "when moving a user out of a room"
        (w/place-user! world-svc borges library)
        (testing "we see one user in the lobby and one in the library"
          (is (= (:id library) (get @user->room (:name borges))))
          (is (= (:id lobby) (get @user->room (:name kafka))))
          (is (= #{"kafka"} (get @room->users (:id lobby))))
          (is (= #{"borges"} (get @room->users (:id library)))))))))
