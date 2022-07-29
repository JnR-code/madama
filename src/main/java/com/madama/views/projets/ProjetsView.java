package com.madama.views.projets;

import com.madama.data.entity.Project;
import com.madama.data.service.ProjectService;
import com.madama.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
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

@PageTitle("Projets")
@Route(value = "projects/:projectID?/:action?(edit)", layout = MainLayout.class)
@PermitAll
public class ProjetsView extends Div implements BeforeEnterObserver {

    private final String PROJECT_ID = "projectID";
    private final String PROJECT_EDIT_ROUTE_TEMPLATE = "projects/%s/edit";

    private Grid<Project> grid = new Grid<>(Project.class, false);

    private TextField name;
    private TextField version;
    private TextField phase;
    private TextField methodo;
    private TextField client;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Project> binder;

    private Project project;

    private final ProjectService projectService;

    @Autowired
    public ProjetsView(ProjectService projectService) {
        this.projectService = projectService;
        addClassNames("projets-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        grid.addColumn("name").setAutoWidth(true);
        grid.addColumn("version").setAutoWidth(true);
        grid.addColumn("phase").setAutoWidth(true);
        grid.addColumn("methodo").setAutoWidth(true);
        grid.addColumn("client").setAutoWidth(true);
        grid.setItems(query -> projectService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(PROJECT_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(ProjetsView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Project.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.project == null) {
                    this.project = new Project();
                }
                binder.writeBean(this.project);

                projectService.update(this.project);
                clearForm();
                refreshGrid();
                Notification.show("Project details stored.");
                UI.getCurrent().navigate(ProjetsView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the project details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> projectId = event.getRouteParameters().get(PROJECT_ID).map(UUID::fromString);
        if (projectId.isPresent()) {
            Optional<Project> projectFromBackend = projectService.get(projectId.get());
            if (projectFromBackend.isPresent()) {
                populateForm(projectFromBackend.get());
            } else {
                Notification.show(String.format("The requested project was not found, ID = %s", projectId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(ProjetsView.class);
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
        phase = new TextField("Phase");
        methodo = new TextField("Methodo");
        client = new TextField("Client");
        Component[] fields = new Component[]{name, version, phase, methodo, client};

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

    private void populateForm(Project value) {
        this.project = value;
        binder.readBean(this.project);

    }
}
