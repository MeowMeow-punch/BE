package MeowMeowPunch.pickeat.importer;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@Profile("importer")
@RequiredArgsConstructor
public class GroupMappingImporterRunner implements CommandLineRunner {

	private final GroupMappingImporterService importerService;

	@Override
	public void run(String... args) throws Exception {
		log.info("[IMPORTER] GroupMapping 러너 시작");
		importerService.importCsv();
		log.info("[IMPORTER] GroupMapping 러너 종료");
	}
}
