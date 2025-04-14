package org.infinispan.tutorial.simple.remote.query;

import org.infinispan.api.annotations.indexing.Basic;
import org.infinispan.api.annotations.indexing.Embedded;
import org.infinispan.api.annotations.indexing.Indexed;
import org.infinispan.protostream.annotations.Proto;

import java.util.List;

@Proto
@Indexed
public record Team(
        @Basic(projectable = true)
        String teamName,
        @Basic(projectable = true)
        Integer points,
        @Embedded
        List<Person> players){
}
