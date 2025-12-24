package MeowMeowPunch.pickeat.domain.diet.exception;

import MeowMeowPunch.pickeat.global.error.exception.InvalidGroupException;

public class WelstoryGroupNotFoundException extends InvalidGroupException {
    public WelstoryGroupNotFoundException(String groupName) {
        super("Welstory group not found: " + groupName);
    }
}

