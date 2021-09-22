# FMTOK8s Payments Service
For more information check [http://github.com/salaboy/from-monolith-to-k8s](http://github.com/salaboy/from-monolith-to-k8s)

This service is simulating a third-party payment service

## APIs


This service expose the following endpoints: 

- POST `/api` to submit a payment for processing. You should submit a `reservationId` in the body of the request. 
- GET `/api/{reservationId}` to check if the payment was processed correctly.  


## Build and Release

```
mvn package
```

```
docker build -t salaboy/fmtok8s-payments-service:0.1.0
docker push salaboy/fmtok8s-payments-service:0.1.0
```

```
cd charts/fmtok8s-payments-service
helm package .
```

Copy tar to http://github.com/salaboy/helm and push