/*
 * Copyright (C) 2015 PÂRIS Quentin
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 */

package com.playonlinux.ui.impl.javafx.mainwindow.apps;

import com.playonlinux.app.PlayOnLinuxException;
import com.playonlinux.dto.ui.apps.AppsCategoryDTO;
import com.playonlinux.dto.ui.apps.AppsItemDTO;
import com.playonlinux.dto.ui.apps.AppsWindowDTO;
import com.playonlinux.apps.AppsFilter;
import com.playonlinux.ui.api.EntitiesProvider;
import com.playonlinux.ui.impl.javafx.mainwindow.*;
import com.playonlinux.ui.impl.javafx.widget.MiniatureListWidget;
import com.playonlinux.core.observer.Observable;
import com.playonlinux.core.observer.Observer;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import org.apache.log4j.Logger;

import static com.playonlinux.core.lang.Localisation.translate;

final public class ViewApps extends MainWindowView implements Observer<Observable, AppsWindowDTO> {
    private static final Logger LOGGER = Logger.getLogger(ViewApps.class);

    private FailurePanel failurePanel;
    private HBox waitPanel;

    private final EntitiesProvider<AppsItemDTO, AppsWindowDTO> windowDTOEntitiesProvider;
    private final MiniatureListWidget availableInstallerListWidget;

    private final EventHandlerApps eventHandlerApps;

    private TextField searchBar;
    private CategoryView categoryView;

    private CheckBox testingCheck;
    private CheckBox noCdNeededCheck;
    private CheckBox commercialCheck;
    private AppsCategoryDTO selectedCategory;

    public ViewApps(MainWindow parent) {
        super(parent);
        eventHandlerApps = new EventHandlerApps();

        availableInstallerListWidget = MiniatureListWidget.create();
        windowDTOEntitiesProvider = eventHandlerApps.getRemoteAvailableInstallers();

        this.initWait();
        this.initFailure();

        this.drawSideBar();
        this.showWait();
    }

    private void initFailure() {
        failurePanel = new FailurePanel();
    }

    private void initWait() {
        waitPanel = new WaitPanel();
    }

    protected void drawSideBar() {
        searchBar = new TextField();
        searchBar.setOnKeyReleased((e) -> applyFilter(""));

        categoryView = new CategoryView(this);

        testingCheck = new CheckBox(translate("Testing"));
        noCdNeededCheck = new CheckBox(translate("No CD needed"));
        commercialCheck = new CheckBox(translate("Commercial"));

        testingCheck.setOnMouseClicked((e) -> applyFilterOnSelectedCategory());
        noCdNeededCheck.setOnMouseClicked((e) -> applyFilterOnSelectedCategory());
        commercialCheck.setOnMouseClicked((e) -> applyFilterOnSelectedCategory());

        addToSideBar(searchBar, new LeftSpacer(), categoryView, new LeftSpacer(),
                new LeftBarTitle("Filters"),
                testingCheck, noCdNeededCheck, commercialCheck);

        super.drawSideBar();
    }

    private void showAvailableApps() {
        showRightView(availableInstallerListWidget);
    }

    private void showWait() {
        showRightView(waitPanel);
    }

    private void showFailure() {
        showRightView(failurePanel);
    }

    private void showAppDetails(AppsItemDTO item) {
        showRightView(new AppPanel(eventHandlerApps, item));
    }



    public void setUpEvents() {
        windowDTOEntitiesProvider.addObserver(this);
        failurePanel.getRetryButton().setOnMouseClicked(event -> {
            try {
                this.eventHandlerApps.updateAvailableInstallers();
            } catch (PlayOnLinuxException e) {
                LOGGER.warn(e);
            }
        });
    }

    @Override
    public void update(Observable o, AppsWindowDTO appsWindowDTO) {

        Platform.runLater(() -> {
            availableInstallerListWidget.clear();

            if (appsWindowDTO.isDownloading()) {
                this.showWait();
            } else if (appsWindowDTO.isDownloadFailed()) {
                this.showFailure();
            } else {
                this.showAvailableApps();

                categoryView.setCategories(appsWindowDTO.getCategoryDTOs());

                for (AppsItemDTO appsItemDTO : appsWindowDTO.getAppsItemDTOs()) {
                    Node itemNode = availableInstallerListWidget.addItem(appsItemDTO.getName());
                    itemNode.setOnMouseClicked((evt) -> showAppDetails(appsItemDTO));
                }
            }
        });
    }


    public void selectCategory(AppsCategoryDTO category) {
        this.selectedCategory = category;
        searchBar.setText("");
        applyFilter(category.getName());
    }

    public void applyFilterOnSelectedCategory() {
        if(selectedCategory != null) {
            applyFilter(selectedCategory.getName());
        }
    }

    private void applyFilter(String categoryName) {
        windowDTOEntitiesProvider.applyFilter(
                new AppsFilter(
                        categoryName,
                        searchBar.getText(),
                        testingCheck.isSelected(),
                        noCdNeededCheck.isSelected(),
                        commercialCheck.isSelected()
                )
        );

    }

}