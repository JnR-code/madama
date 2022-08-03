package com.madama.views.projets;

import com.madama.data.entity.Project;
import com.madama.data.entity.Technologie;
import com.madama.data.service.ProjectService;
import com.madama.data.service.TechnologieRepository;
import com.madama.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.icon.VaadinIcon;
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

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
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

    Button delete = new Button("Delete");
    Button update = new Button("Update");

    private BeanValidationBinder<Project> binder;

    private Project project;

    private FormLayout techsLayout;
    private List<TextField> listTechs = new ArrayList<>();
    private List<Button> listButton = new ArrayList<>();

    private final ProjectService projectService;
    private final TechnologieRepository technologieRepository;

    @Autowired
    public ProjetsView(ProjectService projectService, TechnologieRepository technologieRepository) {
        this.projectService = projectService;
        this.technologieRepository = technologieRepository;
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

        save.setVisible(false);
        delete.setVisible(true);
        cancel.setVisible(false);
        update.setVisible(true);
        update.addClickListener(event -> setReadOnly(false));
        delete.addClickListener(event -> projectService.delete(this.project.getId()));

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
            setReadOnly(true);
        });

        save.addClickListener(e -> {
            try {
                if (this.project == null) {
                    this.project = new Project();
                }
                setReadOnly(true);
                binder.writeBean(this.project);

                projectService.update(this.project);
                saveTechs();
                technologieRepository.saveAll(this.project.getTechnologies());
                Notification.show("Project details stored. "+this.project.getTechnologies().toString());
                clearForm();
                refreshGrid();
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

        techsLayout = new FormLayout();
        techsLayout.setClassName("techsLayout");
        FormLayout formLayout = new FormLayout();
        name = new TextField("Name");
        version = new TextField("Version");
        phase = new TextField("Phase");
        methodo = new TextField("Methodo");
        client = new TextField("Client");

        Component[] fields = new Component[]{name, version, phase, methodo, client};

        formLayout.add(fields);

        editorDiv.add(formLayout);
        editorDiv.add(techsLayout);
        createButtonLayout(editorLayoutDiv);

        splitLayout.addToSecondary(editorLayoutDiv);
    }

    private void createButtonLayout(Div editorLayoutDiv) {
        HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setClassName("button-layout");
        cancel.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        save.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        update.addThemeVariants(ButtonVariant.LUMO_TERTIARY);
        delete.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        buttonLayout.add(save, cancel, update, delete);
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
        clearTechLayout();
    }

    private void populateForm(Project value) {
        this.project = value;
        binder.readBean(this.project);

        clearTechLayout();
        if (this.project != null){
            for (Technologie technologie: this.project.getTechnologies()){
                TextField t1 = new TextField();
                t1.setValue(technologie.getName());
                techsLayout.add(t1);
                listTechs.add(t1);
            }
        }
    }

    private void clearTechLayout(){
        techsLayout.removeAll();
        Label label = new Label("Technologies");
        techsLayout.add(label);
        Button plusButton = new Button(new Icon(VaadinIcon.PLUS));
        plusButton.addClickListener(clickEvent -> {
            addNewTechChoice();
        });
        plusButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        plusButton.getElement().setAttribute("aria-label", "Add item");
        listButton.add(plusButton);
        techsLayout.add(plusButton);
    }

    private void addNewTechChoice(){
        HorizontalLayout layout = new HorizontalLayout();
        TextField t1 = new TextField();
        layout.add(t1);
        Button minusButton = new Button(new Icon(VaadinIcon.MINUS));
        minusButton.addClickListener(clickEvent -> {
            removeTechChoice(layout, t1);
        });
        minusButton.addThemeVariants(ButtonVariant.LUMO_ICON);
        minusButton.getElement().setAttribute("aria-label", "Remove item");
        layout.add(minusButton);
        listTechs.add(t1);
        listButton.add(minusButton);
        techsLayout.add(layout);

    }

    private void removeTechChoice(HorizontalLayout layout, TextField t1){
        listTechs.remove(t1);
        techsLayout.remove(layout);
    }

    private void saveTechs(){
        List<String> techsName = listTechs.stream().map(t -> t.getValue()).collect(Collectors.toList());
    }

    private void setReadOnly(Boolean isReadOnly){
        update.setVisible(isReadOnly);
        save.setVisible(!isReadOnly);
        cancel.setVisible(!isReadOnly);
        delete.setVisible(isReadOnly);
        setFieldReadOnly(isReadOnly);

    }

    private void setFieldReadOnly(Boolean isReadOnly){
        name.setReadOnly(isReadOnly);
        version.setReadOnly(isReadOnly);
        phase.setReadOnly(isReadOnly);
        methodo.setReadOnly(isReadOnly);
        client.setReadOnly(isReadOnly);
        listButton.stream().forEach(b -> b.setVisible(!isReadOnly));
    }
}
