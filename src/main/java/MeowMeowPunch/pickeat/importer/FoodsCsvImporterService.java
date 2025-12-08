package MeowMeowPunch.pickeat.importer;

import java.io.Reader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.Objects;

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
			    calcium, iron, sodium, image,
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
			    image         = EXCLUDED.image,
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
					ps.setString(1, record.get("food_code"));
					ps.setString(2, record.get("name"));
					ps.setString(3, record.get("category"));

					ps.setInt(4, Integer.parseInt(record.get("base_amount")));
					ps.setString(5, record.get("base_unit"));

					ps.setBigDecimal(6, toDecimal(record.get("kcal")));
					ps.setBigDecimal(7, toDecimal(record.get("carbs")));
					ps.setBigDecimal(8, toDecimal(record.get("protein")));
					ps.setBigDecimal(9, toDecimal(record.get("fat")));
					ps.setBigDecimal(10, toDecimal(record.get("sugar")));

					ps.setBigDecimal(11, toDecimal(record.get("dietary_fiber")));
					ps.setBigDecimal(12, toDecimal(record.get("vit_a")));
					ps.setBigDecimal(13, toDecimal(record.get("vit_c")));
					ps.setBigDecimal(14, toDecimal(record.get("vit_d")));
					ps.setBigDecimal(15, toDecimal(record.get("calcium")));
					ps.setBigDecimal(16, toDecimal(record.get("iron")));
					ps.setBigDecimal(17, toDecimal(record.get("sodium")));

					ps.setString(18, record.get("image"));

					ps.addBatch();
				}

				ps.executeBatch();
			} catch (Exception e) {
				conn.rollback();
				log.error("[IMPORTER] Import failed", e);
				throw e;
			}

			conn.commit();
			log.info("[IMPORTER] Import finished");
		}
	}

	private static BigDecimal toDecimal(String raw) {
		String trimmed = raw == null ? "" : raw.trim();
		if (trimmed.isEmpty()) {
			return BigDecimal.ZERO;
		}
		return new BigDecimal(trimmed);
	}
}
