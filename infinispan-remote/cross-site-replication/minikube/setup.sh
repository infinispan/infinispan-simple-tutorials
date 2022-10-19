# Starts Minikube
echo 'Starting Minikube'
minikube start --driver=virtualbox --cpus 4 --memory "8192mb"

# Installs Operator in Minikube
echo 'Installing the Operator in Minikube'
curl -sL https://github.com/operator-framework/operator-lifecycle-manager/releases/download/v0.22.0/install.sh | bash -s v0.22.0
kubectl create -f https://operatorhub.io/install/infinispan.yaml
kubectl get csv
sleep 30
echo 'Operator Ready'

# Identity (admin/password), Clusters and Caches
 echo 'Using the Operator to create Identities, LON + NYC clusters and caches'
kubectl create -f identities_lon.yaml
kubectl create -f identities_nyc.yaml
kubectl apply -f LON.yaml
kubectl apply -f NYC.yaml
kubectl apply -f cache_lon.yaml
kubectl apply -f cache_nyc.yaml

# Checks these clusters are up
kubectl wait --timeout=120s --for=condition=CrossSiteViewFormed infinispan example-cluster-lon
kubectl wait --timeout=120s --for=condition=CrossSiteViewFormed infinispan example-cluster-nyc

echo 'Services are ready'
minikube service list