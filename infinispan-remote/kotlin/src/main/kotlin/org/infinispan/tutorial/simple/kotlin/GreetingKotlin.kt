package org.infinispan.tutorial.simple.kotlin

import org.infinispan.protostream.annotations.Proto

@Proto
data class GreetingKotlin(@JvmField var name: String?=null,
                          @JvmField var greeting: String?=null)
