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
                        node {
                            sleep 2
                        }
                    },
                    "chrome" : {
                        node {
                            sleep 2
                        }
                    },
                    "edge" : {
                        node {
                            sleep 2
                        }
                    }
                )
            }
        }

        stage("manual-approval") {
            when {
                branch "master"
            }
            steps {
                input "Sure?"
            }
        }

        stage("staging") {
            agent any
            when {
                branch "master"
            }
            steps {
                deleteDir()
                unstash "binary"
                deploy("root", "STAGING_IP", "target/*.jar")
            }
        }

        stage("prod") {
            agent any
            when {
                branch "master"
            }
            steps {
                deleteDir()
                unstash "binary"
                deploy("root", "PROD_IP", "target/*.jar")
            }
        }
    }
}
