package com.madama.views.technologies;

import com.madama.data.entity.Technologie;
import com.madama.data.service.TechnologieService;
import com.madama.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.security.PermitAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

@PageTitle("Technologies")
@Route(value = "technologies/:technologieID?/:action?(edit)", layout = MainLayout.class)
@PermitAll
@Uses(Icon.class)
public class TechnologiesView extends Div implements BeforeEnterObserver {

    private final String TECHNOLOGIE_ID = "technologieID";
    private final String TECHNOLOGIE_EDIT_ROUTE_TEMPLATE = "technologies/%s/edit";

    private Grid<Technologie> grid = new Grid<>(Technologie.class, false);

    private TextField name;
    private TextField version;
    private Checkbox isLts;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Technologie> binder;

    private Technologie technologie;

    private final TechnologieService technologieService;

    @Autowired
    public TechnologiesView(TechnologieService technologieService) {
        this.technologieService = technologieService;
        addClassNames("technologies-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("version").setAutoWidth(true);
        LitRenderer<Technologie> isLtsRenderer = LitRenderer.<Technologie>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", isLts -> isLts.isIsLts() ? "check" : "minus").withProperty("color",
                        isLts -> isLts.isIsLts()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(isLtsRenderer).setHeader("Is Lts").setAutoWidth(true);

        grid.setItems(query -> technologieService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(TECHNOLOGIE_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(TechnologiesView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Technologie.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.technologie == null) {
                    this.technologie = new Technologie();
                }
                binder.writeBean(this.technologie);

                technologieService.update(this.technologie);
                clearForm();
                refreshGrid();
                Notification.show("Technologie details stored.");
                UI.getCurrent().navigate(TechnologiesView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the technologie details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> technologieId = event.getRouteParameters().get(TECHNOLOGIE_ID).map(UUID::fromString);
        if (technologieId.isPresent()) {
            Optional<Technologie> technologieFromBackend = technologieService.get(technologieId.get());
            if (technologieFromBackend.isPresent()) {
                populateForm(technologieFromBackend.get());
            } else {
                Notification.show(
                        String.format("The requested technologie was not found, ID = %s", technologieId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(TechnologiesView.class);
            }
        }
    }

    private void createEditorLayout(SplitLayout splitLayout) {
        Div editorLayoutDiv = new Div();
        editorLayoutDiv.setClassName("editor-layout");

        Div editorDiv = new Div();
        editorDiv.setClassName("editor");
        editorLayoutDiv.add(editorDiv);

        FormLayout formLayout = new FormLayout();
        name = new TextField("Name");
        version = new TextField("Version");
        isLts = new Checkbox("Is Lts");
        Component[] fields = new Component[]{name, version, isLts};

        formLayout.add(fields);
        editorDiv.add(formLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel);
        editorLayoutDiv.add(buttonLayout);
    }

    private void createGridLayout(SplitLayout splitLayout) {
        Div wrapper = new Div();
        wrapper.setClassName("grid-wrapper");
        splitLayout.addToPrimary(wrapper);
        wrapper.add(grid);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Technologie value) {
        this.technologie = value;
        binder.readBean(this.technologie);

    }
}
