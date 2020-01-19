def call(String channel, Closure body) {
  sendNotifications('STARTED', channel)
  try {
    body()
    sendNotifications('SUCCESS', channel)
  } catch (err) {
    sendNotifications('FAILED', channel)
    error "Build failed, caught exception: ${err}"
  }
}