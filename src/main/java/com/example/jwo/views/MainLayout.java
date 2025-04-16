package com.example.jwo.views;

import com.example.jwo.security.SecurityService;
import com.vaadin.flow.component.applayout.AppLayout;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.CssImport;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import org.springframework.security.core.userdetails.UserDetails;

@CssImport("./styles/main-layout.css")
public class MainLayout extends AppLayout {

    public MainLayout(SecurityService securityService) {
        // Create the navbar
        HorizontalLayout navbar = new HorizontalLayout();
        navbar.setWidthFull();
        navbar.setPadding(true);
        navbar.setSpacing(true);
        navbar.setAlignItems(FlexComponent.Alignment.CENTER);
        navbar.getStyle().set("background-color", "#f8f9fa")
                .set("border-bottom", "1px solid #dee2e6")
                .set("box-shadow", "0 2px 4px rgba(0, 0, 0, 0.1)");

        // Application title
        H1 title = new H1("Kalat");
        title.getStyle().set("font-size", "20px").set("margin", "0");

        // Navigation buttons
        Button homeButton = new Button("Koti", e -> getUI().ifPresent(ui -> ui.navigate("")));
        Button kalatButton = new Button("Kalat", e -> getUI().ifPresent(ui -> ui.navigate("kalat")));
        Button addButton = new Button("Lisää", e -> getUI().ifPresent(ui -> ui.navigate("add")));


        // Authentication buttons
        UserDetails user = securityService.getAuthenticatedUser();

        Button adminButton = null;
        if (user != null && user.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            adminButton = new Button("Admin", e -> getUI().ifPresent(ui -> ui.navigate("admin")));
        }

        Button authButton = user == null
                ? new Button("Kirjaudu / Rekisteröidy", e -> getUI().ifPresent(ui -> ui.navigate("login")))
                : new Button("Kirjaudu ulos (" + user.getUsername() + ")", e -> securityService.logout());



        // Add components to the navbar
        navbar.add(title, homeButton,kalatButton, addButton);
        if (adminButton != null) {
            navbar.add(adminButton);
        }
        navbar.add(authButton);

        // Add navbar to the AppLayout
        addToNavbar(true, navbar); // true ensures navbar spans full width
    }
}