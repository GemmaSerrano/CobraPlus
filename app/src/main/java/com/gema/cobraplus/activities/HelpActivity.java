package com.gema.cobraplus.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.gema.cobraplus.R;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;

public class HelpActivity extends AppCompatActivity {

    private TextView txtToggleHowTo, txtToggleFAQ;
    private LinearLayout llHowToContent, llFAQContent;
    private MaterialButton btnBackHelp;

    //Guarda si los bloques principales están abiertos o cerrados
    private boolean isHowToOpen = false;
    private boolean isFAQOpen = false;

    //para mantener los títulos abiertos en las listas
    private ArrayList<String> openHelpSections = new ArrayList<>();
    private ArrayList<String> openFAQSections = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.help_activity);

        initViews();

        //Restaurar las listas
        if (savedInstanceState != null) {
            isHowToOpen = savedInstanceState.getBoolean("HOW_TO_OPEN", false);
            isFAQOpen = savedInstanceState.getBoolean("FAQ_OPEN", false);

            ArrayList<String> savedHelpSections =
                    savedInstanceState.getStringArrayList("OPEN_HELP_SECTIONS");

            ArrayList<String> savedFAQSections =
                    savedInstanceState.getStringArrayList("OPEN_FAQ_SECTIONS");

            if (savedHelpSections != null) openHelpSections = savedHelpSections;
            if (savedFAQSections != null) openFAQSections = savedFAQSections;
        }

        loadHowToContent();
        loadFAQContent();
        setupListeners();

        llHowToContent.setVisibility(isHowToOpen ? View.VISIBLE : View.GONE);
        llFAQContent.setVisibility(isFAQOpen ? View.VISIBLE : View.GONE);
    }

    //Inicializa los elementos de la pantalla
    private void initViews() {
        txtToggleHowTo = findViewById(R.id.txtToggleHowTo);
        txtToggleFAQ = findViewById(R.id.txtToggleFAQ);
        llHowToContent = findViewById(R.id.llHowToContent);
        llFAQContent = findViewById(R.id.llFAQContent);
        btnBackHelp = findViewById(R.id.btnBackHelp);
    }

    //Asocia acciones a los botones principales
    private void setupListeners() {
        txtToggleHowTo.setOnClickListener(v -> toggleHowTo());
        txtToggleFAQ.setOnClickListener(v -> toggleFAQ());
        btnBackHelp.setOnClickListener(v -> finish());
    }

    //Abre o cierra el bloque "Cómo usar la aplicación"
    private void toggleHowTo() {
        isHowToOpen = !isHowToOpen;
        llHowToContent.setVisibility(isHowToOpen ? View.VISIBLE : View.GONE);
    }

    //Abre o cierra el bloque "Preguntas frecuentes"
    private void toggleFAQ() {
        isFAQOpen = !isFAQOpen;
        llFAQContent.setVisibility(isFAQOpen ? View.VISIBLE : View.GONE);
    }


    //Guarda el estado antes de recrear la pantalla, por ejemplo al girar el dispositivo
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean("HOW_TO_OPEN", isHowToOpen);
        outState.putBoolean("FAQ_OPEN", isFAQOpen);
        outState.putStringArrayList("OPEN_HELP_SECTIONS", openHelpSections);
        outState.putStringArrayList("OPEN_FAQ_SECTIONS", openFAQSections);
    }

    //Carga los apartados de ayuda de uso de la aplicación
    private void loadHowToContent() {
        addHelpSection(
                "Pantalla principal",
                "Desde la pantalla principal se accede a las funciones principales de la aplicación: registrar actividad, gestionar conceptos, consultar informes, acceder al perfil, ayuda y cerrar sesión."
        );

        addHelpSection(
                "Gestión de conceptos",
                "Permite consultar los conceptos creados. El usuario puede crear conceptos personalizados a partir de conceptos base y eliminarlos si es necesario."
        );

        addHelpSection(
                "Registrar actividad",
                "Permite registrar una actividad laboral seleccionando fecha, empresa, concepto y cantidad. Los comentarios son opcionales."
        );

        addHelpSection(
                "Gestión de informes",
                "Permite consultar informes generados, crear nuevos informes, visualizarlos, exportarlos en PDF o Excel y eliminarlos."
        );

        addHelpSection(
                "Perfil",
                "Permite consultar y modificar los datos del usuario. Si el acceso se ha realizado con Google, solo se permite modificar el nombre."
        );
    }

    //Carga las preguntas frecuentes
    private void loadFAQContent() {
        addFAQ(
                "¿Puedo tener varios trabajos?",
                "Sí. Puedes registrar conceptos asociados a distintas empresas."
        );

        addFAQ(
                "¿Puedo exportar informes?",
                "Sí. Puedes exportarlos en formato PDF o Excel desde el menú de opciones o desde la pantalla de visualización del informe."
        );

        addFAQ(
                "¿Puedo modificar un registro antes de crear un informe?",
                "Sí. Antes de aceptar un informe, se muestra una pantalla de confirmación donde puedes revisar y modificar los registros incluidos."
        );

        addFAQ(
                "¿Dónde se guardan los informes exportados?",
                "Se guardan en la ubicación seleccionada por el usuario en el dispositivo."
        );

        addFAQ(
                "¿Puedo eliminar informes?",
                "Sí. Los informes pueden eliminarse desde el menú de opciones o desde la pantalla de visualización, siempre con confirmación previa."
        );
    }

    //Añade un apartado de ayuda con título y descripción desplegable
    private void addHelpSection(String title, String description) {
        TextView titleView = createTitleText(title);
        TextView descriptionView = createDescriptionText(description);

        descriptionView.setVisibility(openHelpSections.contains(title) ? View.VISIBLE : View.GONE);

        titleView.setOnClickListener(v -> {
            if (descriptionView.getVisibility() == View.VISIBLE) {
                descriptionView.setVisibility(View.GONE);
                openHelpSections.remove(title);
            } else {
                descriptionView.setVisibility(View.VISIBLE);
                if (!openHelpSections.contains(title)) {
                    openHelpSections.add(title);
                }
            }
        });

        llHowToContent.addView(titleView);
        llHowToContent.addView(descriptionView);
    }

    //Añade una pregunta frecuente con respuesta desplegable
    private void addFAQ(String question, String answer) {
        TextView questionView = createTitleText(question);
        TextView answerView = createDescriptionText(answer);

        answerView.setVisibility(openFAQSections.contains(question) ? View.VISIBLE : View.GONE);

        questionView.setOnClickListener(v -> {
            if (answerView.getVisibility() == View.VISIBLE) {
                answerView.setVisibility(View.GONE);
                openFAQSections.remove(question);
            } else {
                answerView.setVisibility(View.VISIBLE);
                if (!openFAQSections.contains(question)) {
                    openFAQSections.add(question);
                }
            }
        });

        llFAQContent.addView(questionView);
        llFAQContent.addView(answerView);
    }

    //Crea el texto de título de cada apartado
    private TextView createTitleText(String text) {
        TextView textView = new TextView(this);
        textView.setText("• " + text);
        textView.setTextSize(16);
        textView.setTextColor(getColor(R.color.button_blue));
        textView.setPadding(0, 12, 0, 6);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        return textView;
    }

    //Crea el texto descriptivo de cada apartado
    private TextView createDescriptionText(String text) {
        TextView textView = new TextView(this);
        textView.setText(text);
        textView.setTextSize(14);
        textView.setTextColor(getColor(R.color.black));
        textView.setPadding(24, 0, 0, 12);
        textView.setTextAlignment(View.TEXT_ALIGNMENT_TEXT_START);
        return textView;
    }
}