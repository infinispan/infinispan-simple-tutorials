package org.infinispan.tutorial.model;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;

@ProtoSchema(schemaPackageName = "tutorial",
        includeClasses = {Character.class, Archetype.class},
        dependsOn = {
                org.infinispan.protostream.types.java.CommonTypes.class
        })
public interface AppSchema extends GeneratedSchema {
}
