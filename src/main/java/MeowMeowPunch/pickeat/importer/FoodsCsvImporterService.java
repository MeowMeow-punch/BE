package MeowMeowPunch.pickeat.importer;

import java.io.Reader;
import java.math.BigDecimal;
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
public class FoodsCsvImporterService {

	private final DataSource dataSource;

	@Value("${foods.csv-path:/app/data/foods.csv}")
	private String csvPath;

	private static final String[] HEADERS = {
		"food_code", "name", "category",
		"base_amount", "base_unit",
		"kcal", "carbs", "protein", "fat", "sugar",
		"dietary_fiber", "vit_a", "vit_c", "vit_d",
		"calcium", "iron", "sodium", "image"
	};

	public void importCsv() throws Exception {
		Path path = Path.of(csvPath);
		log.info("[IMPORTER] Start import from {}", path.toAbsolutePath());

		String sql = """
			INSERT INTO foods (
			    food_code, name, category,
			    base_amount, base_unit,
			    kcal, carbs, protein, fat, sugar,
			    dietary_fiber, vit_a, vit_c, vit_d,
			    calcium, iron, sodium, thumbnail_url,
			    created_at, updated_at
			)
			VALUES (
			    ?, ?, ?,
			    ?, ?,
			    ?, ?, ?, ?, ?,
			    ?, ?, ?, ?,
			    ?, ?, ?, ?,
			    now(), now()
			)
			ON CONFLICT (food_code) DO UPDATE SET
			    name          = EXCLUDED.name,
			    category      = EXCLUDED.category,
			    base_amount   = EXCLUDED.base_amount,
			    base_unit     = EXCLUDED.base_unit,
			    kcal          = EXCLUDED.kcal,
			    carbs         = EXCLUDED.carbs,
			    protein       = EXCLUDED.protein,
			    fat           = EXCLUDED.fat,
			    sugar         = EXCLUDED.sugar,
			    dietary_fiber = EXCLUDED.dietary_fiber,
			    vit_a         = EXCLUDED.vit_a,
			    vit_c         = EXCLUDED.vit_c,
			    vit_d         = EXCLUDED.vit_d,
			    calcium       = EXCLUDED.calcium,
			    iron          = EXCLUDED.iron,
			    sodium        = EXCLUDED.sodium,
			    thumbnail_url = EXCLUDED.thumbnail_url,
			    updated_at    = now()
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
					String foodCode = record.get("food_code");

					ps.setString(1, foodCode);
					ps.setString(2, record.get("name"));
					ps.setString(3, record.get("category"));

					ps.setInt(4, toInt(record.get("base_amount"), "base_amount", foodCode));
					ps.setString(5, record.get("base_unit"));

					ps.setBigDecimal(6, toDecimal(record.get("kcal"), "kcal", foodCode));
					ps.setBigDecimal(7, toDecimal(record.get("carbs"), "carbs", foodCode));
					ps.setBigDecimal(8, toDecimal(record.get("protein"), "protein", foodCode));
					ps.setBigDecimal(9, toDecimal(record.get("fat"), "fat", foodCode));
					ps.setBigDecimal(10, toDecimal(record.get("sugar"), "sugar", foodCode));

					ps.setBigDecimal(11, toDecimal(record.get("dietary_fiber"), "dietary_fiber", foodCode));
					ps.setBigDecimal(12, toDecimal(record.get("vit_a"), "vit_a", foodCode));
					ps.setBigDecimal(13, toDecimal(record.get("vit_c"), "vit_c", foodCode));
					ps.setBigDecimal(14, toDecimal(record.get("vit_d"), "vit_d", foodCode));
					ps.setBigDecimal(15, toDecimal(record.get("calcium"), "calcium", foodCode));
					ps.setBigDecimal(16, toDecimal(record.get("iron"), "iron", foodCode));
					ps.setBigDecimal(17, toDecimal(record.get("sodium"), "sodium", foodCode));

					// CSV 컬럼명은 image지만 DB 컬럼은 thumbnail_url
					ps.setString(18, record.get("image"));

					ps.addBatch();
				}

				ps.executeBatch();
				conn.commit();
				log.info("[IMPORTER] Import finished");
			} catch (Exception e) {
				conn.rollback();
				log.error("[IMPORTER] Import failed, rolled back", e);
				throw e;
			}
		}
	}

	private static BigDecimal toDecimal(String raw, String columnName, String foodCode) {
		String trimmed = raw == null ? "" : raw.trim();
		if (trimmed.isEmpty()) {
			log.warn("[IMPORTER] 빈 {} 값 감지. food_code={} → 0으로 대체", columnName, foodCode);
			return BigDecimal.ZERO;
		}
		try {
			return new BigDecimal(trimmed);
		} catch (NumberFormatException e) {
			log.warn("[IMPORTER] 잘못된 {} 값 감지. food_code={}, raw='{}' → 0으로 대체",
				columnName, foodCode, trimmed);
			return BigDecimal.ZERO;
		}
	}

	private int toInt(String value, String columnName, String foodCode) {
		if (value == null || value.isBlank()) {
			log.warn("[IMPORTER] 빈 {} 값 감지. food_code={} → 0으로 대체", columnName, foodCode);
			return 0;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			log.warn("[IMPORTER] 잘못된 {} 값 감지. food_code={}, raw='{}' → 0으로 대체",
				columnName, foodCode, value);
			return 0;
		}
	}
}
