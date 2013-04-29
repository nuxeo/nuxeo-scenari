NUXEO_URL=http://localhost:8080
LOGIN=Administrator
PWD=Administrator

curl -v -X POST -H "Content-Type:application/zip" -u $LOGIN:$PWD $NUXEO_URL/nuxeo/site/scenari/upload --data-binary @$1
