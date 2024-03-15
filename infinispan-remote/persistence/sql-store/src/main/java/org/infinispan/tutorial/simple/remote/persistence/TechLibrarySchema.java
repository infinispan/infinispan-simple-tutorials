package org.infinispan.tutorial.simple.remote.persistence;

import org.infinispan.protostream.GeneratedSchema;
import org.infinispan.protostream.annotations.ProtoSchema;

@ProtoSchema(schemaPackageName = "library", includeClasses = {Book.class, Author.class})
public interface TechLibrarySchema extends GeneratedSchema {
}
