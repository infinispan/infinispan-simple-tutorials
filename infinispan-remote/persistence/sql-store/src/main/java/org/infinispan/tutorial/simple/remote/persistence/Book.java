package org.infinispan.tutorial.simple.remote.persistence;

import org.infinispan.protostream.annotations.Proto;

@Proto
public record Book(String isbn, String title, String authorName, String country) {}
