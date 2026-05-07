#!/bin/sh
set -e

TUTORIALS_DIR="${1:-$(pwd)}"
WEBSITE_DIR="${2:-$(pwd)/../infinispan.github.io}"

echo "Building guides from: $TUTORIALS_DIR"
echo "Deploying to website: $WEBSITE_DIR"

cd "$TUTORIALS_DIR"
./mvnw -Pguides -pl docs-maven-plugin package -DskipTests -q

cp target/guides/index.yaml "$WEBSITE_DIR/_data/guides.yaml"
mkdir -p "$WEBSITE_DIR/_guides"
cp target/guides/guides/*.adoc "$WEBSITE_DIR/_guides/"

echo "Done. $(ls target/guides/guides/*.adoc | wc -l | tr -d ' ') guides deployed."
