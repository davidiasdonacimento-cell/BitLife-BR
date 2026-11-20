package com.bitlifebr.game;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bitlifebr.game.database.GameDatabase;
import com.bitlifebr.game.life.LifeEngine;
import com.bitlifebr.game.life.LifeEvent;
import com.bitlifebr.game.life.LifeFactory;
import com.bitlifebr.game.life.LifeState;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * Interface principal redesenhada: escolhas em cartões, painel de atributos,
 * atividades imediatas e acontecimentos anuais com três decisões.
 */
public final class MainActivity extends Activity {
    private static final int BG = Color.rgb(11, 17, 31);
    private static final int PANEL = Color.rgb(23, 33, 53);
    private static final int PANEL_ALT = Color.rgb(30, 43, 67);
    private static final int TEXT = Color.rgb(245, 247, 252);
    private static final int MUTED = Color.rgb(165, 179, 201);
    private static final int GREEN = Color.rgb(102, 222, 181);
    private static final int BLUE = Color.rgb(107, 177, 255);
    private static final int PURPLE = Color.rgb(183, 145, 255);
    private static final int ORANGE = Color.rgb(255, 185, 105);

    private GameDatabase database;
    private LifeState currentLife;
    private String currentPage = "home";

    @Override
    protected void onCreate(Bundle state) {
        super.onCreate(state);
        database = new GameDatabase(this);
        currentLife = database.loadActiveLife();
        if (currentLife == null) showHome();
        else showLife();
    }

    private void showHome() {
        currentPage = "home";
        LinearLayout root = screen("VIDA BR", "Um mundo inteiro para construir a sua história.");
        addHero(root, "🌎", "SUA VIDA COMEÇA AQUI",
                "Crie uma pessoa, acompanhe cada fase e escolha o que acontece em cada ano.");
        if (currentLife != null) {
            addSection(root, "CONTINUAR DE ONDE PAROU");
            addChoiceCard(root, "👤", currentLife.fullName() + " • " + currentLife.age + " anos",
                    currentLife.city + " - " + currentLife.stateCode + " • " + (currentLife.alive ? "Vida em andamento" : "Vida encerrada"),
                    GREEN, this::showLife);
        }
        addSection(root, "COMEÇAR");
        addChoiceCard(root, "✨", "Criar uma nova vida",
                "Escolha nome, personalidade, cidade e família.", GREEN, this::showStartOptions);
        addChoiceCard(root, "🌳", "Legado e vidas salvas",
                "Abra outras histórias ou escolha um descendente.", BLUE, this::showLives);
        addChoiceCard(root, "🏆", "Conquistas",
                "Veja marcos realmente alcançados nas suas histórias.", PURPLE, this::showAchievements);
        addChoiceCard(root, "📦", "Central de conteúdo",
                "Baixe e instale recursos publicados pelo servidor.", ORANGE,
                () -> startActivity(new Intent(this, ContentPackActivity.class)));
        addFooter(root, "Seu progresso fica salvo neste aparelho. Cada vida possui um registro separado.");
    }

    private void showStartOptions() {
        currentPage = "start";
        LinearLayout root = screen("COMO SUA VIDA COMEÇA?", "Escolha um dos caminhos disponíveis.");
        addChoiceCard(root, "🧑", "Criar personagem",
                "Comece uma história do seu jeito.", GREEN, () -> showCreationForm(false));
        addChoiceCard(root, "👶", "Ser filho ou filha",
                "Crie um novo personagem ligado à vida que está selecionada.", BLUE,
                () -> showCreationForm(true));
        addChoiceCard(root, "🌱", "Escolher descendente existente",
                "Continue a história por alguém da sua família.", PURPLE, this::showDescendants);
        addButton(root, "Voltar", this::showHome, false);
    }

    private void showCreationForm(boolean childMode) {
        currentPage = "create";
        LinearLayout root = screen(childMode ? "NOVA GERAÇÃO" : "CRIAR PERSONAGEM",
                childMode ? "O novo personagem ficará ligado à vida selecionada." : "Personalize a identidade e a família.");
        EditText firstName = addEdit(root, "Nome", "");
        EditText surname = addEdit(root, "Sobrenome", "");
        Spinner gender = addSpinner(root, "Gênero", new String[]{"Feminino", "Masculino", "Não especificado"});
        Spinner personality = addSpinner(root, "Personalidade", new String[]{
                "Equilibrado", "Curioso", "Disciplinado", "Sociável", "Reservado", "Criativo", "Ambicioso"
        });
        EditText birthDate = addEdit(root, "Data de nascimento (DD/MM/AAAA)",
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        EditText city = addEdit(root, "Município", "Salvador");
        EditText stateCode = addEdit(root, "UF", "BA");

        addSection(root, "SUA FAMÍLIA");
        Spinner familyMode = addSpinner(root, "Responsáveis", new String[]{
                "Gerar automaticamente", "Personalizar nomes"
        });
        EditText parentOne = addEdit(root, "Nome do responsável 1", "");
        EditText parentTwo = addEdit(root, "Nome do responsável 2", "");
        addLabel(root, "Quantidade de irmãos");
        NumberPicker siblings = new NumberPicker(this);
        siblings.setMinValue(0);
        siblings.setMaxValue(12);
        siblings.setValue(1);
        siblings.setWrapSelectorWheel(false);
        root.addView(siblings, match());

        addChoiceCard(root, "ℹ️", "Geração automática",
                childMode && currentLife != null
                        ? "O personagem será vinculado a " + currentLife.fullName() + "."
                        : "Nomes e relações familiares serão gerados quando não forem personalizados.",
                BLUE, () -> { });

        addButton(root, "CRIAR E COMEÇAR A JOGAR", () -> {
            String name = value(firstName);
            String last = value(surname);
            String cityName = value(city);
            String uf = value(stateCode).toUpperCase(Locale.ROOT);
            if (name.isEmpty() || last.isEmpty()) {
                message("Faltam dados", "Preencha nome e sobrenome para continuar.");
                return;
            }
            if (cityName.isEmpty() || !uf.matches("[A-Z]{2}")) {
                message("Localização inválida", "Informe um município e uma UF com duas letras.");
                return;
            }
            int age;
            try {
                age = calculateAge(value(birthDate));
            } catch (ParseException error) {
                message("Data inválida", "Use uma data real no formato DD/MM/AAAA e não escolha uma data futura.");
                return;
            }
            if (age < 0 || age > 120) {
                message("Idade fora do limite", "A idade inicial precisa ficar entre 0 e 120 anos.");
                return;
            }
            boolean custom = familyMode.getSelectedItemPosition() == 1;
            String parentId = childMode && currentLife != null ? currentLife.id : null;
            currentLife = LifeFactory.create(parentId, childMode ? "child" : "create",
                    name, last, value(birthDate), String.valueOf(gender.getSelectedItem()),
                    String.valueOf(personality.getSelectedItem()).toLowerCase(Locale.ROOT),
                    uf, cityName, custom, value(parentOne), value(parentTwo),
                    siblings.getValue(), new Random());
            currentLife.age = age;
            database.saveLife(currentLife);
            Toast.makeText(this, "Personagem criado e salvo!", Toast.LENGTH_SHORT).show();
            showLife();
        }, true);
        addButton(root, "Cancelar", this::showStartOptions, false);
    }

    /** Painel principal: as escolhas rápidas estão visíveis sem abrir caixas de diálogo. */
    private void showLife() {
        currentPage = "life";
        if (currentLife == null) { showHome(); return; }
        LinearLayout root = screen("MINHA VIDA", "Suas decisões, seus resultados.");
        addProfileCard(root);
        addSection(root, "SEUS ATRIBUTOS");
        addStat(root, "❤️ Saúde", currentLife.health, GREEN);
        addStat(root, "😊 Felicidade", currentLife.happiness, ORANGE);
        addStat(root, "🧠 Inteligência", currentLife.intelligence, BLUE);
        addStat(root, "✨ Aparência", currentLife.appearance, PURPLE);
        addStat(root, "🤝 Carisma", currentLife.charisma, BLUE);
        addStat(root, "📚 Disciplina", currentLife.discipline, GREEN);
        addStat(root, "⭐ Popularidade", currentLife.popularity, ORANGE);
        addCardText(root, "💰 Dinheiro", formatMoney(currentLife.moneyCents),
                currentLife.alive ? "Saldo atual da sua vida" : "A vida foi encerrada");

        addSection(root, "O QUE VOCÊ QUER FAZER AGORA?");
        addText(root, "Escolha uma atividade. Ela altera seus atributos e o histórico, mas não passa o ano.", 13, MUTED);
        for (QuickAction action : quickActions(currentLife)) {
            addChoiceCard(root, action.icon, action.title, action.description,
                    action.color, () -> performActivity(action.id));
        }

        addSection(root, "ACONTECIMENTO DO PRÓXIMO ANO");
        addChoiceCard(root, "📅", "Um novo ano está esperando por você",
                "Avance para receber um acontecimento da sua fase de vida e escolher entre três caminhos.",
                GREEN, this::showAnnualEvent);
        addSection(root, "FAMÍLIA E HISTÓRIA");
        addChoiceCard(root, "🏡", "Minha família",
                "Veja responsáveis, irmãos e vínculos da sua vida.", BLUE, this::showFamily);
        addChoiceCard(root, "📖", "Minha história",
                currentLife.history.size() + " acontecimentos registrados.", PURPLE, this::showHistory);
        addChoiceCard(root, "🌳", "Vidas e legado",
                "Troque de personagem ou crie uma nova geração.", ORANGE, this::showLives);
        addChoiceCard(root, "📦", "Central de conteúdo",
                "Instalação e atualização de recursos do jogo.", BLUE,
                () -> startActivity(new Intent(this, ContentPackActivity.class)));
        addBottomNavigation(root);
        addFooter(root, "A idade só muda quando você escolhe avançar um ano.");
    }

    private void addProfileCard(LinearLayout root) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(16), dp(17), dp(16), dp(17));
        card.setBackground(round(PANEL_ALT, 20));
        TextView avatar = text(avatarFor(currentLife), 36, TEXT);
        avatar.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams avatarParams = new LinearLayout.LayoutParams(dp(62), dp(62));
        card.addView(avatar, avatarParams);
        LinearLayout info = new LinearLayout(this);
        info.setOrientation(LinearLayout.VERTICAL);
        info.setPadding(dp(13), 0, 0, 0);
        TextView name = text(currentLife.fullName(), 19, TEXT);
        name.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        info.addView(name, match());
        info.addView(text(currentLife.age + " anos • " + ageStage(currentLife.age), 13, GREEN), match());
        info.addView(text("📍 " + currentLife.city + " - " + currentLife.stateCode, 12, MUTED), match());
        card.addView(info, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        LinearLayout.LayoutParams params = match();
        params.bottomMargin = dp(12);
        root.addView(card, params);
    }

    private void performActivity(String actionId) {
        if (currentLife == null || !currentLife.alive) return;
        try {
            currentLife = LifeEngine.applyActivity(currentLife, actionId);
            database.saveLife(currentLife);
            Toast.makeText(this, "Escolha registrada e atributos atualizados.", Toast.LENGTH_SHORT).show();
            showLife();
        } catch (IllegalArgumentException error) {
            message("Atividade indisponível", error.getMessage());
        }
    }

    private void showAnnualEvent() {
        if (currentLife == null || !currentLife.alive) return;
        if (currentLife.age >= 120) {
            message("Limite de idade", "Esta vida chegou ao limite de idade disponível nesta versão.");
            return;
        }
        currentPage = "event";
        LifeEvent event = LifeEngine.createAnnualEvent(currentLife);
        LinearLayout root = screen("UM NOVO ANO", "Uma situação apareceu na sua vida. O que você escolhe?");
        addHero(root, "📰", "ACONTECIMENTO • " + currentLife.age + " ANOS",
                "Esta decisão avança sua idade em um ano. Leia as opções e escolha uma delas.");
        addCardText(root, event.title, event.description, "📍 " + currentLife.city + " • " + ageStage(currentLife.age));
        addSection(root, "ESCOLHA SEU CAMINHO");
        addChoiceCard(root, "1", event.choiceALabel,
                choiceDescription(event.choiceAId), GREEN,
                () -> applyAnnualChoice(event, event.choiceAId));
        addChoiceCard(root, "2", event.choiceBLabel,
                choiceDescription(event.choiceBId), BLUE,
                () -> applyAnnualChoice(event, event.choiceBId));
        addChoiceCard(root, "3", event.choiceCLabel,
                choiceDescription(event.choiceCId), PURPLE,
                () -> applyAnnualChoice(event, event.choiceCId));
        addButton(root, "Voltar sem avançar o ano", this::showLife, false);
    }

    private void applyAnnualChoice(LifeEvent event, String choiceId) {
        try {
            currentLife = database.applyAnnualChoice(event, choiceId);
            String label = event.choiceAId.equals(choiceId) ? event.choiceALabel
                    : event.choiceBId.equals(choiceId) ? event.choiceBLabel : event.choiceCLabel;
            showEventResult(label);
        } catch (IllegalArgumentException | IllegalStateException error) {
            currentLife = database.loadActiveLife();
            message("Não foi possível aplicar a escolha", error.getMessage()
                    + "\nO save foi recarregado para evitar duplicação.");
            showLife();
        }
    }

    private void showEventResult(String selected) {
        currentPage = "result";
        LinearLayout root = screen("DECISÃO REGISTRADA", "Seu ano avançou e o progresso foi salvo.");
        addHero(root, "✅", "VOCÊ ESCOLHEU",
                selected + ". A sua vida agora está em uma nova fase.");
        addProfileCard(root);
        addSection(root, "ATRIBUTOS ATUALIZADOS");
        addStat(root, "❤️ Saúde", currentLife.health, GREEN);
        addStat(root, "😊 Felicidade", currentLife.happiness, ORANGE);
        addStat(root, "🧠 Inteligência", currentLife.intelligence, BLUE);
        addStat(root, "🤝 Carisma", currentLife.charisma, PURPLE);
        addButton(root, "CONTINUAR MINHA VIDA", this::showLife, true);
        addButton(root, "Ver o que aconteceu", this::showHistory, false);
    }

    private List<QuickAction> quickActions(LifeState life) {
        java.util.ArrayList<QuickAction> items = new java.util.ArrayList<>();
        if (life.age < 6) {
            items.add(new QuickAction("creative_play", "🧸", "Brincar e explorar",
                    "Aumenta a felicidade e desenvolve carisma.", GREEN));
            items.add(new QuickAction("family_time", "🏡", "Ficar com a família",
                    "Fortalece o bem-estar e a convivência.", BLUE));
            items.add(new QuickAction("rest", "😴", "Descansar",
                    "Recupera saúde e felicidade.", PURPLE));
        } else if (life.age < 12) {
            items.add(new QuickAction("focus_study", "📚", "Estudar",
                    "Desenvolve inteligência e disciplina.", BLUE));
            items.add(new QuickAction("join_arts", "🎨", "Fazer uma atividade artística",
                    "Desenvolve carisma e felicidade.", PURPLE));
            items.add(new QuickAction("sports_group", "⚽", "Praticar esporte",
                    "Melhora saúde e felicidade.", GREEN));
            items.add(new QuickAction("rest", "😴", "Descansar",
                    "Recupera saúde e felicidade.", ORANGE));
        } else if (life.age < 18) {
            items.add(new QuickAction("study_group", "📚", "Participar de um grupo de estudos",
                    "Melhora inteligência e disciplina.", BLUE));
            items.add(new QuickAction("sports_group", "🏃", "Praticar esporte",
                    "Melhora saúde e felicidade.", GREEN));
            items.add(new QuickAction("social_network", "🧑‍🤝‍🧑", "Socializar",
                    "Desenvolve carisma e popularidade.", PURPLE));
            items.add(new QuickAction("rest", "😴", "Descansar",
                    "Recupera saúde e felicidade.", ORANGE));
        } else if (life.age < 60) {
            items.add(new QuickAction("work_shift", "💼", "Fazer trabalho extra",
                    "Receba R$ 75,00 e desenvolva disciplina.", GREEN));
            items.add(new QuickAction("professional_course", "🎓", "Fazer um curso",
                    "Aumenta inteligência e disciplina.", BLUE));
            items.add(new QuickAction("social_network", "🤝", "Conhecer pessoas",
                    "Desenvolve carisma e popularidade.", PURPLE));
            items.add(new QuickAction("rest", "😴", "Descansar",
                    "Recupera saúde e felicidade.", ORANGE));
        } else {
            items.add(new QuickAction("community", "🌻", "Participar da comunidade",
                    "Aumenta felicidade e popularidade.", GREEN));
            items.add(new QuickAction("personal_hobby", "🎻", "Dedicar tempo a um hobby",
                    "Estimula felicidade e interesses pessoais.", PURPLE));
            items.add(new QuickAction("family_time", "🏡", "Ficar com a família",
                    "Fortalece os vínculos e a felicidade.", BLUE));
            items.add(new QuickAction("rest", "😴", "Descansar",
                    "Recupera saúde e felicidade.", ORANGE));
        }
        return items;
    }

    private void showFamily() {
        currentPage = "family";
        LinearLayout root = screen("MINHA FAMÍLIA", "Pessoas ligadas à história de " + currentLife.fullName() + ".");
        addChoiceCard(root, "👨", "Responsável 1", currentLife.fatherName,
                BLUE, () -> message("Responsável 1", currentLife.fatherName));
        addChoiceCard(root, "👩", "Responsável 2", currentLife.motherName,
                PURPLE, () -> message("Responsável 2", currentLife.motherName));
        addSection(root, "IRMÃOS • " + currentLife.siblings.size());
        if (currentLife.siblings.isEmpty()) {
            addCardText(root, "Nenhum irmão cadastrado", "A lista de irmãos foi definida na criação desta vida.");
        } else {
            for (int i = 0; i < currentLife.siblings.size(); i++) {
                final String sibling = currentLife.siblings.get(i);
                addChoiceCard(root, "🧒", sibling, "Irmão ou irmã • membro da família",
                        BLUE, () -> message("Membro da família", sibling));
            }
        }
        addChoiceCard(root, "💚", "Passar tempo com a família",
                "Aumenta felicidade e carisma sem avançar a idade.", GREEN,
                () -> performActivity("family_time"));
        addButton(root, "Voltar à minha vida", this::showLife, false);
    }

    private void showHistory() {
        currentPage = "history";
        LinearLayout root = screen("MINHA HISTÓRIA", "Acontecimentos e escolhas que você já registrou.");
        if (currentLife == null || currentLife.history.isEmpty()) {
            addCardText(root, "Sua história está começando", "Escolha uma atividade ou avance um ano para registrar acontecimentos.");
        } else {
            addCardText(root, currentLife.history.size() + " acontecimentos",
                    "Os registros mais recentes aparecem primeiro.");
            for (int i = currentLife.history.size() - 1; i >= 0; i--) {
                addChoiceCard(root, "•", "Registro " + (i + 1), currentLife.history.get(i),
                        i % 2 == 0 ? BLUE : PURPLE, () -> { });
            }
        }
        addButton(root, "Voltar à minha vida", this::showLife, true);
    }

    private void showDescendants() {
        currentPage = "descendants";
        List<LifeState> descendants = database.loadDescendants();
        if (descendants.isEmpty()) {
            LinearLayout root = screen("DESCENDENTES", "Continue uma história da sua família.");
            addCardText(root, "Nenhum descendente salvo ainda",
                    "Na tela Nova vida, escolha “Ser filho ou filha” para criar o próximo membro da família.");
            addButton(root, "Criar filho ou filha", () -> showCreationForm(true), true);
            addButton(root, "Voltar", this::showLives, false);
            return;
        }
        LinearLayout root = screen("ESCOLHER DESCENDENTE", "Selecione qualquer personagem da lista.");
        for (LifeState descendant : descendants) {
            addChoiceCard(root, "🌱", descendant.fullName() + " • " + descendant.age + " anos",
                    descendant.city + " - " + descendant.stateCode + " • " + ageStage(descendant.age),
                    GREEN, () -> {
                        currentLife = descendant;
                        database.setActiveLife(currentLife.id);
                        showLife();
                    });
        }
        addButton(root, "Voltar", this::showLives, false);
    }

    private void showLives() {
        currentPage = "lives";
        List<LifeState> lives = database.loadLives();
        LinearLayout root = screen("LEGADO E VIDAS", "Cada personagem mantém sua própria história salva.");
        if (lives.isEmpty()) {
            addCardText(root, "Nenhuma vida salva", "Comece uma nova história para preencher seu legado.");
        } else {
            for (LifeState life : lives) {
                boolean active = currentLife != null && currentLife.id.equals(life.id);
                addChoiceCard(root, active ? "🟢" : "👤",
                        life.fullName() + " • " + life.age + " anos",
                        life.city + " - " + life.stateCode + (active ? " • Vida selecionada" : " • " + (life.isDescendant() ? "Descendente" : "Vida independente")),
                        active ? GREEN : BLUE, () -> {
                            currentLife = life;
                            database.setActiveLife(currentLife.id);
                            showLife();
                        });
            }
        }
        addChoiceCard(root, "🌱", "Escolher um descendente",
                "Continue pela geração seguinte.", PURPLE, this::showDescendants);
        addButton(root, "Nova vida independente", () -> showCreationForm(false), true);
        addButton(root, "Voltar", this::showLife, false);
    }

    private void showAchievements() {
        currentPage = "achievements";
        LinearLayout root = screen("CONQUISTAS", "Marcos liberados pelos dados reais das suas vidas.");
        java.util.ArrayList<String> earned = new java.util.ArrayList<>();
        if (currentLife != null) {
            if (currentLife.age >= 1) earned.add("Primeiro aniversário • avançou o primeiro ano");
            if (currentLife.age >= 10) earned.add("Uma década • chegou aos 10 anos");
            if (currentLife.age >= 18) earned.add("Vida adulta • completou 18 anos");
            if (currentLife.age >= 60) earned.add("Nova fase • chegou aos 60 anos");
            if (currentLife.history.size() >= 10) earned.add("Memórias • registrou 10 acontecimentos");
            if (currentLife.siblings.size() >= 3) earned.add("Família grande • possui três ou mais irmãos");
        }
        if (earned.isEmpty()) {
            addCardText(root, "Sua primeira conquista está esperando",
                    "Avance na sua história e faça escolhas para desbloquear marcos.");
        } else {
            for (String item : earned) addChoiceCard(root, "🏅", item,
                    "Conquista desbloqueada nesta vida.", GREEN, () -> { });
        }
        addButton(root, "Voltar à minha vida", this::showLife, true);
    }

    private void addBottomNavigation(LinearLayout root) {
        addSection(root, "ATALHOS");
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER);
        addNavButton(row, "👤\nVida", this::showLife);
        addNavButton(row, "👨‍👩‍👧\nFamília", this::showFamily);
        addNavButton(row, "📖\nHistória", this::showHistory);
        addNavButton(row, "🌳\nLegado", this::showLives);
        root.addView(row, match());
    }

    private void addNavButton(LinearLayout row, String label, Runnable action) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(11);
        button.setTextColor(TEXT);
        button.setMinHeight(dp(54));
        button.setBackground(round(PANEL_ALT, 12));
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f);
        params.setMargins(dp(3), dp(3), dp(3), dp(3));
        row.addView(button, params);
    }

    private void addHero(LinearLayout root, String icon, String title, String detail) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER);
        card.setPadding(dp(18), dp(23), dp(18), dp(23));
        card.setBackground(round(PANEL_ALT, 20));
        TextView image = text(icon, 43, TEXT);
        image.setGravity(Gravity.CENTER);
        card.addView(image, match());
        TextView heading = text(title, 16, GREEN);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setGravity(Gravity.CENTER);
        heading.setPadding(0, dp(10), 0, dp(7));
        card.addView(heading, match());
        TextView body = text(detail, 13, MUTED);
        body.setGravity(Gravity.CENTER);
        card.addView(body, match());
        LinearLayout.LayoutParams params = match();
        params.bottomMargin = dp(12);
        root.addView(card, params);
    }

    private void addSection(LinearLayout root, String title) {
        TextView label = text(title, 12, GREEN);
        label.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        label.setLetterSpacing(0.06f);
        label.setPadding(0, dp(17), 0, dp(8));
        root.addView(label, match());
    }

    private void addStat(LinearLayout root, String label, int value, int color) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(13), dp(10), dp(13), dp(10));
        card.setBackground(round(PANEL, 12));
        LinearLayout line = new LinearLayout(this);
        line.setGravity(Gravity.CENTER_VERTICAL);
        TextView name = text(label, 13, TEXT);
        line.addView(name, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView amount = text(value + "/100", 12, MUTED);
        line.addView(amount, wrap());
        card.addView(line, match());
        ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
        progress.setMax(100);
        progress.setProgress(Math.max(0, Math.min(100, value)));
        progress.setProgressTintList(android.content.res.ColorStateList.valueOf(color));
        progress.setProgressBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.rgb(44, 57, 78)));
        LinearLayout.LayoutParams barParams = match();
        barParams.topMargin = dp(5);
        card.addView(progress, barParams);
        LinearLayout.LayoutParams params = match();
        params.bottomMargin = dp(6);
        root.addView(card, params);
    }

    private void addCardText(LinearLayout root, String title, String detail, String footer) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(14), dp(15), dp(14));
        card.setBackground(round(PANEL, 15));
        TextView headline = text(title, 16, TEXT);
        headline.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(headline, match());
        TextView body = text(detail, 13, MUTED);
        body.setPadding(0, dp(6), 0, dp(5));
        card.addView(body, match());
        card.addView(text(footer, 11, GREEN), match());
        LinearLayout.LayoutParams params = match();
        params.bottomMargin = dp(8);
        root.addView(card, params);
    }

    private void addCardText(LinearLayout root, String title, String detail) {
        addCardText(root, title, detail, "");
    }

    private void addChoiceCard(LinearLayout root, String icon, String title,
                               String detail, int accent, Runnable action) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(dp(13), dp(13), dp(13), dp(13));
        card.setBackground(round(PANEL, 15));
        card.setClickable(true);
        card.setFocusable(true);

        TextView symbol = text(icon, 23, accent);
        symbol.setGravity(Gravity.CENTER);
        LinearLayout.LayoutParams symbolParams = new LinearLayout.LayoutParams(dp(42), dp(42));
        card.addView(symbol, symbolParams);

        LinearLayout copy = new LinearLayout(this);
        copy.setOrientation(LinearLayout.VERTICAL);
        copy.setPadding(dp(11), 0, dp(7), 0);
        TextView headline = text(title, 15, TEXT);
        headline.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        copy.addView(headline, match());
        TextView description = text(detail, 12, MUTED);
        description.setPadding(0, dp(4), 0, 0);
        copy.addView(description, match());
        card.addView(copy, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        TextView arrow = text("›", 28, accent);
        card.addView(arrow, wrap());

        card.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = match();
        params.bottomMargin = dp(8);
        root.addView(card, params);
    }

    private void addButton(LinearLayout root, String label, Runnable action, boolean primary) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(14);
        button.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        button.setTextColor(primary ? Color.rgb(10, 33, 31) : TEXT);
        button.setBackground(round(primary ? GREEN : PANEL_ALT, 14));
        button.setPadding(dp(12), dp(8), dp(12), dp(8));
        button.setOnClickListener(v -> action.run());
        LinearLayout.LayoutParams params = match();
        params.topMargin = dp(6);
        params.bottomMargin = dp(5);
        root.addView(button, params);
    }

    private LinearLayout screen(String title, String subtitle) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BG);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(18), dp(24), dp(18), dp(26));
        TextView brand = text("VIDA BR   •   SIMULADOR DE VIDA", 10, GREEN);
        brand.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        brand.setLetterSpacing(0.10f);
        root.addView(brand, match());
        TextView heading = text(title, 25, TEXT);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        heading.setPadding(0, dp(7), 0, 0);
        root.addView(heading, match());
        TextView sub = text(subtitle, 13, MUTED);
        sub.setPadding(0, dp(4), 0, dp(14));
        root.addView(sub, match());
        scroll.addView(root);
        setContentView(scroll);
        return root;
    }

    private EditText addEdit(LinearLayout root, String label, String initial) {
        addLabel(root, label);
        EditText edit = new EditText(this);
        edit.setSingleLine(true);
        edit.setText(initial);
        edit.setTextColor(TEXT);
        edit.setHintTextColor(MUTED);
        edit.setTextSize(15);
        edit.setPadding(dp(12), dp(9), dp(12), dp(9));
        edit.setBackground(round(PANEL_ALT, 10));
        root.addView(edit, match());
        return edit;
    }

    private Spinner addSpinner(LinearLayout root, String label, String[] choices) {
        addLabel(root, label);
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, choices);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setBackground(round(PANEL_ALT, 10));
        root.addView(spinner, match());
        return spinner;
    }

    private void addLabel(LinearLayout root, String value) {
        TextView label = text(value, 12, MUTED);
        label.setPadding(0, dp(6), 0, dp(5));
        root.addView(label, match());
    }

    private void addText(LinearLayout root, String value, int size, int color) {
        TextView view = text(value, size, color);
        view.setPadding(0, 0, 0, dp(8));
        root.addView(view, match());
    }

    private void addFooter(LinearLayout root, String value) {
        TextView footer = text(value, 11, MUTED);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(18), 0, dp(5));
        root.addView(footer, match());
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private GradientDrawable round(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private LinearLayout.LayoutParams match() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private LinearLayout.LayoutParams wrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }

    private String value(EditText edit) {
        return edit.getText() == null ? "" : edit.getText().toString().trim();
    }

    private int calculateAge(String input) throws ParseException {
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.ROOT);
        format.setLenient(false);
        Date parsed = format.parse(input);
        if (parsed == null || parsed.after(new Date())) throw new ParseException("Data inválida", 0);
        Calendar birth = Calendar.getInstance();
        birth.setTime(parsed);
        Calendar today = Calendar.getInstance();
        int age = today.get(Calendar.YEAR) - birth.get(Calendar.YEAR);
        if (today.get(Calendar.DAY_OF_YEAR) < birth.get(Calendar.DAY_OF_YEAR)) age--;
        return age;
    }

    private String formatMoney(long cents) {
        return String.format(Locale.forLanguageTag("pt-BR"), "R$ %,.2f", cents / 100.0);
    }

    private String ageStage(int age) {
        if (age < 6) return "Primeira infância";
        if (age < 12) return "Infância";
        if (age < 18) return "Adolescência";
        if (age < 60) return "Vida adulta";
        return "Terceira idade";
    }

    private String avatarFor(LifeState life) {
        if ("Feminino".equalsIgnoreCase(life.gender)) return "👩";
        if ("Masculino".equalsIgnoreCase(life.gender)) return "👨";
        return "🧑";
    }

    private String choiceDescription(String id) {
        switch (id) {
            case "family_time": return "Melhora felicidade, saúde e convivência.";
            case "creative_play": return "Estimula criatividade, felicidade e carisma.";
            case "rest": return "Recupera saúde e felicidade.";
            case "focus_study": case "study_group": return "Desenvolve inteligência e disciplina.";
            case "join_arts": return "Aumenta felicidade, carisma e popularidade.";
            case "sports_group": return "Melhora saúde, felicidade e carisma.";
            case "professional_course": return "Investe em inteligência e disciplina.";
            case "social_network": return "Desenvolve carisma, popularidade e felicidade.";
            case "work_shift": return "Recebe R$ 75,00 e aumenta disciplina, com custo de energia.";
            case "community": return "Aumenta felicidade e popularidade.";
            case "personal_hobby": return "Estimula felicidade e interesses pessoais.";
            default: return "Esta escolha altera os dados da sua vida.";
        }
    }

    private void message(String title, String body) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(body)
                .setPositiveButton("Entendi", null).show();
    }

    private void onBackToLife() { showLife(); }

    @Override
    public void onBackPressed() {
        if ("event".equals(currentPage) || "result".equals(currentPage)
                || "family".equals(currentPage) || "history".equals(currentPage)
                || "lives".equals(currentPage) || "descendants".equals(currentPage)
                || "achievements".equals(currentPage)) {
            showLife();
        } else if ("create".equals(currentPage)) {
            showStartOptions();
        } else if ("start".equals(currentPage)) {
            showHome();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        if (database != null) database.close();
        super.onDestroy();
    }

    private static final class QuickAction {
        final String id, icon, title, description;
        final int color;
        QuickAction(String id, String icon, String title, String description, int color) {
            this.id = id; this.icon = icon; this.title = title;
            this.description = description; this.color = color;
        }
    }
}