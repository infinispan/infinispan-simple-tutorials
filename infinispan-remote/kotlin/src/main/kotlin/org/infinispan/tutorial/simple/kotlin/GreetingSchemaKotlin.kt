package org.infinispan.tutorial.simple.kotlin

import org.infinispan.protostream.GeneratedSchema
import org.infinispan.protostream.annotations.ProtoSchema

@ProtoSchema(includeClasses = [GreetingKotlin::class])
interface GreetingSchemaKotlin : GeneratedSchema