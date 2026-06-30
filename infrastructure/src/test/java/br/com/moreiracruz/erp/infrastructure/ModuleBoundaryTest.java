package br.com.moreiracruz.erp.infrastructure;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "br.com.moreiracruz.erp")
public class ModuleBoundaryTest {

    @ArchTest
    static final ArchRule domain_should_not_depend_on_spring =
        noClasses().that().resideInAnyPackage(
                "br.com.moreiracruz.erp.modules..domain..",
                "br.com.moreiracruz.erp.shared.kernel..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "org.springframework..",
                "jakarta.persistence..",
                "javax.persistence.."
            );

    @ArchTest
    static final ArchRule domain_models_should_not_be_jpa_entities =
        noClasses().that().resideInAnyPackage(
                "br.com.moreiracruz.erp.modules..domain..",
                "br.com.moreiracruz.erp.shared.kernel..")
            .should().beAnnotatedWith("jakarta.persistence.Entity")
            .orShould().beAnnotatedWith("jakarta.persistence.Table")
            .orShould().beAnnotatedWith("jakarta.persistence.MappedSuperclass");

    @ArchTest
    static final ArchRule auth_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("br.com.moreiracruz.erp.modules.auth..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "br.com.moreiracruz.erp.modules.product..",
                "br.com.moreiracruz.erp.modules.inventory..",
                "br.com.moreiracruz.erp.modules.sales..",
                "br.com.moreiracruz.erp.modules.customer..",
                "br.com.moreiracruz.erp.modules.finance..",
                "br.com.moreiracruz.erp.modules.pricing.."
            );

    @ArchTest
    static final ArchRule product_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("br.com.moreiracruz.erp.modules.product..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "br.com.moreiracruz.erp.modules.auth..",
                "br.com.moreiracruz.erp.modules.inventory..",
                "br.com.moreiracruz.erp.modules.sales..",
                "br.com.moreiracruz.erp.modules.customer..",
                "br.com.moreiracruz.erp.modules.finance..",
                "br.com.moreiracruz.erp.modules.pricing.."
            );

    @ArchTest
    static final ArchRule inventory_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("br.com.moreiracruz.erp.modules.inventory..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "br.com.moreiracruz.erp.modules.auth..",
                "br.com.moreiracruz.erp.modules.product..",
                "br.com.moreiracruz.erp.modules.sales..",
                "br.com.moreiracruz.erp.modules.customer..",
                "br.com.moreiracruz.erp.modules.finance..",
                "br.com.moreiracruz.erp.modules.pricing.."
            );

    @ArchTest
    static final ArchRule sales_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("br.com.moreiracruz.erp.modules.sales..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "br.com.moreiracruz.erp.modules.auth..",
                "br.com.moreiracruz.erp.modules.product..",
                "br.com.moreiracruz.erp.modules.customer..",
                "br.com.moreiracruz.erp.modules.finance..",
                "br.com.moreiracruz.erp.modules.pricing.."
            );

    @ArchTest
    static final ArchRule customer_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("br.com.moreiracruz.erp.modules.customer..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "br.com.moreiracruz.erp.modules.auth..",
                "br.com.moreiracruz.erp.modules.product..",
                "br.com.moreiracruz.erp.modules.inventory..",
                "br.com.moreiracruz.erp.modules.sales..",
                "br.com.moreiracruz.erp.modules.finance..",
                "br.com.moreiracruz.erp.modules.pricing.."
            );

    @ArchTest
    static final ArchRule finance_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("br.com.moreiracruz.erp.modules.finance..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "br.com.moreiracruz.erp.modules.auth..",
                "br.com.moreiracruz.erp.modules.product..",
                "br.com.moreiracruz.erp.modules.inventory..",
                "br.com.moreiracruz.erp.modules.sales..",
                "br.com.moreiracruz.erp.modules.customer..",
                "br.com.moreiracruz.erp.modules.pricing.."
            );

    @ArchTest
    static final ArchRule pricing_should_not_depend_on_other_modules =
        noClasses().that().resideInAPackage("br.com.moreiracruz.erp.modules.pricing..")
            .should().dependOnClassesThat()
            .resideInAnyPackage(
                "br.com.moreiracruz.erp.modules.auth..",
                "br.com.moreiracruz.erp.modules.product..",
                "br.com.moreiracruz.erp.modules.inventory..",
                "br.com.moreiracruz.erp.modules.sales..",
                "br.com.moreiracruz.erp.modules.customer..",
                "br.com.moreiracruz.erp.modules.finance.."
            );
}
