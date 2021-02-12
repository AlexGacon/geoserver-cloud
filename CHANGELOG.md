# Cloud Native GeoServer Changelog

## Relase 0.2.0, February 11, 2021

`v0.2.0` code freeze by Dec 31, 2020. Internal priority shifts delayed the release until now, though the system has been deployed in production using Kubernetes since 0.1.0 by end of August 2020.

### New catalog-service

`v0.2.0`'s most important update is the incorporation of the `catalog` service and the inclusion of, a microservice sitting in between the front-services (wms, wfs, rest, etc) and the actual catalog back-end (still `jdbcconfig`). 

The `catalog-service` is a reactive web service based on [String Webflux](https://docs.spring.io/spring-framework/docs/current/reference/html/web-reactive.html) and scales well both vertically and horizontally.

The `catalog-service` back-end is supported by a [Reactive Feign](https://github.com/Playtika/feign-reactive) client.

### Catalog/config pluggable architecture

sfsdfsd

### Catalog/config caching

sdfsdfsd

Catalog and configuration change events are propagated to the event-bus, and all the nodes in the cluster react accordingly, cleaning up

### Improved configuration

Common service configuration properties have been collapsed to a single `application.yml` configuration file (see [./config/application.yml]

### Remove events JSON payload encoding

### Added importer extension to the `web-ui` service

### Continuous integration build job

--- 

### Release 0.1.0, August 30, 2020

Still an early stage, working towards release [0.2.0](https://github.com/camptocamp/geoserver-microservices/projects/1).

Passed the feasibility exploration prototype phase by `0.1.0` and we're confident this is a good path towards the main objective of having cloud-native,
independently scalable GeoServer microservices.

`0.2.0` will focus on wrapping up integration improvements related to catalog and configuration back-end pluggability, including a microservice to serve as the catalog and resource store for all front-end services.

We're currently using GeoServer's `jdbcconfig` and `jdbcstore` community modules to host the catalog and resources on a PostgreSQL database, included in the docker-composition provided at the root directory. If you spawn too many instances (e.g. `docker-compose scale wfs=10`), some will fail to connect as the maximum number of allowed database server connections is reached by the aggregated connection pools from all service instances. The `catalog-service` will solve this situation, providing a unified abstraction layer for the rest of services, so that the actual catalog backend needs to be configured only on the `catalog-service`, while its clients use a catalog and resource store plugin that talks to the `catalog-service`.

The use `jdbcconfig` and `jdbcstore` has been customized to allow for spring-boot style configuration through `application.yml` or `application.properties` files, served up by the `config-service` from the `config/` directory. That said, it is envisioned to develop alternate, more scalable, catalog backend plugins.

We're using [spring-cloud-bus](https://cloud.spring.io/spring-cloud-static/spring-cloud-bus/3.0.0.M1/reference/html/) to coordinate configuration changes across services in the cluster.

By [1.0.0](https://github.com/camptocamp/geoserver-microservices/milestone/2) at the end of September 2020 we should be able to deploying to Kubernetes using Kubernetes native services for service discovery and externalized configuration.

Nonetheless the chosen architecture aims to allow for a number of containerized application platforms such as:

- Single server docker-compose 
- Kubernetes
- Docker swarm

And deployment/configuration choices like:

- Shared data directory
- Per-service data directory with event-based synchronization
- Per-service access to catalog/config backend (e.g. all of them connecting directly to the config database)
- Catalog microservice: all services use the `catalog-service` client as their catalog, and this in turn talks to the actual backend, which, since the `catalog-service` itself can be scaled out, its backend can be any of the above options.

Feel free to try it out following the above instructions and report bugs [here](https://github.com/camptocamp/geoserver-microservices/issues).

