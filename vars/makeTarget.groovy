#!/usr/bin/env groovy

/**
 * Run make file target
 */
def call(String target) {
  container('builder') {
    sh "make ${target}"
  }
}
