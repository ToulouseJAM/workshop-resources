#!/usr/bin/env groovy

stage("build & unit tests") {
    node("build") {
        sleep 10
    }
}

stage("static-analysis") {
    node("build") {
        sleep 10
    }
}

stage("acceptance-tests") {

    def tests = [
            "firefox" : {
                node {
                    sleep 10
                }
            },
            "chrome" : {
                node {
                    sleep 10
                }
            },
            "edge" : {
                node {
                    sleep 10
                }
            }
    ]

    parallel tests

}

stage("staging") {
    node {
        sleep 10
    }
}

stage("manual-approval") {
    input "Deploy to production?"
}

stage("deploy") {
    node {
        sleep 10
    }
}
