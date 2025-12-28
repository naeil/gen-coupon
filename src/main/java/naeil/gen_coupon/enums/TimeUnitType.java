package naeil.gen_coupon.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TimeUnitType {

    S("s", 1000L),
    M("m", 60 * 1000L),
    H("h", 60 * 60 * 1000L);

    private final String suffix;
    private final long millis;

    public static long toMillis(String value) {

        if (value == null || value.trim().length() < 2) {
            throw new IllegalArgumentException("Invalid time format : " + value);
        }

        value = value.trim().toLowerCase();

        String num = value.substring(0, value.length() - 1);
        String unit = value.substring(value.length() -1);

        int time;
        try {
            time = Integer.parseInt(num);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid time number : " + value);
        }

        if (time <= 0) {
            throw new IllegalArgumentException("Time must be greater than 0");
        }

        for(TimeUnitType t : values()) {
            if(t.suffix.equals(unit)) {
                long millis = time * t.millis;

                if (time < 60_000) {
                    throw new IllegalArgumentException("Interval is too short");
                }

                if (time > 86_400_000L) {
                    throw new IllegalArgumentException("Interval is too long");
                }
                return millis;
            }
        }

        throw new IllegalArgumentException("Invalid time unit : " + value);
    }

}
