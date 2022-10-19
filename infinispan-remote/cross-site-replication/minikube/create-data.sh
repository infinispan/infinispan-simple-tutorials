export LON_SITE_EXTERNAL=$(minikube service --url example-cluster-lon-external)

echo "= Put 10 entries in LON cache"
for i in {1..10}
do
  URL=$LON_SITE_EXTERNAL'/rest/v2/caches/mycache/key_'-$i
  DATA='{
           "_type": "string",
           "_value": "value_'$i'"
        }'
  curl -XPOST --digest -u admin:password -d "$DATA" -H 'Content-Type: application/json' $URL
done

echo "= End"
