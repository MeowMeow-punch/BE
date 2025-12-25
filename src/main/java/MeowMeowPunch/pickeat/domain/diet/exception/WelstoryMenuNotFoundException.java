package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

public class WelstoryMenuNotFoundException extends InvalidGroupException {
    public WelstoryMenuNotFoundException(String restaurantName, String menuName) {
        super("Welstory menu not found: restaurant=" + restaurantName + ", menu=" + menuName);
    }
}

