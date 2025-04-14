package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;

@ProtoSchema(schemaPackageName = "tutorial", includeClasses = { Team.class, Person.class, PersonKey.class })
public interface TutorialSchema extends GeneratedSchema {
}
