#!groovy

import groovy.json.JsonOutput
import java.util.Optional
import hudson.tasks.test.AbstractTestResultAction
import hudson.model.Actionable
import hudson.tasks.junit.CaseResult

def call(String channel) {
  def utils = new com.quadrabee.utils()

  sh "./gradlew ${utils.gradleDefaultSwitches} clean build ${utils.gradleAdditionalTestTargets} ${utils.gradleAdditionalSwitches} --refresh-dependencies"
  step $class: 'JUnitResultArchiver', testResults: '**/rspec*.xml'
  utils.populateGlobalVariables()

  def buildColor = currentBuild.result == null ? "good" : "warning"
  def buildStatus = currentBuild.result == null ? "Success" : currentBuild.result
  def jobName = "${env.JOB_NAME}"

  // Strip the branch name out of the job name (ex: "Job Name/branch1" -> "Job Name")
  jobName = jobName.getAt(0..(jobName.indexOf('/') - 1))

  if (failed > 0) {
      buildStatus = "Failed"
      buildColor = "danger"
      def failedTestsString = getFailedTests()

      notifySlack("", channel, [
          [
              title: "${jobName}, build #${env.BUILD_NUMBER}",
              title_link: "${env.BUILD_URL}",
              color: "${buildColor}",
              text: "${buildStatus}\n${author}",
              "mrkdwn_in": ["fields"],
              fields: [
                  [
                      title: "Branch",
                      value: "${env.GIT_BRANCH}",
                      short: true
                  ],
                  [
                      title: "Test Results",
                      value: "${testSummary}",
                      short: true
                  ],
                  [
                      title: "Last Commit",
                      value: "${message}",
                      short: false
                  ]
              ]
          ],
          [
              title: "Failed Tests",
              color: "${buildColor}",
              text: "${failedTestsString}",
              "mrkdwn_in": ["text"],
          ]
      ])
  } else {
      notifySlack("", channel, [
          [
              title: "${jobName}, build #${env.BUILD_NUMBER}",
              title_link: "${env.BUILD_URL}",
              color: "${buildColor}",
              author_name: "${author}",
              text: "${buildStatus}\n${author}",
              fields: [
                  [
                      title: "Branch",
                      value: "${env.GIT_BRANCH}",
                      short: true
                  ],
                  [
                      title: "Test Results",
                      value: "${testSummary}",
                      short: true
                  ],
                  [
                      title: "Last Commit",
                      value: "${message}",
                      short: false
                  ]
              ]
          ]
      ])
  }
}
