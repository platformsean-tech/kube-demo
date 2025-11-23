
k8s_yaml('k8s/namespace.yaml')

#
k8s_yaml([
    'k8s/wallet/deployment.yaml',
    'k8s/wallet/service.yaml',
    'k8s/engine/deployment.yaml',
    'k8s/engine/service.yaml',
])


#rebuilds on source changes
docker_build(
    'wallet:latest',
    './wallet',
    dockerfile='./wallet/Dockerfile',
    ignore=[
        'wallet/build/',
        'wallet/.gradle/',
        'wallet/gradle/wrapper/gradle-wrapper.jar',
    ],
)

k8s_resource(
    'wallet',
    labels=['wallet'],
    port_forwards=8081,
)

# Engine service - rebuilds on source changes
docker_build(
    'engine:latest',
    './engine',
    dockerfile='./engine/Dockerfile',
    ignore=[
        'engine/build/',
        'engine/.gradle/',
        'engine/gradle/wrapper/gradle-wrapper.jar',
    ],
)

k8s_resource(
    'engine',
    labels=['engine'],
    port_forwards=8082,
)
