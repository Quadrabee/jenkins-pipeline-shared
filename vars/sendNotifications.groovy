#!/usr/bin/env groovy

/**
 * Send notifications based on build status string
 */
def call(String buildStatus = 'STARTED', String channel, String subjectOverride = null) {
  // build status of null means successful
  buildStatus = buildStatus ?: 'SUCCESS'

  // Default values
  def buildPhase = 'failed';
  def color = '#c21f2d'

  // Override default values based on build status
  if (buildStatus == 'STARTED') {
    color = '#d59e27'
    buildPhase = 'started';
  } else if (buildStatus == 'SUCCESS') {
    color = '#349b4a'
    buildPhase = 'succeeded';
  }

  def subject = subjectOverride ?: "BuildJob ${buildPhase} (_${env.JOB_NAME}_)"

  if (env.TAG_NAME) {
    subject = "BuildJob ${buildPhase} for release *${env.TAG_NAME}*"
  } else if (env.CHANGE_ID) {
    try {
      subject = "BuildJob ${buildPhase} for PR *<${pullRequest.url}|#${pullRequest.number} ${pullRequest.title}>*"
    } catch (e) {
      if (env.CHANGE_TITLE && env.CHANGE_URL) {
        subject = "BuildJob ${buildPhase} for PR *<${env.CHANGE_URL}|#${env.CHANGE_ID} ${env.CHANGE_TITLE}>*"
      } else {
        subject = "BuildJob ${buildPhase} for PR '${env.CHANGE_ID}'"
      }
    }
  } else if (env.BRANCH_NAME) {
    subject = "BuildJob ${buildPhase} on branch '${env.BRANCH_NAME}' (_${env.JOB_NAME}_)"
  }

  def summary = "<${env.BUILD_URL}|See build logs>"

  def blocks = [
    [
      "type": "section",
      "text": [
        "type": "mrkdwn",
        "text": subject
      ]
    ],
    [
      "type": "context",
      "elements": [
        // [
        //   "type": "image",
        //   "image_url": "https://img2.freepng.es/20180515/zxe/kisspng-jenkins-docker-continuous-delivery-installation-so-5afa799e222331.1197773615263645741398.jpg",
        //   "alt_text": "images"
        // ],
        [
          "type": "mrkdwn",
          "text": summary
        ]
      ]
    ]
  ]

  def attachments = [
    [
      "blocks": blocks,
      "color": color
    ]
  ]

  // Send notifications
  slackSend (attachments: attachments, channel: channel)

  // Warn/Greet committers for fail only
  try {
    if (buildPhase == 'failed') {
      slackSend (attachments: attachments, notifyCommitters: true)
    }
  } catch (e) {
  }
}
