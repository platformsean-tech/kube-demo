



brew install k3d
k3d cluster create dev --servers 1 --agents 2 -p "8080:80@loadbalancer"


