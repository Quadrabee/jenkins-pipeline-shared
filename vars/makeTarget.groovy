#!/usr/bin/env groovy

/**
 * Run make file target
 */
def makeTarget(String target) {
  container('builder') {
    sh "make ${target}"
  }
}
