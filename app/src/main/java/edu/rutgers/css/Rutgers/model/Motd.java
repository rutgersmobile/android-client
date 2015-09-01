package edu.rutgers.css.Rutgers.model;

/**
 * POJO for holding Motd info from the API
 */
public final class Motd {
    private final String motd;
    private final String title;
    private final String data;
    private final boolean isWindow;
    private final boolean hasCloseButton;
    private final String betaCheck;

    public Motd(String motd, String title, String data, boolean isWindow, boolean hasCloseButton, String betaCheck) {
        this.motd = motd;
        this.title = title;
        this.data = data;
        this.isWindow = isWindow;
        this.hasCloseButton = hasCloseButton;
        this.betaCheck = betaCheck;
    }

    public String getMotd() {
        return motd;
    }

    public String getTitle() {
        return title;
    }

    public String getData() {
        return data;
    }

    public boolean isWindow() {
        return isWindow;
    }

    public boolean hasCloseButton() {
        return hasCloseButton;
    }

    public String getBetaCheck() {
        return betaCheck;
    }
}
