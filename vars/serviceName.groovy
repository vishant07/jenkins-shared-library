def call() {
    serviceName = sh (
        script: "echo ${SERVICE_NAME} |  cut -d '-' -f 1",
        returnStdout: true
        ).trim()
    echo "${serviceName}"
}

// def call(String name) {
//     // you can call any valid step functions from your code, just like you can from Pipeline scripts
//     echo "Hello world, ${name}"
// }