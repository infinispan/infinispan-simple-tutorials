package org.infinispan.tutorial.simple.remote.persistence;

import org.infinispan.protostream.annotations.Proto;

// tag::entities[]
@Proto
public record Author(int id, String isbn, String name, String country) {}
// end::entities[]
