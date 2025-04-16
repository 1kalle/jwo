package com.example.jwo.views;

import com.example.jwo.security.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

@Route(value = "", layout = MainLayout.class)
@AnonymousAllowed
public class HomeView extends VerticalLayout {

    public HomeView(SecurityService securityService) {
        
        addClassName("welcome-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 welcomeText = new H1("Tervetuloa");
        Button loginButton = new Button("Kirjaudu sisään", e -> {getUI().ifPresent(ui -> ui.navigate("login"));});
        Button registerButton = new Button("Rekisteröidy", e -> getUI().ifPresent(ui -> ui.navigate("register")));

        add(welcomeText, loginButton, registerButton);

    }
}
