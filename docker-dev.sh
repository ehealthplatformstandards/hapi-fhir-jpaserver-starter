docker build --no-cache -t dte-hapi-fhir:dev .

mkdir -p docker

docker save -o docker/dte-hapi-fhir-dev.tar dte-hapi-fhir:dev

read -p "Done - Tape Enter..."