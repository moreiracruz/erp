package com.erp.infrastructure;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.erp.modules")
public class ModuleBoundaryTest {

    @ArchTest
    static final ArchRule auth_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("com.erp.modules.auth..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "com.erp.modules.product..",
                "com.erp.modules.inventory..",
                "com.erp.modules.sales..",
                "com.erp.modules.customer..",
                "com.erp.modules.finance..",
                "com.erp.modules.pricing.."
            );

    @ArchTest
    static final ArchRule product_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("com.erp.modules.product..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "com.erp.modules.auth..",
                "com.erp.modules.inventory..",
                "com.erp.modules.sales..",
                "com.erp.modules.customer..",
                "com.erp.modules.finance..",
                "com.erp.modules.pricing.."
            );

    @ArchTest
    static final ArchRule inventory_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("com.erp.modules.inventory..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "com.erp.modules.auth..",
                "com.erp.modules.product..",
                "com.erp.modules.sales..",
                "com.erp.modules.customer..",
                "com.erp.modules.finance..",
                "com.erp.modules.pricing.."
            );

    @ArchTest
    static final ArchRule sales_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("com.erp.modules.sales..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "com.erp.modules.auth..",
                "com.erp.modules.product..",
                "com.erp.modules.customer..",
                "com.erp.modules.finance..",
                "com.erp.modules.pricing.."
            );

    @ArchTest
    static final ArchRule customer_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("com.erp.modules.customer..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "com.erp.modules.auth..",
                "com.erp.modules.product..",
                "com.erp.modules.inventory..",
                "com.erp.modules.sales..",
                "com.erp.modules.finance..",
                "com.erp.modules.pricing.."
            );

    @ArchTest
    static final ArchRule finance_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("com.erp.modules.finance..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "com.erp.modules.auth..",
                "com.erp.modules.product..",
                "com.erp.modules.inventory..",
                "com.erp.modules.sales..",
                "com.erp.modules.customer..",
                "com.erp.modules.pricing.."
            );

    @ArchTest
    static final ArchRule pricing_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("com.erp.modules.pricing..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "com.erp.modules.auth..",
                "com.erp.modules.product..",
                "com.erp.modules.inventory..",
                "com.erp.modules.sales..",
                "com.erp.modules.customer..",
                "com.erp.modules.finance.."
            );
}
