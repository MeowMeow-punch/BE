package MeowMeowPunch.pickeat.welstory.entity;

import MeowMeowPunch.pickeat.global.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// Welstory 그룹명 ↔ 식당 ID 매핑 엔티티
@Getter
@Entity
@Table(name = "group_mapping", uniqueConstraints = {
	@UniqueConstraint(columnNames = {"group_id"}),
	@UniqueConstraint(columnNames = {"group_name"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupMapping extends BaseEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "group_id", nullable = false, length = 32)
	private String groupId;

	@Column(name = "group_name", nullable = false, length = 255)
	private String groupName;
}
