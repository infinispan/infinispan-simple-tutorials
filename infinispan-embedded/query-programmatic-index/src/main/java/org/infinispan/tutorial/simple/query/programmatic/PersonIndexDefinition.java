package org.infinispan.tutorial.simple.query.programmatic;

import org.hibernate.search.mapper.pojo.bridge.mapping.programmatic.TypeBinder;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.ProgrammaticMappingConfigurationContext;
import org.hibernate.search.mapper.pojo.mapping.definition.programmatic.TypeMappingStep;
import org.infinispan.search.mapper.mapping.MappingConfigurationContext;
import org.infinispan.search.mapper.mapping.ProgrammaticSearchMappingProvider;

public class PersonIndexDefinition implements ProgrammaticSearchMappingProvider {

    @Override
    public void configure(MappingConfigurationContext context) {
        ProgrammaticMappingConfigurationContext programmaticMappingConfigurationContext = context.programmaticMapping();
        TypeMappingStep type = programmaticMappingConfigurationContext.type(Person.class);
        type.indexed().enabled(true);
        type.property("name").fullTextField();
        type.property("surname").fullTextField();
    }
}
