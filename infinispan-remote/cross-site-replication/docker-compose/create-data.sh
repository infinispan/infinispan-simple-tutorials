echo "= Init create data"
echo "= Clear all data"
curl -XDELETE --digest -u admin:password http://localhost:11222/rest/v2/caches/xsiteCache
curl -XDELETE --digest -u admin:password http://localhost:31222/rest/v2/caches/xsiteCache

echo "= Create X-Site Cache"
curl -XPOST --digest -u admin:password -H "Content-Type: application/xml" -d "@xsiteCache.xml" http://localhost:11222/rest/v2/caches/xsiteCache
curl -XPOST --digest -u admin:password -H "Content-Type: application/xml" -d "@ny-backup-xsiteCache.xml" http://localhost:31222/rest/v2/caches/xsiteCache

echo "= Put 10 entries in XSite cache"
for i in {1..10}
do
  URL='http://localhost:11222/rest/v2/caches/xsiteCache/key_'$i
  DATA='{
             "_type": "string",
             "_value": "value_'$i'"
          }'
  curl -XPOST --digest -u admin:password -d "$DATA" -H 'Content-Type: application/json' $URL
done

echo "= End"
