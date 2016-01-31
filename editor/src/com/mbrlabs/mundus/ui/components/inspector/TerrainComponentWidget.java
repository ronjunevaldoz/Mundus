/*
 * Copyright (c) 2016. See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mbrlabs.mundus.ui.components.inspector;

import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Align;
import com.kotcrab.vis.ui.VisUI;
import com.kotcrab.vis.ui.util.dialog.DialogUtils;
import com.kotcrab.vis.ui.widget.VisLabel;
import com.kotcrab.vis.ui.widget.VisTable;
import com.kotcrab.vis.ui.widget.VisTextButton;
import com.kotcrab.vis.ui.widget.tabbedpane.Tab;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPane;
import com.kotcrab.vis.ui.widget.tabbedpane.TabbedPaneListener;
import com.mbrlabs.mundus.core.project.ProjectContext;
import com.mbrlabs.mundus.core.project.ProjectManager;
import com.mbrlabs.mundus.terrain.Splatmap;
import com.mbrlabs.mundus.commons.model.MTexture;
import com.mbrlabs.mundus.commons.scene3d.GameObject;
import com.mbrlabs.mundus.commons.scene3d.components.Component;
import com.mbrlabs.mundus.commons.scene3d.components.TerrainComponent;
import com.mbrlabs.mundus.commons.terrain.TerrainTexture;
import com.mbrlabs.mundus.core.Inject;
import com.mbrlabs.mundus.core.Mundus;
import com.mbrlabs.mundus.tools.ToolManager;
import com.mbrlabs.mundus.tools.brushes.TerrainBrush;
import com.mbrlabs.mundus.ui.Ui;
import com.mbrlabs.mundus.ui.components.dialogs.TextureBrowser;
import com.mbrlabs.mundus.ui.widgets.FaTextButton;
import com.mbrlabs.mundus.ui.widgets.TextureGrid;

/**
 * @author Marcus Brummer
 * @version 29-01-2016
 */
public class TerrainComponentWidget extends ComponentWidget<TerrainComponent> implements TabbedPaneListener {

    private TabbedPane tabbedPane;
    private VisTable tabContainer = new VisTable();

    private RaiseLowerTab raiseLowerTab;
    private FlattenTab flattenTab;
    private PaintTab paintTab;
    private SettingsTab settingsTab;

    @Inject
    private ToolManager toolManager;
    @Inject
    private ProjectManager projectManager;
    @Inject
    private ProjectContext projectContext;

    public TerrainComponentWidget(TerrainComponent terrainComponent) {
        super("Terrain Component", terrainComponent);
        Mundus.inject(this);

        tabbedPane = new TabbedPane();
        tabbedPane.addListener(this);

        raiseLowerTab = new RaiseLowerTab();
        flattenTab = new FlattenTab();
        paintTab = new PaintTab();
        settingsTab = new SettingsTab();

        tabbedPane.add(raiseLowerTab);
        tabbedPane.add(flattenTab);
        tabbedPane.add(paintTab);
        tabbedPane.add(settingsTab);

        setupUI();

        tabbedPane.switchTab(0);
    }

    private void setupUI() {
        collapsibleContent.add(tabbedPane.getTable()).padBottom(7).row();
        collapsibleContent.add(tabContainer).expand().fill().row();
    }

    @Override
    public void onDelete() {

    }

    @Override
    public void setValues(GameObject go) {
        Component c = go.findComponentByType(Component.Type.TERRAIN);
        if(c != null) {
            this.component = (TerrainComponent) c;
        }
    }

    @Override
    public void switchedTab(Tab tab) {
        tabContainer.clearChildren();
        tabContainer.add(tab.getContentTable()).expand().fill();
    }

    @Override
    public void removedTab(Tab tab) {
        // no
    }

    @Override
    public void removedAllTabs() {
        // nope
    }

    /**
     * The raise/lower tab contains all brushes used to modify the height of the terrain.
     */
    private class RaiseLowerTab extends Tab {

        private VisTable table;

        public RaiseLowerTab() {
            super(false, false);
            table = new VisTable();
            table.align(Align.left);
            table.add(new BrushTable(TerrainBrush.BrushMode.RAISE_LOWER)).expand().fill().row();
        }

        @Override
        public String getTabTitle() {
            return "Up/Down";
        }

        @Override
        public Table getContentTable() {
            return table;
        }
    }

    /**
     * The flatten tab contains all brushes used to flatten the terrain height.
     */
    private class FlattenTab extends Tab {

        private VisTable table;

        public FlattenTab() {
            super(false, false);
            table = new VisTable();
            table.align(Align.left);
            table.add(new BrushTable(TerrainBrush.BrushMode.FLATTEN)).expand().fill().row();
        }

        @Override
        public String getTabTitle() {
            return "Flatten";
        }

        @Override
        public Table getContentTable() {
            return table;
        }
    }

    /**
     * The paint tab contains everything to paint on the terrain texture.
     */
    private class PaintTab extends Tab {

        private VisTable table;
        private VisTextButton addTextureBtn;
        private TextureGrid textureGrid;

        private TextureBrowser textureBrowser;

        public PaintTab() {
            super(false, false);
            table = new VisTable();
            table.align(Align.left);
            table.add(new BrushTable(TerrainBrush.BrushMode.PAINT)).expand().fill().padBottom(5).row();
            table.addSeparator().height(1);

            textureGrid = new TextureGrid(40, 5);
            textureGrid.setBackground(VisUI.getSkin().getDrawable("menu-bg"));
            table.add(textureGrid).expand().fill().pad(5).row();
            table.addSeparator().height(1);

            addTextureBtn = new VisTextButton("Add Texture");
            table.add(addTextureBtn).right().row();

            textureBrowser = new TextureBrowser();
            textureBrowser.setTextureListener(new TextureGrid.OnTextureClickedListener() {
                @Override
                public void onTextureSelected(MTexture texture) {
                    TerrainTexture terrainTexture = component.getTerrain().getTerrainTexture();
                    int texCount = terrainTexture.countSplatChannelTextures();

                    // set base
                    if(terrainTexture.hasDefaultBaseTexture()) {
                        terrainTexture.setBase(texture);
                        textureGrid.addTexture(texture);
                        // create empty splat map
                        Splatmap sm = new Splatmap(512, 512);
                        terrainTexture.setSplat(sm);
                        textureBrowser.fadeOut();
                        return;
                    }

//                    Splatmap splatmap = new Splatmap(256, 256);
//                    splatmap.drawCircle(40, 40, 20, 0.5f, Splatmap.Channel.R);
//                    splatmap.drawCircle(150, 100, 39, 1f, Splatmap.Channel.R);
//                    splatmap.updateTexture();
//                    splatmap.saveAsPNG(Gdx.files.absolute("/home/marcus/Desktop/splat.png"));
//                    splat.splat = splatmap;

                    // set textures in terrainTexture
                    if(texCount == 0) {
                        terrainTexture.setChanR(texture);
                    } else if(texCount == 1) {
                        terrainTexture.setChanG(texture);
                    } else if(texCount == 2) {
                        terrainTexture.setChanB(texture);
                    } else if(texCount == 3) {
                        terrainTexture.setChanA(texture);
                    } else {
                        DialogUtils.showErrorDialog(Ui.getInstance(), "Not more than 5 textures per terrain please :)");
                        return;
                    }

                    textureBrowser.fadeOut();
                    textureGrid.addTexture(texture);
                }
            });

            addTextureBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    Ui.getInstance().showDialog(textureBrowser);
                }
            });

            textureGrid.setListener(new TextureGrid.OnTextureClickedListener() {
                @Override
                public void onTextureSelected(MTexture texture) {
                    // TODO

                }
            });

            setTextures();
        }

        private void setTextures() {
            TerrainTexture terrainTexture = component.getTerrain().getTerrainTexture();
            if(terrainTexture.getBase().getId() > -1) {
                textureGrid.addTexture(terrainTexture.getBase());
            }
            if(terrainTexture.getChanR() != null) {
                textureGrid.addTexture(terrainTexture.getChanR());
            }
            if(terrainTexture.getChanG() != null) {
                textureGrid.addTexture(terrainTexture.getChanG());
            }
            if(terrainTexture.getBase() != null) {
                textureGrid.addTexture(terrainTexture.getBase());
            }
            if(terrainTexture.getChanA() != null) {
                textureGrid.addTexture(terrainTexture.getChanA());
            }
        }

        @Override
        public String getTabTitle() {
            return "Paint";
        }

        @Override
        public Table getContentTable() {
            return table;
        }
    }

    /**
     * The settings tabs contains all settings of the terrain.
     */
    private class SettingsTab extends Tab {

        private VisTable table;

        public SettingsTab() {
            super(false, false);
            table = new VisTable();
            table.align(Align.left);
            table.add(new VisLabel("Settings"));
        }

        @Override
        public String getTabTitle() {
            return "Settings";
        }

        @Override
        public Table getContentTable() {
            return table;
        }
    }


    /**
     * The Brush table contains all available brushes.
     */
    private class BrushTable extends VisTable {
        private FaTextButton sphereBrushBtn;
        private TerrainBrush.BrushMode brushMode;

        public BrushTable() {
            super();
            align(Align.left);
            sphereBrushBtn = new FaTextButton(toolManager.sphereBrushTool.getIconFont(), FaTextButton.styleBg);
            add(new VisLabel("Brushes:")).padBottom(10).row();
            add(sphereBrushBtn).size(30, 30);

            sphereBrushBtn.addListener(new ClickListener() {
                @Override
                public void clicked(InputEvent event, float x, float y) {
                    activateBrush(toolManager.sphereBrushTool);
                }
            });
        }

        public BrushTable(TerrainBrush.BrushMode mode) {
            this();
            this.brushMode = mode;
        }

        public TerrainBrush.BrushMode getBrushMode() {
            return brushMode;
        }

        public void setBrushMode(TerrainBrush.BrushMode brushMode) {
            this.brushMode = brushMode;
        }

        public void activateBrush(TerrainBrush brush) {
            try {
                brush.setMode(brushMode);
                toolManager.activateTool(brush);
                brush.setTerrain(component.getTerrain());
            } catch (TerrainBrush.ModeNotSupportedException e) {
                e.printStackTrace();
                DialogUtils.showErrorDialog(Ui.getInstance(), e.getMessage());
            }

        }

    }



}