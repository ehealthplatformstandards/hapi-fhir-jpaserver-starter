docker build --no-cache -t dte-hapi .

mkdir -p docker

docker save -o docker/dte-hapi.tar dte-hapi

pause