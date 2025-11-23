default_registry('ttl.sh/sean-tilt')

allow_k8s_contexts('default')  
k8s_yaml('k8s/namespace.yaml')

k8s_yaml([
    'k8s/wallet/deployment.yaml',
    'k8s/wallet/service.yaml',
    'k8s/engine/deployment.yaml',
    'k8s/engine/service.yaml',
    'k8s/bonus/deployment.yaml',
    'k8s/bonus/service.yaml',
])

#LAUNCHER
docker_build(
    'ghcr.io/platformsean-tech/launcher:latest',
    context='./launcher',
    dockerfile='./launcher/Dockerfile',
    ignore=[
        'launcher/build/',
        'launcher/.gradle/',
        'launcher/gradle/wrapper/gradle-wrapper.jar',
    ],
)

#WALLET
docker_build(
    'wallet:latest',
    context='./wallet',
    dockerfile='./wallet/Dockerfile',
    ignore=[
        'wallet/build/',
        'wallet/.gradle/',
        'wallet/gradle/wrapper/gradle-wrapper.jar',
    ],
    live_update=[
        sync('./wallet/build/libs/wallet.war', '/app/wallet.war'),
    ],
)

k8s_resource(
    'wallet',
    labels=['wallet'],
    port_forwards='8081:8080',

)

#ENGINE
docker_build(
    'engine:latest',
    context='./engine',
    dockerfile='./engine/Dockerfile',
    ignore=[
        'engine/build/',
        'engine/.gradle/',
        'engine/gradle/wrapper/gradle-wrapper.jar',
    ],
    live_update=[
        sync('./engine/build/libs/engine.war', '/app/engine.war'),
    ],
)

k8s_resource(
    'engine',
    labels=['engine'],
    port_forwards='8082:8080',

)

#BONUS
docker_build(
    'bonus:latest',
    context='./bonus',
    dockerfile='./bonus/Dockerfile',
)

k8s_resource(
    'bonus',
    labels=['bonus'],
    port_forwards='8083:8080',
)
