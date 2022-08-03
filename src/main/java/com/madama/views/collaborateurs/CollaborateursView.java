package com.madama.views.collaborateurs;

import com.madama.data.entity.Mate;
import com.madama.data.service.MateService;
import com.madama.views.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.checkbox.Checkbox;
import com.vaadin.flow.component.datepicker.DatePicker;
import com.vaadin.flow.component.dependency.Uses;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Image;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.Icon;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.splitlayout.SplitLayout;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.data.binder.BeanValidationBinder;
import com.vaadin.flow.data.binder.ValidationException;
import com.vaadin.flow.data.renderer.LitRenderer;
import com.vaadin.flow.router.BeforeEnterEvent;
import com.vaadin.flow.router.BeforeEnterObserver;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.spring.data.VaadinSpringDataHelpers;
import elemental.json.Json;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.util.UriUtils;

import javax.annotation.security.PermitAll;

@PageTitle("Collaborateurs")
@Route(value = "mates/:mateID?/:action?(edit)", layout = MainLayout.class)
@Uses(Icon.class)
@PermitAll
public class CollaborateursView extends Div implements BeforeEnterObserver {

    private final String MATE_ID = "mateID";
    private final String MATE_EDIT_ROUTE_TEMPLATE = "mates/%s/edit";

    private Grid<Mate> grid = new Grid<>(Mate.class, false);

    private TextField firstName;
    private TextField lastName;
    private Upload avatar;
    private Image avatarPreview;
    private Checkbox isActif;
    private TextField poste;
    private DatePicker dateDebut;

    private Button cancel = new Button("Cancel");
    private Button save = new Button("Save");

    private BeanValidationBinder<Mate> binder;

    private Mate mate;

    private final MateService mateService;

    @Autowired
    public CollaborateursView(MateService mateService) {
        this.mateService = mateService;
        addClassNames("collaborateurs-view");

        // Create UI
        SplitLayout splitLayout = new SplitLayout();

        createGridLayout(splitLayout);
        createEditorLayout(splitLayout);

        add(splitLayout);

        // Configure Grid
        LitRenderer<Mate> avatarRenderer = LitRenderer.<Mate>of(
                        "<span style='border-radius: 50%; overflow: hidden; display: flex; align-items: center; justify-content: center; width: 64px; height: 64px'><img style='max-width: 100%' src=${item.avatar} /></span>")
                .withProperty("avatar", Mate::getAvatar);
        grid.addColumn(avatarRenderer).setHeader("Avatar").setWidth("96px").setFlexGrow(0);
        grid.addColumn("firstName").setAutoWidth(true);
        grid.addColumn("lastName").setAutoWidth(true);



        LitRenderer<Mate> isActifRenderer = LitRenderer.<Mate>of(
                "<vaadin-icon icon='vaadin:${item.icon}' style='width: var(--lumo-icon-size-s); height: var(--lumo-icon-size-s); color: ${item.color};'></vaadin-icon>")
                .withProperty("icon", isActif -> isActif.isIsActif() ? "check" : "minus").withProperty("color",
                        isActif -> isActif.isIsActif()
                                ? "var(--lumo-primary-text-color)"
                                : "var(--lumo-disabled-text-color)");

        grid.addColumn(isActifRenderer).setHeader("Is Actif").setAutoWidth(true);

        grid.addColumn("poste").setAutoWidth(true);
        grid.addColumn("dateDebut").setAutoWidth(true);
        grid.setItems(query -> mateService.list(
                PageRequest.of(query.getPage(), query.getPageSize(), VaadinSpringDataHelpers.toSpringDataSort(query)))
                .stream());
        grid.addThemeVariants(GridVariant.LUMO_NO_BORDER);

        // when a row is selected or deselected, populate form
        grid.asSingleSelect().addValueChangeListener(event -> {
            if (event.getValue() != null) {
                UI.getCurrent().navigate(String.format(MATE_EDIT_ROUTE_TEMPLATE, event.getValue().getId()));
            } else {
                clearForm();
                UI.getCurrent().navigate(CollaborateursView.class);
            }
        });

        // Configure Form
        binder = new BeanValidationBinder<>(Mate.class);

        // Bind fields. This is where you'd define e.g. validation rules

        binder.bindInstanceFields(this);

        attachImageUpload(avatar, avatarPreview);

        cancel.addClickListener(e -> {
            clearForm();
            refreshGrid();
        });

        save.addClickListener(e -> {
            try {
                if (this.mate == null) {
                    this.mate = new Mate();
                }
                binder.writeBean(this.mate);
                this.mate.setAvatar(avatarPreview.getSrc());

                mateService.update(this.mate);
                clearForm();
                refreshGrid();
                Notification.show("Mate details stored.");
                UI.getCurrent().navigate(CollaborateursView.class);
            } catch (ValidationException validationException) {
                Notification.show("An exception happened while trying to store the mate details.");
            }
        });

    }

    @Override
    public void beforeEnter(BeforeEnterEvent event) {
        Optional<UUID> mateId = event.getRouteParameters().get(MATE_ID).map(UUID::fromString);
        if (mateId.isPresent()) {
            Optional<Mate> mateFromBackend = mateService.get(mateId.get());
            if (mateFromBackend.isPresent()) {
                populateForm(mateFromBackend.get());
            } else {
                Notification.show(String.format("The requested mate was not found, ID = %s", mateId.get()), 3000,
                        Notification.Position.BOTTOM_START);
                // when a row is selected but the data is no longer available,
                // refresh grid
                refreshGrid();
                event.forwardTo(CollaborateursView.class);
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
        avatarPreview = new Image();
        avatarPreview.setWidth("100%");
        avatar = new Upload();
        avatar.getStyle().set("box-sizing", "border-box");
        avatar.getElement().appendChild(avatarPreview.getElement());
        firstName = new TextField("First Name");
        lastName = new TextField("Last Name");
        Label avatarLabel = new Label("Avatar");

        isActif = new Checkbox("Is Actif");
        poste = new TextField("Poste");
        dateDebut = new DatePicker("Date Debut");
        Component[] fields = new Component[]{firstName, lastName, avatarLabel, avatar, isActif, poste, dateDebut};

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

    private void attachImageUpload(Upload upload, Image preview) {
        ByteArrayOutputStream uploadBuffer = new ByteArrayOutputStream();
        upload.setAcceptedFileTypes("image/*");
        upload.setReceiver((fileName, mimeType) -> {
            return uploadBuffer;
        });
        upload.addSucceededListener(e -> {
            String mimeType = e.getMIMEType();
            String base64ImageData = Base64.getEncoder().encodeToString(uploadBuffer.toByteArray());
            String dataUrl = "data:" + mimeType + ";base64,"
                    + UriUtils.encodeQuery(base64ImageData, StandardCharsets.UTF_8);
            upload.getElement().setPropertyJson("files", Json.createArray());
            preview.setSrc(dataUrl);
            uploadBuffer.reset();
        });
        preview.setVisible(false);
    }

    private void refreshGrid() {
        grid.select(null);
        grid.getLazyDataView().refreshAll();
    }

    private void clearForm() {
        populateForm(null);
    }

    private void populateForm(Mate value) {
        this.mate = value;
        binder.readBean(this.mate);
        this.avatarPreview.setVisible(value != null);
        if (value == null) {
            this.avatarPreview.setSrc("");
        } else {
            this.avatarPreview.setSrc(value.getAvatar());
        }

    }
}
