package com.smartRahi.SmartRahi;

import com.smartRahi.SmartRahi.Repository.CityRepository;
import com.smartRahi.SmartRahi.Services.GtfsImportService;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

import org.slf4j.Logger;
import org.springframework.scheduling.annotation.EnableScheduling;


@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
@EnableAsync
public class SmartRahiApplication {

	private static final Logger log = LoggerFactory.getLogger(SmartRahiApplication.class);

	public static void main(String[] args) {
		SpringApplication.run(SmartRahiApplication.class, args);
	}

	/**
	 * This @Bean creates a CommandLineRunner.
	 * Spring Boot will run this method automatically *once* on startup.
	 * This is where we trigger the GTFS import.
	 */
	@Bean
	public CommandLineRunner importGtfsData(
			GtfsImportService gtfsImportService,
			CityRepository cityRepository // <-- Inject CityRepository for the check
	) {
		return args -> {
			log.info("Application started... preparing for GTFS import.");

			// --- CONFIGURE YOUR IMPORT HERE ---

			// 1. Set the path to your UNZIPPED GTFS folder (or the .zip file)
			//    (Remember to use double backslashes \\ on Windows)
			String gtfsPath = "C:\\Users\\anmol\\Downloads\\DATASET";

			// 2. Do you want to use Nominatim to find the city name?
			boolean useGeocoding = true;

			// 3. If not, what is the default name?
			String defaultCity = "Default City";

			// --- END CONFIGURATION ---

			try {
				// This check prevents the importer from running every single time
				// you restart the app.
				if (cityRepository.count() > 0) {
					log.warn("=======================================================");
					log.warn("Database is not empty. Skipping GTFS import.");
					log.warn("To re-import, TRUNCATE all GTFS tables in your database.");
					log.warn("=======================================================");
					return; // Stops the import
				}

				// If database is empty, run the full import
				log.info("Database is empty. Starting full GTFS ingestion...");
				gtfsImportService.ingestGtfs(gtfsPath, useGeocoding, defaultCity);

			} catch (Exception e) {
				log.error("--- FATAL INGESTION ERROR ---");
				log.error("The transaction has been rolled back.");
				log.error("Error: ", e); // Full stack trace for debugging
			}
		};
	}
}


