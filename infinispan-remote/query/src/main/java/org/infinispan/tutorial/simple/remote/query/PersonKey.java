package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.protostream.annotations.Proto;

@Proto
@Indexed
public record PersonKey(@Basic(projectable = true) String id,
                        @Basic(projectable = true) String pseudo) {
}
