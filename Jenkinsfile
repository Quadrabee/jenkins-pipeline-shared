pipeline {
  agent any

  triggers {
    issueCommentTrigger('.*test this please.*')
  }

  environment {
    SLACK_CHANNEL = '#test-q8s'
  }

  stages {

    stage ('channel') {
      steps {
        sendNotifications('STARTED', SLACK_CHANNEL)
      }
    }

    stage('commiters') {
      steps {
        slackSend notifyCommitters: true, message: "Build done"
      }
    }

    stage ('@llambeau') {
      steps {
        slackSend(color: 'good', message: 'just a test @llambeau', channel: SLACK_CHANNEL)
      }
    }
  }
  
  post {
    success {
      sendNotifications('SUCCESS', SLACK_CHANNEL)
    }
    failure {
      sendNotifications('FAILED', SLACK_CHANNEL)
    }
  }
}


