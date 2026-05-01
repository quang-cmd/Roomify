package kqlhotel.gui.utils;

import java.awt.Image;
import java.net.URL;
import javax.swing.ImageIcon;

public final class IconLoader {
    private IconLoader() {}

    public static ImageIcon loadIcon(String filename, int width, int height) {
        if (filename == null) return null;

        try {
            String resourcePath = "/kqlhotel/resources/icons/" + filename;
            URL resource = IconLoader.class.getResource(resourcePath);
            if (resource == null) {
                String srcPath = "src/kqlhotel/resources/icons/" + filename;
                java.io.File file = new java.io.File(srcPath);
                if (file.exists()) {
                    resource = file.toURI().toURL();
                }
            }

            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) {
            // ignore failures and return null
        }

        return null;
    }

    public static ImageIcon loadIconKeepRatio(String filename, int maxSize) {
        if (filename == null) return null;

        try {
            String resourcePath = "/kqlhotel/resources/icons/" + filename;
            URL resource = IconLoader.class.getResource(resourcePath);
            if (resource == null) {
                String srcPath = "src/kqlhotel/resources/icons/" + filename;
                java.io.File file = new java.io.File(srcPath);
                if (file.exists()) {
                    resource = file.toURI().toURL();
                }
            }

            if (resource != null) {
                ImageIcon icon = new ImageIcon(resource);
                int imgWidth = icon.getIconWidth();
                int imgHeight = icon.getIconHeight();

                double scale = Math.min((double) maxSize / imgWidth, (double) maxSize / imgHeight);
                int newWidth = (int) (imgWidth * scale);
                int newHeight = (int) (imgHeight * scale);

                Image scaled = icon.getImage().getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
                return new ImageIcon(scaled);
            }
        } catch (Exception e) {
            // ignore failures and return null
        }

        return null;
    }

    public static ImageIcon getAmenityIcon(String amenity) {
        if (amenity == null) return null;
        switch (amenity) {
            case "Wifi": return loadIcon("wifi.png", 20, 20);
            case "Minibar": return loadIcon("minibar.png", 20, 20);
            case "Ban công": return loadIcon("balcony.png", 20, 20);
            case "Phòng khách": return loadIcon("room.png", 20, 20);
            case "Máy lạnh": return loadIcon("ac.png", 20, 20);
            default: return null;
        }
    }

    public static String getThumbnailFile(String roomType) {
        if (roomType == null) return null;
        String key = roomType.trim().toLowerCase();
        if (key.contains("standard")) return "standard.png";
        if (key.contains("suite")) return "suite.png";
        if (key.contains("vip")) return "vip.png";
        if (key.contains("deluxe")) return "deluxe.png";
        if (key.contains("family")) return "family.png";
        if (key.contains("superior")) return "superior.png";
        return "room.png";
    }

    // public static String getIconFile(String letter) {
    //     if (letter == null) return null;
    //     switch (letter) {
    //         case "D": return "deluxe.png";
    //         case "G1": return "grand-premium-1.png";
    //         case "G2": return "grand-premium-2.png";
    //         case "S": return "suite.png";
    //         default: return null;
    //     }
    // }

    // public static String getRoomInitial(String roomType) {
    //     if (roomType == null) return "?";
    //     if (roomType.startsWith("Grand Premium 1")) return "G1";
    //     if (roomType.startsWith("Grand Premium 2")) return "G2";
    //     if (roomType.startsWith("Grand")) return "G";
    //     if (roomType.startsWith("Suite")) return "S";
    //     return roomType.substring(0, 1).toUpperCase();
    // }
}
