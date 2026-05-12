package org.infinispan.tutorial.simple.kotlin

import org.infinispan.protostream.annotations.Proto

// tag::proto-entity[]
@Proto
data class GreetingKotlin(@JvmField var name: String?=null,
                          @JvmField var greeting: String?=null)
// end::proto-entity[]
