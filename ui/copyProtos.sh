rm -r protos
if [ -d "src/generated" ]; then
  rm -r src/generated
fi
rsync -rR ../**/src/main/proto/./com/example/**/api/*.proto protos
rsync -rR ../**/src/main/proto/./com/example/**/domain/*.proto protos
rsync -rR ../gateway/src/main/proto/./com/example/gateway/**/*.proto protos
rsync -rR ../common/src/main/proto/./com/example/common/**/*.proto protos
rsync -rR ../common/src/main/proto/./com/example/common/*.proto protos
rsync -rR ../gateway/target/protobuf_external/./**/*.proto protos
rsync -rR ../gateway/target/protobuf_external/./**/**/*.proto protos
rm protos/google/api/service.proto