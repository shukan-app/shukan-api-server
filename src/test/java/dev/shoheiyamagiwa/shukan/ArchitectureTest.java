package dev.shoheiyamagiwa.shukan;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

public final class ArchitectureTest {
	private static JavaClasses importedClasses;
	
	@BeforeAll
	public static void setUp() {
		importedClasses = new ClassFileImporter()
				.withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
				.importPackages("dev.shoheiyamagiwa.shukan");
	}
	
	@Test
	public void testLayeredArchitecture() {
		Architectures.layeredArchitecture()
				.consideringAllDependencies()
				.withOptionalLayers(true)
				.layer("Controller").definedBy("dev.shoheiyamagiwa.shukan.controller..",
						"dev.shoheiyamagiwa.shukan.middleware..", "dev.shoheiyamagiwa.shukan")
				.layer("Service").definedBy("dev.shoheiyamagiwa.shukan.service..")
				.layer("Repository").definedBy("dev.shoheiyamagiwa.shukan.repository..")
				.layer("Entity").definedBy("dev.shoheiyamagiwa.shukan.entity..")
				.whereLayer("Controller").mayNotBeAccessedByAnyLayer()
				.whereLayer("Service").mayOnlyBeAccessedByLayers("Controller", "Service")
				.whereLayer("Repository").mayOnlyBeAccessedByLayers("Service", "Repository")
				.check(importedClasses);
	}
	
	@Test
	public void testFinalAndNoInheritance() {
		classes().that().areNotInterfaces()
				.and().areNotEnums()
				.and().areNotRecords()
				.should().haveModifier(JavaModifier.FINAL)
				.andShould(new ArchCondition<>("not extend any class other than Object, Record, Enum, or Exception") {
					@Override
					public void check(JavaClass item, ConditionEvents events) {
						if (item.getSuperclass().isPresent()) {
							String superclassName = item.getSuperclass().get().getName();
							boolean isAllowed = superclassName.equals("java.lang.Object") ||
									superclassName.equals("java.lang.Record") ||
									superclassName.equals("java.lang.Enum") ||
									item.isAssignableTo(Exception.class);
							if (!isAllowed) {
								events.add(SimpleConditionEvent.violated(item, item.getName() + " extends " + superclassName));
							}
						}
					}
				})
				.allowEmptyShould(true)
				.check(importedClasses);
	}
	
	@Test
	public void testServiceRules() {
		classes().that().resideInAPackage("dev.shoheiyamagiwa.shukan.service..")
				.and().areNotInterfaces()
				.should().haveSimpleNameEndingWith("Service")
				.allowEmptyShould(true)
				.check(importedClasses);
		
		classes().that().resideInAPackage("dev.shoheiyamagiwa.shukan.service..")
				.and().areInterfaces()
				.should().haveSimpleNameEndingWith("Repository")
				.orShould().haveSimpleNameEndingWith("RepositoryProvider")
				.orShould().haveSimpleNameEndingWith("TransactionManager")
				.allowEmptyShould(true)
				.check(importedClasses);
	}
	
	@Test
	public void testRepositoryRules() {
		classes().that().resideInAPackage("dev.shoheiyamagiwa.shukan.repository..")
				.and().areNotInterfaces()
				.should().haveSimpleNameEndingWith("Impl")
				.orShould().haveSimpleNameEndingWith("Dao")
				.orShould().haveSimpleNameEndingWith("Dto")
				.allowEmptyShould(true)
				.check(importedClasses);
	}
	
	@Test
	public void testControllerRules() {
		classes().that().resideInAPackage("dev.shoheiyamagiwa.shukan.controller..")
				.should().haveSimpleNameEndingWith("Controller")
				.orShould().haveSimpleNameEndingWith("RequestDto")
				.orShould().haveSimpleNameEndingWith("ResponseDto")
				.allowEmptyShould(true)
				.check(importedClasses);
	}
	
	@Test
	public void testEntityRules() {
		noClasses().that().resideInAPackage("dev.shoheiyamagiwa.shukan.entity..")
				.should().dependOnClassesThat().resideInAnyPackage(
						"dev.shoheiyamagiwa.shukan.service..",
						"dev.shoheiyamagiwa.shukan.repository..",
						"dev.shoheiyamagiwa.shukan.controller..",
						"dev.shoheiyamagiwa.shukan.middleware.."
				)
				.allowEmptyShould(true)
				.check(importedClasses);
	}
}
