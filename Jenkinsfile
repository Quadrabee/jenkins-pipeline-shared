pipeline {
  agent any

  triggers {
    issueCommentTrigger('.*test this please.*')
  }

  environment {
    SLACK_CHANNEL = '#test-q8s'
    DOCKER_TAG = get_docker_tag()
  }

  stages {

    stage ('Start') {
      steps {
        sendNotifications('STARTED', SLACK_CHANNEL)
      }
    }

    stage('Build') {
      steps {
        sh "echo Building with tag ${DOCKER_TAG}"
      }
    }
  }

  post {
    always {
      newTest(SLACK_CHANNEL)
    }
  }
}

def get_docker_tag() {
  if (env.TAG_NAME != null) {
    return env.TAG_NAME
  }
  return 'latest'
}
