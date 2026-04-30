package org.infinispan.tutorial.simple.remote.vectorsearch;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;

@ProtoSchema(includeClasses = Beer.class, schemaPackageName = "quickstart")
public interface BeerSchema extends GeneratedSchema {
}
