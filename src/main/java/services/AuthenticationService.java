package services;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AuthenticationService {
    private static final String AUTHENTICATION_KEY = "flight_reservation_auth";
    private static final Set<String> AUTHENTICATED_VALUES = Set.of("abcd", "aabbccc");

    /**
     * Returns true if one of the cookies contains a valid authentication token
     *
     * @param cookies - a list of cookie headers in the format of "key=value" pair
     */
    public boolean check(List<String> cookies) {
        if (cookies == null) {
            return false;
        }
        Map<String, String> keyValuePairs = getCookiesKeyValuePairs(cookies);

        return keyValuePairs.entrySet()
                .stream()
                .filter(e -> e.getKey().equals(AUTHENTICATION_KEY))
                .anyMatch(e -> AUTHENTICATED_VALUES.contains(e.getValue()));
    }

    private Map<String, String> getCookiesKeyValuePairs(List<String> cookies) {
        return cookies.stream()
                .filter(pair -> pair.contains("=") && pair.length() > 2)
                .collect(Collectors.toMap(pair -> pair.substring(0, pair.indexOf("="))
                        , pair -> pair.substring(pair.indexOf("=") + 1)));
    }
}
