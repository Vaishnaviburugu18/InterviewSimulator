package model;

public class User {
    private final int id;
    private final String username;
    private final String email;
    private int xp;
    private int level;
    private int streak;

    public User(int id, String username, String email) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.xp = 0;
        this.level = 1;
        this.streak = 0;
    }

    public User(int id, String username, String email, int xp, int level, int streak) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.xp = xp;
        this.level = level;
        this.streak = streak;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getEmail() { return email; }
    public int getXp() { return xp; }
    public int getLevel() { return level; }
    public int getStreak() { return streak; }

    public void setXp(int xp) { this.xp = xp; }
    public void setLevel(int level) { this.level = level; }
    public void setStreak(int streak) { this.streak = streak; }

    /** XP needed to reach the next level (100 * current level). */
    public int xpForNextLevel() { return level * 100; }

    /** XP progress within the current level. */
    public int xpInCurrentLevel() { return xp % 100; }
}
