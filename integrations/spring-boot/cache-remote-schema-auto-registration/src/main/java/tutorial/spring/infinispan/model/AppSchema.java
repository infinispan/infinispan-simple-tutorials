package tutorial.spring.infinispan.model;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;

// tag::schema[]
@ProtoSchema(schemaPackageName = "tutorial",
      includeClasses = {Character.class, Archetype.class},
      dependsOn = {
            org.infinispan.protostream.types.java.CommonTypes.class
      })
public interface AppSchema extends GeneratedSchema {
}
// end::schema[]
