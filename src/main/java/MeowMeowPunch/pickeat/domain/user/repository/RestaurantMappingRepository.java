package MeowMeowPunch.pickeat.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import MeowMeowPunch.pickeat.domain.user.entity.RestaurantMapping;

public interface RestaurantMappingRepository extends JpaRepository<RestaurantMapping, Long> {

    java.util.Optional<RestaurantMapping> findByRestaurantName(String restaurantName);
}
