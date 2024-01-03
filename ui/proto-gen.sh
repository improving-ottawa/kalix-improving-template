#!/usr/bin/env bash
IFS=$' \t\r\n'

SCRIPTDIR="$( cd -- "$(dirname "$0")" >/dev/null 2>&1 ; pwd -P )"
pushd "$SCRIPTDIR"

function download_protoc-gen-grpc-web() {
  echo "Installing protoc-gen-grpc-web..."
    request_url="https://github.com/grpc/grpc-web/releases/download/1.4.2/protoc-gen-grpc-web-1.4.2-linux-x86_64"
    unclean_download_url=`curl -s --head -i "$request_url" | grep -i location | cut -d ' ' -f 2`
    download_url="$(echo -n $unclean_download_url)"

    curl "$download_url" --output protoc-gen-grpc-web
    chmod a+x protoc-gen-grpc-web
    mv protoc-gen-grpc-web /usr/local/bin/protoc-gen-grpc-web
    rm protoc-gen-grpc-web
}

# intended for CI/CD environments (which are Linux)
found_protoc_gen="$(which protoc-gen-grpc-web)"

if [[ -z "$found_protoc_gen" && "$OSTYPE" == 'linux-gnu'* ]]; then
  download_protoc-gen-grpc-web
fi

./copyProtos.sh && mkdir -p protos && mkdir -p src/generated && \
protoc -I=protos $(find protos -iname "*.proto") \
  --js_out=import_style=commonjs,binary:src/generated \
  --grpc-web_out=import_style=typescript,mode=grpcweb:src/generated \
  --experimental_allow_proto3_optional=true

exit_code="$?"
popd

# preserve the exit code of the main generate command
exit $exit_code
