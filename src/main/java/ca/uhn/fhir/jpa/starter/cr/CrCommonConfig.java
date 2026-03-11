package ca.uhn.fhir.jpa.starter.cr;

import ca.uhn.fhir.jpa.api.dao.DaoRegistry;
import ca.uhn.fhir.jpa.cache.IResourceChangeListenerRegistry;
import ca.uhn.fhir.jpa.cache.ResourceChangeListenerRegistryInterceptor;
import ca.uhn.fhir.jpa.searchparam.SearchParameterMap;
import ca.uhn.fhir.rest.server.RestfulServer;
import ca.uhn.fhir.rest.server.provider.ResourceProviderFactory;
import org.cqframework.cql.cql2elm.CqlCompilerOptions;
import org.cqframework.cql.cql2elm.model.CompiledLibrary;
import org.cqframework.cql.cql2elm.model.Model;
import org.hl7.cql.model.ModelIdentifier;
import org.hl7.elm.r1.VersionedIdentifier;
import org.opencds.cqf.cql.engine.execution.CqlEngine;
import org.opencds.cqf.cql.engine.runtime.Code;
import org.opencds.cqf.fhir.cql.EvaluationSettings;
import org.opencds.cqf.fhir.cql.engine.retrieve.RetrieveSettings;
import org.opencds.cqf.fhir.cql.engine.terminology.TerminologySettings;
import org.opencds.cqf.fhir.cr.hapi.common.CodeCacheResourceChangeListener;
import org.opencds.cqf.fhir.cr.hapi.common.CqlThreadFactory;
import org.opencds.cqf.fhir.cr.hapi.common.ElmCacheResourceChangeListener;
import org.opencds.cqf.fhir.cr.measure.CareGapsProperties;
import org.opencds.cqf.fhir.cr.measure.MeasureEvaluationOptions;
import org.opencds.cqf.fhir.utility.ValidationProfile;
import org.opencds.cqf.fhir.utility.client.TerminologyServerClientSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.concurrent.DelegatingSecurityContextExecutorService;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Configuration
@Conditional({CrConfigCondition.class})
public class CrCommonConfig {

	@Bean
	RetrieveSettings retrieveSettings(final CqlData cqlData) {
		return cqlData.getRetrieveSettings();
	}

	@Bean
	TerminologySettings terminologySettings(final CqlTerminologyProperties theCqlTerminologyProperties) {
		return theCqlTerminologyProperties.getTerminologySettings();
	}

	@Bean
	TerminologyServerClientSettings terminologyServerClientSettings(final CrProperties theCrProperties) {
		return theCrProperties.getTerminologyServerClientSettings();
	}

	@Bean
	public EvaluationSettings evaluationSettings(
		final CqlRuntimeProperties cqlRuntimeProperties,
		final CqlCompilerProperties cqlCompilerProperties,
		final RetrieveSettings theRetrieveSettings,
		final TerminologySettings theTerminologySettings,
		final Map<VersionedIdentifier, CompiledLibrary> theGlobalLibraryCache,
		final Map<ModelIdentifier, Model> theGlobalModelCache,
		final Map<String, List<Code>> theGlobalValueSetCache) {
		final var evaluationSettings = EvaluationSettings.getDefault();
		final var cqlOptions = evaluationSettings.getCqlOptions();

		final var cqlEngineOptions = cqlOptions.getCqlEngineOptions();
		final Set<CqlEngine.Options> options = EnumSet.noneOf(CqlEngine.Options.class);

		if (cqlRuntimeProperties.isEnableExpressionCaching()) {
			options.add(CqlEngine.Options.EnableExpressionCaching);
		}
		if (cqlRuntimeProperties.isEnableValidation()) {
			options.add(CqlEngine.Options.EnableValidation);
		}
		cqlEngineOptions.setOptions(options);
		if (cqlRuntimeProperties.isDebugLoggingEnabled()) {
			cqlEngineOptions.setDebugLoggingEnabled(true);
		}
		cqlOptions.setCqlEngineOptions(cqlEngineOptions);

		final var cqlCompilerOptions = new CqlCompilerOptions();

		if (cqlCompilerProperties.isEnableDateRangeOptimization()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableDateRangeOptimization);
		}
		if (cqlCompilerProperties.isEnableAnnotations()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableAnnotations);
		}
		if (cqlCompilerProperties.isEnableLocators()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableLocators);
		}
		if (cqlCompilerProperties.isEnableResultsType()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableResultTypes);
		}
		cqlCompilerOptions.setVerifyOnly(cqlCompilerProperties.isVerifyOnly());
		if (cqlCompilerProperties.isEnableDetailedErrors()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableDetailedErrors);
		}
		cqlCompilerOptions.setErrorLevel(cqlCompilerProperties.getErrorSeverityLevel());
		if (cqlCompilerProperties.isDisableListTraversal()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableListTraversal);
		}
		if (cqlCompilerProperties.isDisableListDemotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableListDemotion);
		}
		if (cqlCompilerProperties.isDisableListPromotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableListPromotion);
		}
		if (cqlCompilerProperties.isEnableIntervalDemotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableIntervalDemotion);
		}
		if (cqlCompilerProperties.isEnableIntervalPromotion()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.EnableIntervalPromotion);
		}
		if (cqlCompilerProperties.isDisableMethodInvocation()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableMethodInvocation);
		}
		if (cqlCompilerProperties.isRequireFromKeyword()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.RequireFromKeyword);
		}
		cqlCompilerOptions.setValidateUnits(cqlCompilerProperties.isValidateUnits());
		if (cqlCompilerProperties.isDisableDefaultModelInfoLoad()) {
			cqlCompilerOptions.setOptions(CqlCompilerOptions.Options.DisableDefaultModelInfoLoad);
		}
		cqlCompilerOptions.setSignatureLevel(cqlCompilerProperties.getSignatureLevel());
		cqlCompilerOptions.setCompatibilityLevel(cqlCompilerProperties.getCompatibilityLevel());
		cqlCompilerOptions.setAnalyzeDataRequirements(cqlCompilerProperties.isAnalyzeDataRequirements());
		cqlCompilerOptions.setCollapseDataRequirements(cqlCompilerProperties.isCollapseDataRequirements());

		cqlOptions.setCqlCompilerOptions(cqlCompilerOptions);
		evaluationSettings.setLibraryCache(theGlobalLibraryCache);
		evaluationSettings.setModelCache(theGlobalModelCache);
		evaluationSettings.setValueSetCache(theGlobalValueSetCache);
		evaluationSettings.setRetrieveSettings(theRetrieveSettings);
		evaluationSettings.setTerminologySettings(theTerminologySettings);
		return evaluationSettings;
	}

	@Primary
	@Bean
	public ExecutorService cqlExecutor() {
		final CqlThreadFactory factory = new CqlThreadFactory();
		ExecutorService executor = Executors.newFixedThreadPool(2, factory);
		executor = new DelegatingSecurityContextExecutorService(executor);

		return executor;
	}

	@Bean(name = "measure.CareGapsProperties")
	org.opencds.cqf.fhir.cr.measure.CareGapsProperties careGapsProperties(final CrProperties theCrProperties) {
		final var careGapsProperties = new CareGapsProperties();
		// This check for the resource type really should be happening down in CR where the setting is actually used but
		// that will have to wait for a future CR release
		careGapsProperties.setCareGapsReporter(
			theCrProperties.getCareGaps().getReporter().replace("Organization/", ""));
		careGapsProperties.setCareGapsCompositionSectionAuthor(
			theCrProperties.getCareGaps().getSection_author().replace("Organization/", ""));
		return careGapsProperties;
	}

	@Bean
	MeasureEvaluationOptions measureEvaluationOptions(
		final EvaluationSettings theEvaluationSettings, final Map<String, ValidationProfile> theValidationProfiles) {
		final MeasureEvaluationOptions measureEvalOptions = new MeasureEvaluationOptions();
		measureEvalOptions.setEvaluationSettings(theEvaluationSettings);
		if (measureEvalOptions.isValidationEnabled()) {
			measureEvalOptions.setValidationProfiles(theValidationProfiles);
		}
		return measureEvalOptions;
	}

	@Bean
	public PostInitProviderRegisterer postInitProviderRegisterer(
		final RestfulServer theRestfulServer, final ResourceProviderFactory theResourceProviderFactory) {
		return new PostInitProviderRegisterer(theRestfulServer, theResourceProviderFactory);
	}

	@Bean
	public Map<VersionedIdentifier, CompiledLibrary> globalLibraryCache() {
		return new ConcurrentHashMap<>();
	}

	@Bean
	public Map<ModelIdentifier, Model> globalModelCache() {
		return new ConcurrentHashMap<>();
	}

	@Bean
	public Map<String, List<Code>> globalValueSetCache() {
		return new ConcurrentHashMap<>();
	}

	@Bean
	public ElmCacheResourceChangeListener elmCacheResourceChangeListener(
		final IResourceChangeListenerRegistry theResourceChangeListenerRegistry,
		final DaoRegistry theDaoRegistry,
		final EvaluationSettings theEvaluationSettings) {
		final ElmCacheResourceChangeListener listener =
			new ElmCacheResourceChangeListener(theDaoRegistry, theEvaluationSettings.getLibraryCache());
		theResourceChangeListenerRegistry.registerResourceResourceChangeListener(
			"Library", SearchParameterMap.newSynchronous(), listener, 1000);
		return listener;
	}

	@Bean
	public CodeCacheResourceChangeListener codeCacheResourceChangeListener(
		final IResourceChangeListenerRegistry theResourceChangeListenerRegistry,
		final EvaluationSettings theEvaluationSettings,
		final DaoRegistry theDaoRegistry) {

		final CodeCacheResourceChangeListener listener =
			new CodeCacheResourceChangeListener(theDaoRegistry, theEvaluationSettings.getValueSetCache());
		// registry
		theResourceChangeListenerRegistry.registerResourceResourceChangeListener(
			"ValueSet", SearchParameterMap.newSynchronous(), listener, 1000);

		return listener;
	}

	@Bean
	public ResourceChangeListenerRegistryInterceptor resourceChangeListenerRegistryInterceptor() {
		return new ResourceChangeListenerRegistryInterceptor();
	}
}
