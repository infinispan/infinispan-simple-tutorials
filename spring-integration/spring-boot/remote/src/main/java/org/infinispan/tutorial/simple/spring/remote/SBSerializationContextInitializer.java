package org.infinispan.tutorial.simple.spring.remote;

import org.infinispan.protostream.SerializationContextInitializer;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(schemaPackageName = "simple",
      includeClasses = {BasqueName.class})
public interface SBSerializationContextInitializer extends SerializationContextInitializer {
}
