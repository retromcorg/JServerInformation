package com.johnymuffin.serverinformation.beta;

import com.johnymuffin.beta.fundamentals.player.FundamentalsPlayer;

public class Utility {

    public static final String HIDE_COORDS_KEY = "hide_coords";

    public static boolean isHidingCoordinates(FundamentalsPlayer fundamentalsPlayer) {
        Object value = fundamentalsPlayer.getInformation(HIDE_COORDS_KEY);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return value != null && Boolean.parseBoolean(String.valueOf(value));
    }

}
