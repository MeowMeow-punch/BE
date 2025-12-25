package MeowMeowPunch.pickeat.domain.diet.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import MeowMeowPunch.pickeat.global.common.enums.FeedBackType;
import MeowMeowPunch.pickeat.global.common.enums.DietType;
import MeowMeowPunch.pickeat.global.common.entity.BaseEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "ai_feedback")
public class AiFeedBack extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "ai_feedback_id")
	private Long id;

	@Column(nullable = false)
	private String userId;

	@Column(nullable = false)
	private LocalDate date;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false)
	private FeedBackType type;

	@Enumerated(EnumType.STRING)
	@Column(name = "diet_type")
	private DietType mealType;

	@Column(nullable = false, length = 1000)
	private String content;

	@Builder
	public AiFeedBack(String userId, LocalDate date, FeedBackType type, DietType mealType, String content) {
		this.userId = userId;
		this.date = date;
		this.type = type;
		this.mealType = mealType;
		this.content = content;
	}

	public void updateContent(String content) {
		this.content = content;
	}

	public void update(String content, DietType mealType) {
		this.content = content;
		this.mealType = mealType;
	}
}
