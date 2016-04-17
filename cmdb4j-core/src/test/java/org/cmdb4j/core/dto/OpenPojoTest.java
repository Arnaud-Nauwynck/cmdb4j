package org.cmdb4j.core.dto;

import java.util.List;

import org.junit.Test;

import com.openpojo.reflection.PojoClass;
import com.openpojo.reflection.PojoClassFilter;
import com.openpojo.reflection.filters.FilterChain;
import com.openpojo.reflection.filters.FilterPackageInfo;
import com.openpojo.reflection.impl.PojoClassFactory;
import com.openpojo.validation.Validator;
import com.openpojo.validation.ValidatorBuilder;
import com.openpojo.validation.rule.impl.GetterMustExistRule;
import com.openpojo.validation.rule.impl.SetterMustExistRule;
import com.openpojo.validation.test.impl.GetterTester;
import com.openpojo.validation.test.impl.SetterTester;

public class OpenPojoTest {

    @Test
    public void testPojoStructureAndBehavior() {
        Validator validator = ValidatorBuilder.create() //
                .with(new GetterMustExistRule())
                .with(new SetterMustExistRule()) // validation
                .with(new SetterTester())
                .with(new GetterTester()) // test execution
                .build();
        String packageName = "org.cmdb4j.core.dto";
        FilterPackageInfo pojoFilter = new FilterPackageInfo();
        
        PojoClassFilter pojoClassFilter = new FilterChain(pojoFilter);
        List<PojoClass> pojoClasses = PojoClassFactory.getPojoClassesRecursively(packageName, pojoClassFilter);
        
        validator.validate(pojoClasses);
    }
}
