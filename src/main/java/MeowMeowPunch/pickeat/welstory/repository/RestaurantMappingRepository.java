package MeowMeowPunch.pickeat.welstory.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import MeowMeowPunch.pickeat.welstory.entity.RestaurantMapping;

// 레스토랑 이름/ID 매핑 조회용 JPA 리포지토리
public interface RestaurantMappingRepository extends JpaRepository<RestaurantMapping, Long> {
	Optional<RestaurantMapping> findByRestaurantName(String restaurantName);
}
