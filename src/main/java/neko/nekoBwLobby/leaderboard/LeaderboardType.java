package neko.nekoBwLobby.leaderboard;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum LeaderboardType {
    SCORE("score", "分数"),
    KILLS("kills", "击杀数"),
    DESTROYED_BEDS("destroyed_beds", "挖床数"),
    DEATHS("deaths", "死亡数");

    private final String configKey;
    private final String displayName;

    LeaderboardType(String configKey, String displayName) {
        this.configKey = configKey;
        this.displayName = displayName;
    }

    public String getConfigKey() {
        return configKey;
    }

    public String getDisplayName() {
        return displayName;
    }

    public LeaderboardType getNextType() {
        LeaderboardType[] values = LeaderboardType.values();
        int currentIndex = this.ordinal();
        int nextIndex = (currentIndex + 1) % values.length;
        return values[nextIndex];
    }

    private static final Map<String, LeaderboardType> BY_CONFIG_KEY = Arrays.stream(values())
            .collect(Collectors.toMap(LeaderboardType::getConfigKey, e -> e));

    public static LeaderboardType getByConfigKey(String configKey) {
        return BY_CONFIG_KEY.get(configKey.toLowerCase());
    }
}