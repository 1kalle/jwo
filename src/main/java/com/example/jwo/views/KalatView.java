package com.example.jwo.views;

import com.example.jwo.views.MainLayout;
import com.example.jwo.entity.Kala;
import com.example.jwo.repository.KalaRepository;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.H1;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.value.ValueChangeMode;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.server.auth.AnonymousAllowed;

import java.util.List;

@Route(value = "kalat", layout = MainLayout.class)
@PageTitle("Kalalista")
@AnonymousAllowed
public class KalatView extends VerticalLayout {

    public KalatView(KalaRepository kalaRepository) {
        addClassName("kalat-view");
        setSizeFull();
        setAlignItems(Alignment.CENTER);

        H1 title = new H1("Kaikki kalat");

        // Filter fields
        TextField lajiFilter = new TextField("Suodata lajilla");
        lajiFilter.setClearButtonVisible(true);
        lajiFilter.setValueChangeMode(ValueChangeMode.LAZY);

        NumberField minPituusFilter = new NumberField("Min pituus (cm)");
        minPituusFilter.setMin(0);
        minPituusFilter.setClearButtonVisible(true);
        minPituusFilter.setValueChangeMode(ValueChangeMode.LAZY);

        NumberField maxPituusFilter = new NumberField("Max pituus (cm)");
        maxPituusFilter.setMin(0);
        maxPituusFilter.setClearButtonVisible(true);
        maxPituusFilter.setValueChangeMode(ValueChangeMode.LAZY);

        NumberField minPainoFilter = new NumberField("Min paino (kg)");
        minPainoFilter.setMin(0);
        minPainoFilter.setClearButtonVisible(true);
        minPainoFilter.setValueChangeMode(ValueChangeMode.LAZY);

        NumberField maxPainoFilter = new NumberField("Max paino (kg)");
        maxPainoFilter.setMin(0);
        maxPainoFilter.setClearButtonVisible(true);
        maxPainoFilter.setValueChangeMode(ValueChangeMode.LAZY);

        TextField userFilter = new TextField("Suodata käyttäjällä");
        userFilter.setClearButtonVisible(true);
        userFilter.setValueChangeMode(ValueChangeMode.LAZY);

        // Arrange filters in a layout
        HorizontalLayout filterLayout = new HorizontalLayout(
                lajiFilter, minPituusFilter, maxPituusFilter,
                minPainoFilter, maxPainoFilter, userFilter
        );
        filterLayout.setWidthFull();
        filterLayout.setAlignItems(Alignment.BASELINE);

        // Grid setup
        Grid<Kala> grid = new Grid<>(Kala.class, false);
        grid.addColumn(Kala::getLaji).setHeader("Laji").setSortable(true);
        grid.addColumn(Kala::getPituus).setHeader("Pituus (cm)").setSortable(true);
        grid.addColumn(Kala::getPaino).setHeader("Paino (kg)").setSortable(true);
        grid.addColumn(kala -> kala.getUser() != null ? kala.getUser().getUsername() : "Tuntematon")
                .setHeader("Käyttäjä").setSortable(true);

        // Data provider for filtering
        List<Kala> kalat = kalaRepository.findAll();
        ListDataProvider<Kala> dataProvider = new ListDataProvider<>(kalat);
        grid.setItems(dataProvider);

        // Apply filters
        lajiFilter.addValueChangeListener(e -> applyFilters(
                dataProvider, lajiFilter.getValue(), minPituusFilter.getValue(), maxPituusFilter.getValue(),
                minPainoFilter.getValue(), maxPainoFilter.getValue(), userFilter.getValue()
        ));
        minPituusFilter.addValueChangeListener(e -> applyFilters(
                dataProvider, lajiFilter.getValue(), minPituusFilter.getValue(), maxPituusFilter.getValue(),
                minPainoFilter.getValue(), maxPainoFilter.getValue(), userFilter.getValue()
        ));
        maxPituusFilter.addValueChangeListener(e -> applyFilters(
                dataProvider, lajiFilter.getValue(), minPituusFilter.getValue(), maxPituusFilter.getValue(),
                minPainoFilter.getValue(), maxPainoFilter.getValue(), userFilter.getValue()
        ));
        minPainoFilter.addValueChangeListener(e -> applyFilters(
                dataProvider, lajiFilter.getValue(), minPituusFilter.getValue(), maxPituusFilter.getValue(),
                minPainoFilter.getValue(), maxPainoFilter.getValue(), userFilter.getValue()
        ));
        maxPainoFilter.addValueChangeListener(e -> applyFilters(
                dataProvider, lajiFilter.getValue(), minPituusFilter.getValue(), maxPituusFilter.getValue(),
                minPainoFilter.getValue(), maxPainoFilter.getValue(), userFilter.getValue()
        ));
        userFilter.addValueChangeListener(e -> applyFilters(
                dataProvider, lajiFilter.getValue(), minPituusFilter.getValue(), maxPituusFilter.getValue(),
                minPainoFilter.getValue(), maxPainoFilter.getValue(), userFilter.getValue()
        ));

        add(title, filterLayout, grid);
    }

    private void applyFilters(ListDataProvider<Kala> dataProvider, String lajiFilter,
                              Double minPituus, Double maxPituus, Double minPaino, Double maxPaino, String userFilter) {
        dataProvider.setFilter(kala -> {
            boolean matchesLaji = lajiFilter == null || lajiFilter.trim().isEmpty() ||
                    kala.getLaji().toLowerCase().contains(lajiFilter.toLowerCase());
            boolean matchesMinPituus = minPituus == null || kala.getPituus() >= minPituus;
            boolean matchesMaxPituus = maxPituus == null || kala.getPituus() <= maxPituus;
            boolean matchesMinPaino = minPaino == null || kala.getPaino() >= minPaino;
            boolean matchesMaxPaino = maxPaino == null || kala.getPaino() <= maxPaino;
            boolean matchesUser = userFilter == null || userFilter.trim().isEmpty() ||
                    (kala.getUser() != null && kala.getUser().getUsername().toLowerCase().contains(userFilter.toLowerCase()));

            return matchesLaji && matchesMinPituus && matchesMaxPituus &&
                    matchesMinPaino && matchesMaxPaino && matchesUser;
        });
    }
}