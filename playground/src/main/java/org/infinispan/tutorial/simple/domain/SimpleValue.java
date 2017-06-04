/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.infinispan.tutorial.simple.domain;

import org.infinispan.commons.marshall.AdvancedExternalizer;
import org.infinispan.commons.marshall.SerializeWith;
import org.infinispan.commons.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Set;

@SerializeWith(SimpleValue.SimpleValueExternalizer.class)
public class SimpleValue implements Value {

    public SimpleValue(String val) {
      this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }

    private String val;

    public static class SimpleValueExternalizer implements AdvancedExternalizer<SimpleValue> {

        private final static Logger log = LoggerFactory.getLogger(SimpleValueExternalizer.class);

        @Override
        public void writeObject(ObjectOutput objectOutput, SimpleValue simpleValue) throws IOException {
            log.debug("AdvancedExternalizer writing object SimpleValue [" + simpleValue.val + "]");
            objectOutput.writeUTF(simpleValue.val);
        }

        @Override
        public SimpleValue readObject(ObjectInput objectInput) throws IOException, ClassNotFoundException {
            log.debug("AdvancedExternalizer reading object SimpleValue");
            return new SimpleValue(objectInput.readUTF());
        }

        @Override
        public Set<Class<? extends SimpleValue>> getTypeClasses() {
            return Util.<Class<? extends SimpleValue>>asSet(SimpleValue.class);
        }

        @Override
        public Integer getId() {
            return 1;
        }
    }
}
