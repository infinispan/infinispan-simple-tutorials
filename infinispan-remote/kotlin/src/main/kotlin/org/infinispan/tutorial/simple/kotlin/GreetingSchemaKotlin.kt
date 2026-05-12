package org.infinispan.tutorial.simple.kotlin

import org.infinispan.protostream.GeneratedSchema
import org.infinispan.protostream.annotations.ProtoSchema

// tag::proto-schema[]
@ProtoSchema(includeClasses = [GreetingKotlin::class])
interface GreetingSchemaKotlin : GeneratedSchema
// end::proto-schema[]