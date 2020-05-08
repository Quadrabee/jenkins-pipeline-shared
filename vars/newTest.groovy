#!groovy

import groovy.json.JsonOutput
import java.util.Optional
import hudson.tasks.test.AbstractTestResultAction
import hudson.model.Actionable
import hudson.tasks.junit.CaseResult
import com.quadrabee.utils

def call(String channel) {
  def utils = new com.quadrabee.utils()
  utils.test()
}
