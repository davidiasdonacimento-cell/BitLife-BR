package com.bitlifebr.game.life;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/** Lê eventos do pacote instalado; se não houver catálogo válido, o jogo usa o catálogo interno. */
final class InstalledContentCatalog {
    private InstalledContentCatalog() { }

    static LifeEvent createAnnualEvent(Context context, LifeState life) {
        if (context == null || life == null) return null;
        try {
            SharedPreferences prefs = context.getSharedPreferences("content_pack", Context.MODE_PRIVATE);
            String installedPath = prefs.getString("installed_path", "");
            if (installedPath == null || installedPath.trim().isEmpty()) return null;
            File catalog = new File(new File(installedPath), "catalogs/events.json");
            if (!catalog.isFile() || catalog.length() <= 2 || catalog.length() > 32L * 1024L * 1024L) return null;
            String source;
            try (InputStream in = new FileInputStream(catalog);
                 ByteArrayOutputStream out = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[16384];
                int count;
                while ((count = in.read(buffer)) != -1) {
                    if (out.size() + count > 32 * 1024 * 1024) return null;
                    out.write(buffer, 0, count);
                }
                source = new String(out.toByteArray(), StandardCharsets.UTF_8);
            }
            JSONArray events = new JSONArray(source);
            List<JSONObject> eligible = new ArrayList<>();
            for (int i = 0; i < events.length(); i++) {
                JSONObject event = events.optJSONObject(i);
                if (event == null) continue;
                JSONArray ageRange = event.optJSONArray("ageRange");
                int minAge = ageRange == null ? 0 : ageRange.optInt(0, 0);
                int maxAge = ageRange == null ? 120 : ageRange.optInt(1, 120);
                if (life.age < minAge || life.age > maxAge) continue;
                String title = event.optString("title", "");
                if (title.isEmpty() || wasUsedRecently(life, title)) continue;
                JSONArray choices = event.optJSONArray("choices");
                if (choices == null || choices.length() < 3) continue;
                eligible.add(event);
            }
            if (eligible.isEmpty()) return null;
            int seed = 31 * life.id.hashCode() + 17 * life.age + life.history.size();
            JSONObject selected = eligible.get(new Random(seed).nextInt(eligible.size()));
            JSONArray choices = selected.getJSONArray("choices");
            String category = selected.optString("category", "");
            String city = life.city == null ? "sua cidade" : life.city;
            String description = selected.optString("description", "Um novo acontecimento faz parte da sua história.")
                    .replace("{{city}}", city);
            return new LifeEvent(life.id, life.age,
                    "pack_" + selected.optString("id", "event") + "_" + life.id + "_" + life.age,
                    selected.optString("title", "Novo acontecimento") + " • " + life.age + " anos",
                    description,
                    actionFor(category, 0), choiceLabel(choices, 0),
                    actionFor(category, 1), choiceLabel(choices, 1),
                    actionFor(category, 2), choiceLabel(choices, 2));
        } catch (Exception ignored) {
            return null;
        }
    }

    private static String choiceLabel(JSONArray choices, int index) {
        JSONObject choice = choices.optJSONObject(index);
        String label = choice == null ? "" : choice.optString("label", "");
        return label.isEmpty() ? "Escolher esta opção" : label;
    }

    private static String actionFor(String category, int index) {
        String key = category.toLowerCase(Locale.ROOT);
        String[][] actions = {
            {"family_time", "creative_play", "rest"},
            {"focus_study", "join_arts", "sports_group"},
            {"social_network", "focus_study", "sports_group"},
            {"professional_course", "work_shift", "social_network"},
            {"work_shift", "professional_course", "family_time"},
            {"community", "social_network", "personal_hobby"},
            {"personal_hobby", "join_arts", "creative_play"},
            {"family_time", "professional_course", "rest"},
            {"rest", "sports_group", "family_time"},
            {"family_time", "personal_hobby", "social_network"}
        };
        int group = 0;
        if (key.contains("escola")) group = 1;
        else if (key.contains("amizade")) group = 2;
        else if (key.contains("carreira")) group = 3;
        else if (key.contains("finan")) group = 4;
        else if (key.contains("comun")) group = 5;
        else if (key.contains("hobby")) group = 6;
        else if (key.contains("moradia")) group = 7;
        else if (key.contains("saúde") || key.contains("rotina")) group = 8;
        else if (key.contains("viagem")) group = 9;
        return actions[group][Math.max(0, Math.min(2, index))];
    }

    private static boolean wasUsedRecently(LifeState life, String title) {
        if (life.history == null) return false;
        int start = Math.max(0, life.history.size() - 60);
        for (int i = start; i < life.history.size(); i++) {
            String entry = life.history.get(i);
            if (entry != null && entry.contains(title)) return true;
        }
        return false;
    }
}
