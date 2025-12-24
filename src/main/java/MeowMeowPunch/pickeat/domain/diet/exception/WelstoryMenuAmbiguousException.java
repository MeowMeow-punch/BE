package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

public class WelstoryMenuAmbiguousException extends InvalidGroupException {
    public WelstoryMenuAmbiguousException(String restaurantName, String menuName) {
        super("Welstory menu ambiguous: restaurant=" + restaurantName + ", menu=" + menuName);
    }
}

