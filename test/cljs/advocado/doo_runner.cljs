(ns advocado.doo-runner
  (:require [doo.runner :refer-macros [doo-tests]]
            [advocado.core-test]))

(doo-tests 'advocado.core-test)

