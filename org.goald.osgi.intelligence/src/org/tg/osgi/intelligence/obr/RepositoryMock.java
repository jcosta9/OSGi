package org.tg.osgi.intelligence.obr;

import goald.model.util.BundleBuilder;
import goald.planning.DameRespository;
import goald.repository.IRepository;
import goald.repository.RepositoryBuilder;

public class RepositoryMock {
	

		public static DameRespository regRepo() {

			IRepository _repo = RepositoryBuilder.create()
					.add(
							BundleBuilder.create()
							.identification("ProvideHealthSupport.def")
							.defines("ProvideHealthSupport")
							.build())
						.add(
							BundleBuilder.create()
							.identification("ProvideHealthSupport.impl")
							.provides("ProvideHealthSupport")
							.dependsOn("ProvideSelfDiagnosedEmergenciesSupport")
							.dependsOn("ProvideAutomatedLifeSupport")
							.build())
						/* 2nd level definitions */
						.add(
							BundleBuilder.create()
							.identification("ProvideSelfDiagnosedEmergenciesSupport-definition")
							.defines("ProvideSelfDiagnosedEmergenciesSupport")
							.build())
						.add(
							BundleBuilder.create()
							.identification("ProvideAutomatedLifeSupport-definition")
							.defines("ProvideAutomatedLifeSupport")
							.build())
						/* Provide Automated Life Support plans*/
						.add(
							BundleBuilder.create()
							.identification("ProvideAutomatedLifeSupport-impl")
							.provides("ProvideAutomatedLifeSupport")
							.dependsOn("MonitorPatient")
							.dependsOn("EnactTreatment")
							.build())
						.add(
							BundleBuilder.create()
							.identification("MonitorPatient-definition")
							.defines("MonitorPatient")
							.build())
						.add(
							BundleBuilder.create()
							.identification("EnactTreatment-definition")
							.defines("EnactTreatment")
							.build())
						/* Monitor Patient */
						.add(
							BundleBuilder.create()
							.identification("MonitorPatient-impl")
							.provides("MonitorPatient")
							.dependsOn("GetVitalParams")
							.dependsOn("AnalyzeData")
							.build())
						.add(
							BundleBuilder.create()
							.identification("GetVitalParams-definition")
							.defines("GetVitalParams")
							.build())
						.add(
							BundleBuilder.create()
							.identification("GetVitalParams-impl")
							.provides("GetVitalParams")
							.build())
						.add(
							BundleBuilder.create()
							.identification("AnalyzeData-definition")
							.defines("AnalyzeData")
							.build())
						/*
						.add(
							BundleBuilder.create()
							.identification("LocalAnalysis-impl")
							.provides("AnalyzeData")
							.build())
							*/
						.add(
							BundleBuilder.create()
							.identification("RemoteAnalysis-impl")
							.provides("AnalyzeData")
							.requires("internet-connection")
							.build())
						/* Enact Treatment */
						.add(
							BundleBuilder.create()
							.identification("EnactTreatment-impl")
							.provides("EnactTreatment")
							.dependsOn("AdministerMedicine")
							.dependsOn("NotifyEmergencyMedicalServices")
							.build())
						.add(
							BundleBuilder.create()
							.identification("AdministerMedicine-definition")
							.defines("AdministerMedicine")
							.build())
						.add(
							BundleBuilder.create()
							.identification("AdministerMedicine-impl")
							.provides("AdministerMedicine")
							.dependsOn("ChangeDrug")
							.dependsOn("ChangeDose")
							.build())
						.add(
							BundleBuilder.create()
							.identification("ChangeDrug-definition")
							.defines("ChangeDrug")
							.build())
						.add(
							BundleBuilder.create()
							.identification("ChangeDose-definition")
							.defines("ChangeDose")
							.build())
						.add(
							BundleBuilder.create()
							.identification("ChangeDrug-impl")
							.provides("ChangeDrug")
							.dependsOn("NonInvasive")
							.dependsOn("Invasive")
							.build())
						.add(
							BundleBuilder.create()
							.identification("NonInvasive-definition")
							.defines("NonInvasive")
							.build())
						.add(
							BundleBuilder.create()
							.identification("Invasive-definition")
							.defines("Invasive")
							.build())
						.add(
							BundleBuilder.create()
							.identification("NonInvasive-impl")
							.provides("NonInvasive")
							.build())
						.add(
							BundleBuilder.create()
							.identification("Invasive-impl")
							.provides("Invasive")
							.build())
						.add(
							BundleBuilder.create()
							.identification("ChangeDose-impl")
							.provides("ChangeDose")
							.requires("drug-being-administered")
							.build())
						.add(
							BundleBuilder.create()
							.identification("SendAlarm-impl")
							.provides("SendAlarm")
							.requires("internet-connection")
							.build())
						/* Panic Button Impl */
						.add(
							BundleBuilder.create()
							.identification("ProvideSelfDiagnosedEmergenciesSupport-impl")
							.provides("ProvideSelfDiagnosedEmergenciesSupport")
							.dependsOn("PushButton")
							.dependsOn("NotifyEmergencyMedicalServices")
							.build())
						.add(
							BundleBuilder.create()
							.identification("PushButton-definition")
							.defines("PushButton")
							.build())
						.add(
							BundleBuilder.create()
							.identification("PushButton-impl")
							.provides("PushButton")
							.build())
						.add(
							BundleBuilder.create()
							.identification("NotifyEmergencyMedicalServices-definition")
							.defines("NotifyEmergencyMedicalServices")
							.build())
						/*
						.add(
							BundleBuilder.create()
							.identification("SendSMS-impl")
							.provides("NotifyEmergencyMedicalServices")
							.build())
							*/
						.add(
							BundleBuilder.create()
							.identification("AlarmService-impl")
							.provides("NotifyEmergencyMedicalServices")
							.build())
						.build();
			return new DameRespository(_repo);
		}
}
