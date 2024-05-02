package org.infinispan.tutorial.simple.spring.remote;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;

@ProtoSchema(schemaPackageName = "tutorial", includeClasses = BasqueName.class)
public interface BasquesNamesSchemaBuilder extends GeneratedSchema {
}
