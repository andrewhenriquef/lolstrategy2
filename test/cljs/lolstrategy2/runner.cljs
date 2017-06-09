(ns lolstrategy2.runner
    (:require [doo.runner :refer-macros [doo-tests]]
              [lolstrategy2.core-test]))

(doo-tests 'lolstrategy2.core-test)
