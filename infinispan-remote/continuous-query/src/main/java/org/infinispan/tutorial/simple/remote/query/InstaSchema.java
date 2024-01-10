package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(schemaFileName = "instapost.proto",
      schemaPackageName = "tutorial",
      includeClasses = InstaPost.class)
public interface InstaSchema extends GeneratedSchema {
}
