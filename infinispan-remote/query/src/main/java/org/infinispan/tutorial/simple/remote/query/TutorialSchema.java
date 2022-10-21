package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.AutoProtoSchemaBuilder;

@AutoProtoSchemaBuilder(schemaPackageName = "tutorial", includeClasses = Person.class)
public interface TutorialSchema extends GeneratedSchema {
}
