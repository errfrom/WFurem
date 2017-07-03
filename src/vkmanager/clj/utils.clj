(ns vkmanager.clj.utils
  (:require [clojure.string :as str]
            [clojure.set    :refer [difference]]))

(defn normalize-text [text & {:keys [delimiter] :or {delimiter ""}}]
  (-> text (#(str/split % #" |\n"))
           (#(filter (comp not nil? seq) %))
           (#(str/join delimiter %))))

(def join-by-newline
  (partial str/join "\n"))

(defn align-by [delimiter & strings] ; TODO: не проходит тесты!
  "Выравнивает строки по ограничителю."
    (let [space   " "
          pattern (re-pattern (format "[%s]" delimiter))
          get-index #(str/last-index-of % delimiter)
          max-index  (apply max
                       (map (partial get-index)
                             strings))

          add-spaces #(str/join
                        (str (str/join (repeat (- max-index (get-index %))
                                               space))
                              delimiter)
                        (str/split % pattern))]
    (vec (map (partial add-spaces) strings))))

(defn flatten1 [x]
  "Удаляет один уровень вложенности.
   > (flatten1 [a1 a2 [a3 a4]])
   [a1 a2 a3 a4]"
  (if (nil? (seq x)) []
    (let [head (first x)
          tail (rest  x)]
      (if (sequential? head)
        ; рекурсивно раскладывается двумя путями
        ; если мы имеем [a1 a2 [a3 a4] a5]
        ; получим  (cons a1
        ;            (cons a2
        ;              (into [a3 a4]
        ;                (cons a5 [])
        ; что сворачивается в [a1 a2 a3 a4 a5]
        (into head (flatten1 tail))
        (cons head (flatten1 tail))))))

(defn separate-by-commas [x]
  "Соединяет последовательность в строку по разделителю запятой."
  (if (sequential? x)
    (str/join "," x)
    x))

(defn lstrip [x]
  "Удаляет первый элемент строки."
  (str/join "" (rest x)))

(defn add-missing-keys [x pattern]
  "Сравнивает hashmap с шаблоном (последовательность ключей).
   И, если в hashmap'е отсутствуют некоторые ключи из шаблона,
   добавляет их со значением nil.
   > (add-missing-keys {:a 1 :b 2} #(:a :b :c :d)
   {:a 1 :b 2 :c nil :d nil}."
  (let [excepted     (if (instance? java.util.Set pattern) pattern (set pattern))
        existing     (set (keys x))
        missing      (difference excepted existing)
        missing-nils (apply merge (map #(hash-map % nil) missing))]
    (merge x missing-nils)))

(defn positive-integer? [x]
  "Определяет, является ли число целым и положительным."
  (re-matches #"^[1-9][0-9]*$" x))

(defn update-if-contains [x key' fun]
  "Обертка вокруг встроенной функции update.
   Отличие заключается в обработке исключительной ситуации
   при передаче несуществующего в ассоциативном массиве ключа:
   обычная функция добавляет ключ, эта же возвращает первоначальный
   hashmap"
  (if (contains? x key') (update x key' fun)
                         x))
