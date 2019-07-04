def call() {
    node {

        stage('Checkout') {
            checkout scm
        }

         def p = pipelineCfg()


        stage('Prerequistes'){
            sh " export ${p.CLUSTER_CONFIG}"
            sh " cp /var/lib/jenkins/${p.CLUSTER_CONFIG} ${workspace}/"
            serviceName = sh (
                    script: "echo ${p.SERVICE_NAME} |  cut -d '-' -f 1",
                    returnStdout: true
                ).trim()
        }

        // stage('Updating serviceName') {
        //     serviceName = sh (
        //             script: "echo ${p.SERVICE_NAME} |  cut -d '-' -f 1",
        //             returnStdout: true
        //         ).trim()
        // }

        // stage('current image service tag') {
        //     previousVersion = sh (
        //             script: "kubectl describe deployment p.SERVICE_NAME -n p.ENVIRONMENT_NAME --kubeconfig=p.CLUSTER_CONFIG| grep Image | awk -F \"/noonpay_development/p.SERVICE_NAME:\" \'{print $2}\' ",
        //             returnStdout: true
        //         ).trim()            
        // }

        stage('Build & Test') {
                sh "/opt/maven/bin/mvn --version"
                sh "/opt/maven/bin/mvn clean install"
        }

        stage ('Push Docker Image') {
            docker.withRegistry('https://registry-intl.me-east-1.aliyuncs.com', 'dockerhub') {
                sh "docker build -t ${p.REGISTRY_PATH}/${p.SERVICE_NAME}:${BUILD_NUMBER} ."
                sh "docker push ${p.REGISTRY_PATH}/${p.SERVICE_NAME}:${BUILD_NUMBER}"
            }
        }

        stage ('Deploy') {
            echo "We are going to deploy ${p.SERVICE_NAME}"
            sh "kubectl set image deployment/${p.SERVICE_NAME} ${p.SERVICE_NAME}=${p.REGISTRY_PATH}/${p.SERVICE_NAME}:${BUILD_NUMBER} -n ${p.ENVIRONMENT_NAME} --kubeconfig=${p.CLUSTER_CONFIG}"
            sh "kubectl rollout status deployment/${p.SERVICE_NAME} -n ${p.ENVIRONMENT_NAME} --kubeconfig=${p.CLUSTER_CONFIG}"
        }

    }
}