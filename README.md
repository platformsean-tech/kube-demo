



brew install k3d
k3d cluster create dev --servers 1 --agents 2 -p "8080:80@loadbalancer"

➜  kube-demo git:(main) ✗ curl -sfL https://get.k3s.io | INSTALL_K3S_EXEC="server \
 --tls-san 192.168.18.107 \
 --node-ip 192.168.18.107 \
 --bind-address 0.0.0.0 \
 --advertise-address 192.168.18.107" sh -



https://www.baeldung.com/jboss-undertow
