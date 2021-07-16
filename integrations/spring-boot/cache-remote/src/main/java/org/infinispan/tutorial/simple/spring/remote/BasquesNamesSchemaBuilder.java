package org.infinispan.tutorial.simple.spring.remote;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(schemaPackageName = "tutorial", includeClasses = BasqueName.class)
public interface BasquesNamesSchemaBuilder extends GeneratedSchema {
}
