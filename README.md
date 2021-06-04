# Knative on EKS

## Introduction

Pending...

## EKS cluster creation

We will use [eksctl]() to create a kubernetes cluster on [EKS](https://aws.amazon.com/eks/). By default the cluster is
created with two `m5.large` worker nodes. 

```shell
eksctl create cluster
```

## Kafka installation

We will use [Strimzi](https://strimzi.io/) to install a single node Kafka and Zookeeper on Kubernetes.

```shell
export STRIMZI_VERSION=0.23.0

kubectl create namespace kafka
kubectl create -f https://strimzi.io/install/$STRIMZI_VERSION?namespace=kafka -n kafka
kubectl apply -f https://strimzi.io/examples/$STRIMZI_VERSION/kafka/kafka-persistent-single.yaml -n kafka

kubectl wait kafka/my-cluster --for=condition=Ready --timeout=300s -n kafka
```

## Knative Serving and Kourier installation

We will use [Kourier](https://github.com/knative-sandbox/net-kourier) as Ingress for Knative Serving. Kourier is a 
lightweight alternative for the Istio ingress as its deployment consists only of an Envoy proxy and a control plane for 
it.

```shell
export KNATIVE_VERSION=v0.23.0

kubectl apply -f https://github.com/knative/serving/releases/download/$KNATIVE_VERSION/serving-crds.yaml
kubectl apply -f https://github.com/knative/serving/releases/download/$KNATIVE_VERSION/serving-core.yaml
kubectl wait deployment --all --timeout=-1s --for=condition=Available -n knative-serving

kubectl apply -f https://github.com/knative/net-kourier/releases/download/$KNATIVE_VERSION/kourier.yaml
kubectl wait deployment --all --timeout=-1s --for=condition=Available -n kourier-system
kubectl wait deployment --all --timeout=-1s --for=condition=Available -n knative-serving

kubectl patch configmap/config-network \
  --namespace knative-serving \
  --type merge \
  --patch '{"data":{"ingress.class":"kourier.ingress.networking.knative.dev"}}'

# Configured for EKS  
export EXTERNAL_IP=kubectl --namespace kourier-system get service kourier -o jsonpath='{.status.loadBalancer.ingress[0].hostname}'

# Configure a real DNS (kcdspain.arima.eu) 
# For other alternatives: https://knative.dev/docs/install/install-serving-with-yaml/#configure-dns
kubectl patch configmap/config-domain \
  --namespace knative-serving \
  --type merge \
  --patch '{"data":{"kcdspain.arima.eu":""}}'
```

## Knative Eventing installation

We will do the simplest installation of Knative Eventing, without a broker and a messaging layer.

```shell
export KNATIVE_VERSION=v0.23.0

kubectl apply -f https://github.com/knative/eventing/releases/download/$KNATIVE_VERSION/eventing-crds.yaml
kubectl apply -f https://github.com/knative/eventing/releases/download/$KNATIVE_VERSION/eventing-core.yaml
kubectl wait deployment --all --timeout=-1s --for=condition=Available -n knative-eventing
```

## Building and Deployment

### Knative Serving demo application 

Build container image with:

```shell
./mvnw compile jib:build
```

Deploy with:

```shell
# TODO set latest version
kn service create demo-knative-serving --image=itelleria/demo-knative-serving:0.0.1-SNAPSHOT
```

Check that the application is running:

```shell
curl http://demo-knative-serving.default.kcdspain.arima.eu/actuator/health
```

### Knative Eventing demo application 

Build container image with:

```shell
./mvnw compile jib:build
```

Deploy with:

```shell
# TODO set latest version
kn service create demo-knative-eventing --image=itelleria/demo-knative-eventing:0.0.1-SNAPSHOT
```

Create a Kafka Event Source:

```shell
kubectl apply -f measures-kafka-source.yaml
```

## Load Tests execution

Pending...
