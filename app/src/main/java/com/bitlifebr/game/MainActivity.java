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

/** Interface Android nativa da primeira etapa jogável do BitLife BR. */
public final class MainActivity extends Activity {
    private static final int BACKGROUND = Color.rgb(14, 20, 34);
    private static final int SURFACE = Color.rgb(25, 34, 52);
    private static final int TEXT = Color.rgb(242, 245, 250);
    private static final int MUTED = Color.rgb(174, 185, 202);
    private static final int ACCENT = Color.rgb(105, 220, 190);

    private GameDatabase database;
    private LifeState currentLife;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        database = new GameDatabase(this);
        currentLife = database.loadActiveLife();
        showHome();
    }

    private void showHome() {
        LinearLayout root = screen("BITLIFE BR", "Sua vida. Suas escolhas.");
        TextView intro = text(
                "Um universo particular, muitas histórias e decisões que mudam cada vida.",
                15, MUTED);
        intro.setPadding(0, dp(18), 0, dp(22));
        root.addView(intro, matchWrap());

        if (currentLife != null) {
            addSection(root, "VIDA ATUAL");
            addCardText(root, currentLife.fullName() + "  •  " + currentLife.age + " anos",
                    currentLife.city + " - " + currentLife.stateCode
                            + "  •  " + (currentLife.alive ? "Em andamento" : "Encerrada"));
        }

        addButton(root, "Nova vida", this::showStartOptions, true);
        addButton(root, "Continuar", () -> {
            if (currentLife == null) {
                message("Nenhuma vida disponível", "Crie uma vida para começar a jogar.");
            } else {
                showLife();
            }
        }, currentLife != null);
        addButton(root, "Legado e vidas salvas", this::showLives, false);
        addButton(root, "Conquistas", this::showAchievements, false);
        addButton(root, "Central de conteúdo • baixar recursos",
                () -> startActivity(new Intent(this, ContentPackActivity.class)), false);
        addFooter(root, "Android nativo • Saves locais protegidos contra sobrescrita entre vidas");
        setScreen(root);
    }

    private void showStartOptions() {
        LinearLayout root = screen("COMEÇAR UMA VIDA", "Escolha como sua história começa.");
        addButton(root, "1. Criar personagem", () -> showCreationForm(false), true);
        addButton(root, "2. Ser filho ou filha", () -> showCreationForm(true), false);
        addButton(root, "3. Escolher descendente existente", this::showDescendants, false);
        addButton(root, "Voltar", this::showHome, false);
        addFooter(root, "Cada vida fica salva separadamente na sua conta/aparelho.");
        setScreen(root);
    }

    private void showCreationForm(boolean childMode) {
        LinearLayout root = screen(childMode ? "NASCER EM UMA FAMÍLIA" : "CRIAR PERSONAGEM",
                childMode
                        ? "Escolha os dados do personagem e de sua família."
                        : "Defina a identidade e o ponto de partida da nova vida.");

        EditText firstName = addEdit(root, "Nome", "");
        EditText surname = addEdit(root, "Sobrenome", "");
        Spinner gender = addSpinner(root, "Gênero", new String[]{
                "Feminino", "Masculino", "Não especificado"
        });
        Spinner personality = addSpinner(root, "Personalidade inicial", new String[]{
                "Equilibrado", "Curioso", "Disciplinado", "Sociável",
                "Reservado", "Criativo", "Ambicioso"
        });
        EditText birthDate = addEdit(root, "Data de nascimento (DD/MM/AAAA)",
                new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date()));
        EditText city = addEdit(root, "Município", "Salvador");
        EditText stateCode = addEdit(root, "UF", "BA");

        addSection(root, "FAMÍLIA");
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
        root.addView(siblings, matchWrap());

        TextView familyNote = text(childMode && currentLife != null
                ? "Este personagem será registrado como descendente de "
                    + currentLife.fullName() + "."
                : "Os nomes automáticos serão gerados pelo jogo. Você poderá ampliar a família nas próximas etapas.",
                13, MUTED);
        familyNote.setPadding(0, dp(8), 0, dp(16));
        root.addView(familyNote, matchWrap());

        addButton(root, "Criar e salvar esta vida", () -> {
            String name = value(firstName);
            String last = value(surname);
            String cityName = value(city);
            String uf = value(stateCode).toUpperCase(Locale.ROOT);
            if (name.isEmpty() || last.isEmpty()) {
                message("Dados incompletos", "Informe nome e sobrenome.");
                return;
            }
            if (cityName.isEmpty() || !uf.matches("[A-Z]{2}")) {
                message("Localização inválida", "Informe um município e uma UF com duas letras.");
                return;
            }
            int age;
            try {
                age = calculateAge(value(birthDate));
            } catch (ParseException e) {
                message("Data inválida", "Use uma data real no formato DD/MM/AAAA, sem data futura.");
                return;
            }
            if (age < 0 || age > 120) {
                message("Data fora do limite", "A idade inicial precisa ficar entre 0 e 120 anos.");
                return;
            }

            boolean customParents = familyMode.getSelectedItemPosition() == 1;
            String parentLifeId = childMode && currentLife != null ? currentLife.id : null;
            String mode = childMode ? "child" : "create";
            LifeState created = LifeFactory.create(
                    parentLifeId, mode, name, last, value(birthDate),
                    String.valueOf(gender.getSelectedItem()),
                    String.valueOf(personality.getSelectedItem()).toLowerCase(Locale.ROOT),
                    uf, cityName, customParents, value(parentOne), value(parentTwo),
                    siblings.getValue(), new Random());
            created.age = age;
            currentLife = created;
            database.saveLife(currentLife);
            Toast.makeText(this, "Vida criada e salva neste aparelho.", Toast.LENGTH_SHORT).show();
            showLife();
        }, true);
        addButton(root, "Cancelar", this::showStartOptions, false);
        setScreen(root);
    }

    private void showLife() {
        if (currentLife == null) {
            showHome();
            return;
        }
        LinearLayout root = screen(currentLife.fullName(), currentLife.age + " anos • "
                + currentLife.city + " - " + currentLife.stateCode);

        addSection(root, "ATRIBUTOS");
        addCardText(root, "Saúde " + currentLife.health + " / 100",
                "Felicidade " + currentLife.happiness + "  •  Inteligência "
                        + currentLife.intelligence);
        addCardText(root, "Aparência " + currentLife.appearance + "  •  Carisma "
                        + currentLife.charisma,
                "Disciplina " + currentLife.discipline + "  •  Popularidade "
                        + currentLife.popularity);
        addCardText(root, "Dinheiro: " + formatMoney(currentLife.moneyCents),
                currentLife.alive ? "Vida em andamento" : "Vida encerrada");

        addSection(root, "FAMÍLIA");
        addCardText(root, "Responsável 1: " + currentLife.fatherName,
                "Responsável 2: " + currentLife.motherName);
        addCardText(root, "Irmãos: " + currentLife.siblings.size(),
                currentLife.siblings.isEmpty() ? "Nenhum irmão registrado."
                        : joinNames(currentLife.siblings));

        addButton(root, "Avançar um ano", this::showAnnualEvent, true);
        addButton(root, "História da vida", this::showHistory, false);
        addButton(root, "Voltar ao início", this::showHome, false);
        addFooter(root, "A idade do personagem só avança quando você escolhe avançar.");
        setScreen(root);
    }

    private void showAnnualEvent() {
        if (currentLife == null || !currentLife.alive) return;
        if (currentLife.age >= 120) {
            message("Limite de idade", "Esta vida chegou ao limite de idade desta primeira etapa.");
            return;
        }
        LifeEvent event = LifeEngine.createAnnualEvent(currentLife);
        new AlertDialog.Builder(this)
                .setTitle(event.title)
                .setMessage(event.description)
                .setItems(new String[]{event.choiceALabel, event.choiceBLabel}, (dialog, which) -> {
                    String choiceId = which == 0 ? event.choiceAId : event.choiceBId;
                    try {
                        currentLife = database.applyAnnualChoice(event, choiceId);
                        Toast.makeText(this, "Ano avançado e progresso salvo.", Toast.LENGTH_SHORT).show();
                        showLife();
                    } catch (IllegalArgumentException | IllegalStateException error) {
                        currentLife = database.loadActiveLife();
                        message("Progresso atualizado", error.getMessage() + "\\nA vida foi recarregada para evitar avanço duplicado.");
                        showLife();
                    }
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showHistory() {
        if (currentLife == null) return;
        LinearLayout root = screen("HISTÓRIA DA VIDA", currentLife.fullName());
        if (currentLife.history.isEmpty()) {
            addCardText(root, "Nenhum acontecimento registrado", "Avance um ano para gerar o primeiro evento.");
        } else {
            for (int i = currentLife.history.size() - 1; i >= 0; i--) {
                addCardText(root, "Registro " + (i + 1), currentLife.history.get(i));
            }
        }
        addButton(root, "Voltar à vida", this::showLife, true);
        setScreen(root);
    }

    private void showDescendants() {
        List<LifeState> descendants = database.loadDescendants();
        if (descendants.isEmpty()) {
            message("Nenhum descendente salvo",
                    "Crie uma vida pelo caminho “Ser filho ou filha” enquanto outra vida estiver selecionada. "
                            + "Os personagens vinculados aparecerão aqui.");
            return;
        }
        String[] labels = new String[descendants.size()];
        for (int i = 0; i < descendants.size(); i++) {
            LifeState life = descendants.get(i);
            labels[i] = life.fullName() + " • " + life.age + " anos • "
                    + life.city + " - " + life.stateCode;
        }
        new AlertDialog.Builder(this)
                .setTitle("Escolher descendente")
                .setItems(labels, (dialog, which) -> {
                    currentLife = descendants.get(which);
                    database.setActiveLife(currentLife.id);
                    showLife();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void showLives() {
        List<LifeState> lives = database.loadLives();
        LinearLayout root = screen("LEGADO E VIDAS", "Cada vida possui um save independente.");
        if (lives.isEmpty()) {
            addCardText(root, "Nenhuma vida salva", "Comece uma nova história para preencher seu legado.");
        } else {
            for (LifeState life : lives) {
                addButton(root, life.fullName() + " • " + life.age + " anos",
                        () -> {
                            currentLife = life;
                            database.setActiveLife(life.id);
                            showLife();
                        }, currentLife != null && currentLife.id.equals(life.id));
                addCardText(root, life.city + " - " + life.stateCode,
                        life.isDescendant() ? "Descendente de outra vida" : "Vida independente");
            }
        }
        addButton(root, "Nova vida", this::showStartOptions, false);
        addButton(root, "Voltar", this::showHome, false);
        setScreen(root);
    }

    private void showAchievements() {
        LinearLayout root = screen("CONQUISTAS", "Progresso das suas histórias.");
        addCardText(root, "Sistema em desenvolvimento",
                "As conquistas serão conectadas aos dados reais de idade, educação, carreira, família e legado. "
                        + "Esta tela não concede recompensas fictícias.");
        addButton(root, "Voltar", this::showHome, true);
        setScreen(root);
    }

    private LinearLayout screen(String title, String subtitle) {
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.setBackgroundColor(BACKGROUND);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(22), dp(28), dp(22), dp(30));
        TextView heading = text(title, 27, TEXT);
        heading.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        root.addView(heading, matchWrap());
        TextView sub = text(subtitle, 14, MUTED);
        sub.setPadding(0, dp(6), 0, dp(18));
        root.addView(sub, matchWrap());
        scroll.addView(root);
        setContentView(scroll);
        return root;
    }

    private void addSection(LinearLayout root, String label) {
        TextView view = text(label, 12, ACCENT);
        view.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        view.setPadding(0, dp(18), 0, dp(8));
        root.addView(view, matchWrap());
    }

    private void addCardText(LinearLayout root, String title, String detail) {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(15), dp(13), dp(15), dp(13));
        card.setBackground(background(SURFACE, 14));
        TextView primary = text(title, 16, TEXT);
        primary.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        card.addView(primary, matchWrap());
        TextView secondary = text(detail, 13, MUTED);
        secondary.setPadding(0, dp(5), 0, 0);
        card.addView(secondary, matchWrap());
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(9);
        root.addView(card, params);
    }

    private void addButton(LinearLayout root, String label, Runnable action, boolean primary) {
        Button button = new Button(this);
        button.setText(label);
        button.setAllCaps(false);
        button.setTextSize(15);
        button.setTextColor(primary ? Color.rgb(9, 30, 31) : TEXT);
        button.setBackground(background(primary ? ACCENT : SURFACE, 13));
        button.setPadding(dp(12), dp(8), dp(12), dp(8));
        button.setOnClickListener(view -> action.run());
        LinearLayout.LayoutParams params = matchWrap();
        params.topMargin = dp(7);
        params.bottomMargin = dp(2);
        root.addView(button, params);
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
        edit.setBackground(background(SURFACE, 10));
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(8);
        root.addView(edit, params);
        return edit;
    }

    private Spinner addSpinner(LinearLayout root, String label, String[] options) {
        addLabel(root, label);
        Spinner spinner = new Spinner(this);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setBackground(background(SURFACE, 10));
        LinearLayout.LayoutParams params = matchWrap();
        params.bottomMargin = dp(8);
        root.addView(spinner, params);
        return spinner;
    }

    private void addLabel(LinearLayout root, String label) {
        TextView view = text(label, 13, MUTED);
        view.setPadding(0, dp(6), 0, dp(5));
        root.addView(view, matchWrap());
    }

    private void addFooter(LinearLayout root, String value) {
        TextView footer = text(value, 12, MUTED);
        footer.setGravity(Gravity.CENTER);
        footer.setPadding(0, dp(22), 0, 0);
        root.addView(footer, matchWrap());
    }

    private TextView text(String value, int size, int color) {
        TextView view = new TextView(this);
        view.setText(value);
        view.setTextSize(size);
        view.setTextColor(color);
        return view;
    }

    private GradientDrawable background(int color, int radius) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(dp(radius));
        return drawable;
    }

    private LinearLayout.LayoutParams matchWrap() {
        return new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
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

    private String joinNames(List<String> names) {
        StringBuilder result = new StringBuilder();
        for (String name : names) {
            if (result.length() > 0) result.append(", ");
            result.append(name);
        }
        return result.toString();
    }

    private void message(String title, String body) {
        new AlertDialog.Builder(this).setTitle(title).setMessage(body)
                .setPositiveButton("Entendi", null).show();
    }

    private void setScreen(LinearLayout ignored) {
        // screen() já instala o ScrollView que contém o layout recebido.
    }

    @Override
    public void onBackPressed() {
        showHome();
    }

    @Override
    protected void onDestroy() {
        if (database != null) database.close();
        super.onDestroy();
    }
}