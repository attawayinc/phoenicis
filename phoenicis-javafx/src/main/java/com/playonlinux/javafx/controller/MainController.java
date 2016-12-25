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

package com.playonlinux.javafx.controller;

import com.playonlinux.javafx.controller.apps.AppsController;
import com.playonlinux.javafx.controller.containers.ContainersController;
import com.playonlinux.javafx.controller.engines.EnginesController;
import com.playonlinux.javafx.controller.library.LibraryController;
import com.playonlinux.javafx.views.mainwindow.MainWindow;
import com.playonlinux.javafx.views.mainwindow.MainWindowHeader;
import com.playonlinux.javafx.views.mainwindow.containers.ViewContainers;
import com.playonlinux.javafx.views.mainwindow.engines.ViewEngines;
import com.playonlinux.javafx.views.mainwindow.library.ViewLibrary;
import com.playonlinux.javafx.views.mainwindow.settings.ViewSettings;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import org.springframework.beans.factory.annotation.Value;

import java.util.Optional;

import static com.playonlinux.configuration.localisation.Localisation.translate;

public class MainController {
    private final MainWindow mainWindow;

    @Value("${application.name}")
    private String applicationName;

    private Object onClose;

    public MainController(LibraryController libraryController,
                          AppsController appsController,
                          EnginesController enginesController,
                          ContainersController containersController,
                          ViewSettings viewSettings,
                          MainWindowHeader mainWindowHeader) {
        super();

        this.mainWindow = new MainWindow(
                applicationName,
                libraryController.getView(),
                appsController.getView(),
                enginesController.getView(),
                containersController.getView(),
                viewSettings,
                mainWindowHeader
        );

        appsController.setOnAppLoaded(() -> {
                enginesController.loadEngines();
                containersController.loadContainers();
        });
        appsController.loadApps();
    }

    public void show() {
        mainWindow.show();
    }

    public void setOnClose(Runnable onClose) {
        this.mainWindow.setOnCloseRequest(event ->{
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle(applicationName);
            alert.setHeaderText(translate("Are you sure you want to close all " + applicationName + " windows?"));
            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                Platform.exit();
            } else {
                event.consume();
            }
            onClose.run();
        });
    }
}