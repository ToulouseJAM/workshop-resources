#!/usr/bin/env groovy

pipeline {

    agent none

    stages {

        stage("build & unit tests") {
            agent { label "build "}
            steps {
                withMaven(maven: "M3") {
                    sh "mvn clean install"
                }
            }
            post {
                success {
                    stash includes: 'target/*.jar', name: 'binary'
                }
            }
        }

        stage("static-analysis") {
            agent { label "build "}
            steps {
                withMaven(maven: "M3") {
                    withSonarQubeEnv("sonarqube") {
                        sh "mvn sonar:sonar"
                    }
                }
            }
        }

        stage("test") {
            agent any
            steps {
                parallel (
                    "firefox" : {
                        node ("test") {
                            sleep 2
                        }
                    },
                    "chrome" : {
                        node ("test") {
                            sleep 2
                        }
                    },
                    "edge" : {
                        node ("test"){
                            sleep 2
                        }
                    }
                )
            }
        }

        stage("manual-approval") {
            steps {
                input "Sure?"
            }
        }

        stage("staging") {
            agent any
            steps {
                deleteDir()
                unstash "binary"
                sh "ls -l target"
            }
        }

        stage("prod") {
            agent any
            steps {
                deleteDir()
                unstash "binary"
                sh "ls -l target"
            }
        }
    }
}
