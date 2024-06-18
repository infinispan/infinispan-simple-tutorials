package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.api.annotations.indexing.Keyword;
import org.infinispan.protostream.annotations.Proto;

/**
 * This class is annotated with the infinispan Protostream support annotations.
 * With this method, you don't need to define a protobuf file and a marshaller for the object.
 */
@Proto
@Indexed(keyEntity = "tutorial.PersonKey")
public record Person(@Keyword(projectable = true, sortable = true, normalizer = "lowercase", indexNullAs = "unnamed", norms = false)
                     String firstName,
                     @Keyword(projectable = true, sortable = true, normalizer = "lowercase", indexNullAs = "unnamed", norms = false)
                     String lastName,
                     int bornYear,
                     @Keyword(projectable = true, sortable = true, normalizer = "lowercase", indexNullAs = "unnamed", norms = false)
                     String bornIn
) {}