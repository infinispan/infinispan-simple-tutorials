echo "= Init create data"
echo "= Clear all data"
curl -XDELETE  -u use:pass http://localhost:11222/rest/v2/caches/xsiteCache
curl -XDELETE  -u use:pass http://localhost:31222/rest/v2/caches/xsiteCache

echo "= Create X-Site Cache"
curl -XPOST  -u use:pass -H "Content-Type: application/xml" -d "@xsiteCache.xml" http://localhost:11222/rest/v2/caches/xsiteCache
curl -XPOST  -u use:pass -H "Content-Type: application/xml" -d "@ny-backup-xsiteCache.xml" http://localhost:31222/rest/v2/caches/xsiteCache

echo "= Put 10 entries in XSite cache"
for i in {1..10}
do
  URL="http://localhost:11222/rest/v2/caches/xsiteCache/$i"
  DATA="data-$i"
  curl -XPOST  -u use:pass -d $DATA $URL
done

echo "= End"
