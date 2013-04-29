NUXEO_URL=http://localhost:8080
LOGIN=Administrator
PWD=Administrator

curl -i -u $LOGIN:$PWD $NUXEO_URL/nuxeo/site/scenari/importscreen/default/$1
