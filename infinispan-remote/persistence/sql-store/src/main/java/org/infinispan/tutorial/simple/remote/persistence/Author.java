package org.infinispan.tutorial.simple.remote.persistence;

import org.infinispan.protostream.annotations.Proto;

@Proto
public record Author(int id, String isbn, String name, String country) {}

