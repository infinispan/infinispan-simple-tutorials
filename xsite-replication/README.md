Cross site replication cluster
=================================================
**Technologies:** Infinispan Server, Cross-Site replication, Console
**Summary:** Run two Infinispan clusters locally to learn how cross-site replication works.

About This Tutorial
-------------------
In this tutorial, you start two Infinispan clusters, LON and NY, that provide backups for each other.

This tutorial demonstrates Infinispan cross-site replication capabilities locally using docker-compose. In production environments, Infinispan cluster backups are typically located in different data centers.

Procedure
----------
1. Make sure you have `docker` and `docker-compose` installed locally.

2. Run `docker-compose up`

    The docker-compose yaml creates the LON and NYC clusters using the `infinispan-xsite.xml` file.
 
3. Access the console of the `LON` cluster site at `http://localhost:11222/console` .

4. Access the console of the `NYC` cluster site at `http://localhost:31222/console`.

5. Run `./create-data.sh` to create some sample data.

    The script creates a cache named `xsiteCache` in the LON cluster that defines the NYC site as a backup.
    It is also worth noting that cache topologies can be different when using cross-site replication. In this 
    example, the cache in LON is distributed while the cache in NYC is replicated.

To go further:
Use the console or the [REST API](https://infinispan.org/docs/stable/titles/rest/rest.html#rest_v2_cache_operations) to 
add some data and see how Infinispan replicates data across the clusters.
