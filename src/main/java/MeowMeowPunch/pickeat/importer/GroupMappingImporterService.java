package MeowMeowPunch.pickeat.importer;

import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;

import javax.sql.DataSource;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class GroupMappingImporterService {

	private final DataSource dataSource;

	@Value("${welstory.group-mapping.csv-path:/app/data/group_mapping.csv}")
	private String csvPath;

	private static final String[] HEADERS = {"group_id", "group_name"};

	public void importCsv() throws Exception {
		Path path = Path.of(csvPath);
		if (!Files.exists(path)) {
			log.warn("[IMPORTER] 그룹 매핑 CSV 파일을 찾을 수 없습니다: {}. 건너뜁니다.", path.toAbsolutePath());
			return;
		}

		log.info("[IMPORTER] 그룹 매핑 데이터 임포트 시작: {}", path.toAbsolutePath());

		// group_id가 이미 존재하면 group_name과 updated_at만 업데이트하는 Upsert 쿼리
		String sql = """
			INSERT INTO group_mapping (
			    group_id, group_name,
			    created_at, updated_at
			)
			VALUES (
			    ?, ?,
			    now(), now()
			)
			ON CONFLICT (group_id) DO UPDATE SET
			    group_name = EXCLUDED.group_name,
			    updated_at = now()
			""";

		try (Connection conn = dataSource.getConnection()) {
			conn.setAutoCommit(false);

			try (PreparedStatement ps = conn.prepareStatement(sql);
				 Reader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8);
				 CSVParser parser = CSVFormat.DEFAULT.builder()
					 .setHeader(HEADERS)
					 .setSkipHeaderRecord(true)
					 .setIgnoreHeaderCase(false)
					 .setTrim(true)
					 .build()
					 .parse(reader)) {

				for (CSVRecord record : parser) {
					String groupId = record.get("group_id");
					String groupName = record.get("group_name");

					ps.setString(1, groupId);
					ps.setString(2, groupName);

					ps.addBatch();
				}

				ps.executeBatch();
				conn.commit();
				log.info("[IMPORTER] 그룹 매핑 데이터 임포트 완료");
			} catch (Exception e) {
				conn.rollback();
				log.error("[IMPORTER] 그룹 매핑 데이터 임포트 실패 (롤백됨)", e);
				throw e;
			}
		}
	}
}
