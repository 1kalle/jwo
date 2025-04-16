package com.example.jwo.views;

import com.example.jwo.entity.Kala;
import com.example.jwo.entity.User;
import com.example.jwo.repository.KalaRepository;
import com.example.jwo.repository.UserRepository;
import com.example.jwo.security.SecurityService;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.html.Paragraph;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.RolesAllowed;

@Route(value = "add", layout = MainLayout.class)
@PageTitle("Lisää")
@RolesAllowed({"USER","ADMIN"})

public class AddView extends VerticalLayout {

    public AddView(SecurityService securityService, KalaRepository kalaRepository, UserRepository userRepository) {
        addClassName("add-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);
        setJustifyContentMode(JustifyContentMode.CENTER);

        H1 title = new H1("Lisää uusi kala");

        TextField lajiField = new TextField("Laji");
        NumberField pituusField = new NumberField("Pituus (cm)");
        pituusField.setMin(0);
        NumberField painoField = new NumberField("Paino (kg)");
        painoField.setMin(0);

        Button saveButton = new Button("Tallenna", e -> {
            if (lajiField.isEmpty() || pituusField.isEmpty() || painoField.isEmpty()) {
                Notification.show("Täytä kaikki kentät!", 3000, Notification.Position.MIDDLE);
                return;
            }

            // Get the authenticated user
            org.springframework.security.core.userdetails.UserDetails userDetails = securityService.getAuthenticatedUser();
            if (userDetails == null) {
                Notification.show("Kirjaudu sisään tallentaaksesi kalan!", 3000, Notification.Position.MIDDLE);
                return;
            }

            User user = userRepository.findByUsername(userDetails.getUsername());
            if (user == null) {
                Notification.show("Käyttäjää ei löydy!", 3000, Notification.Position.MIDDLE);
                return;
            }

            Kala kala = new Kala();
            kala.setLaji(lajiField.getValue());
            kala.setPituus(pituusField.getValue());
            kala.setPaino(painoField.getValue());
            kala.setUser(user);

            try {
                kalaRepository.save(kala);
                Notification.show("Kala tallennettu: " + kala.getLaji(), 3000, Notification.Position.MIDDLE);
                lajiField.clear();
                pituusField.clear();
                painoField.clear();
            } catch (Exception ex) {
                Notification.show("Tallennus epäonnistui!", 3000, Notification.Position.MIDDLE);
            }
        });

        FormLayout formLayout = new FormLayout();
        formLayout.add(lajiField, pituusField, painoField, saveButton);

        add(title, formLayout);
    }

}