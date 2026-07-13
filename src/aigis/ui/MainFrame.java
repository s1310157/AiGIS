package aigis.ui;

import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.ButtonGroup;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.JTextArea;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import com.jogamp.opengl.awt.GLJPanel;

import aigis.App;
import aigis.Const;
import aigis.Logger;
import aigis.Scene;
import aigis.SceneManager;
import aigis.gl.Renderer;
import aigis.gl.Textures.Setting;
import aigis.model.CameraInfo;
import aigis.model.ChartData;
import aigis.model.General3D;
import aigis.model.LatLon;
import aigis.model.Model;
import aigis.model.ModelSelection;
import aigis.model.SettingModel.ShapePathData;
import aigis.model.SpectrumMap;
import aigis.model.SpectrumMaps;
import aigis.ui.ButtonCellEditor.CellEventListener;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import aigis.i18n.I18n;
import static aigis.i18n.I18n.t;
import javax.swing.SwingUtilities;
import javax.swing.JEditorPane;
import java.io.Reader;
import java.io.InputStreamReader;

/***
 * MainFrame Controller.
 */
@SuppressWarnings("serial")
public class MainFrame extends MainFrameDesign {

	private SceneManager sceneManager = new SceneManager();
	private Scene scene = sceneManager.getActiveScene();
	private GLSplitWindow window;
	private MapTableModel mapModel;
	private TexTableModel texModel;
	private General3DTableModel g3dModel;
	private SettingDialog settingDialog;
	private TreeMap<String, ChartFrame> chartFrames = new TreeMap<>();
	private final DecimalFormat doubleFormat = new DecimalFormat("#.########");
	private JEditorPane spectrumDescriptionArea;
	private String parentFileName;

    private void refreshTexts() {
        SwingUtilities.updateComponentTreeUI(this);
    }

	private String getParentFileName() {
        return parentFileName;
    }

	public void rebuildUI() {
    SwingUtilities.invokeLater(() -> {
        dispose();
        MainFrame frame = new MainFrame();
		frame.setSize(1400, 1000);
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    });

}

	public MainFrame() {

		JPanel spectrumInfoPanel = getSpectrumInfoPanel();
        this.spectrumDescriptionArea = getSpectrumInfoText();

		// title
		setTitle(Const.APP_NAME);
		// icon
		URL res = App.class.getResource("res/icon.png");
		setIconImage(Toolkit.getDefaultToolkit().getImage(res));
		// exit on close
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		// size
		this.setMinimumSize(new Dimension(320, 320));

		// GUI Components
		JTextField cameraLat = this.getTextCameraLat();
		JTextField cameraLng = this.getTextCameraLng();
		JTextField cameraRoll = this.getTextCameraRoll();
		JTextField cameraDistance = this.getTextCameraDistance();
		JTextField cameraFov = this.getTextFov();
		JTextField lightLat = this.getTextLightLat();
		JTextField lightLng = this.getTextLightLng();
		JTable mapTable = this.getMapTable();


		// Menu Components
		JMenuItem fileOpen = this.getMntmOpen();
		JMenuItem fileOpenNewScene = this.getMntmOpenNewScene();
		JMenuItem fileSaveSS = this.getMntmSaveSS();
		JMenuItem fileAbout = this.getMntmAbout();
		JMenuItem fileSettings = this.getMntmSettings();

		// view menu
		JCheckBoxMenuItem menuSpectrumInfo = this.getMenuSpectrumInfo();
		spectrumInfoPanel.setVisible(menuSpectrumInfo.isSelected());
		menuSpectrumInfo.addActionListener(e -> {
			boolean show = menuSpectrumInfo.isSelected();
            spectrumInfoPanel.setVisible(show);
			spectrumInfoPanel.getParent().revalidate();
			spectrumInfoPanel.getParent().repaint();
		});

		// view
		JCheckBoxMenuItem viewColorbar = this.getChckbxmntmColorBar();
		JCheckBoxMenuItem viewLatLon = this.getChckbxmntmLatLonGrid();
		JCheckBoxMenuItem viewShading = this.getChckbxmntmShading();
		JCheckBoxMenuItem viewFixedLight = this.getChckbxmntmFixedLight();
		JCheckBoxMenuItem viewAxis = this.getChckbxmntmShowAxis();
		JMenuItem viewReset = this.getMntmReset();
		JMenuItem viewRescale = this.getMntmRescaleRange();
		JCheckBoxMenuItem projPerspective = this.getChckbxmntmPerspective();
		JCheckBoxMenuItem projOrthographic = this.getChckbxmntmOrthographic();

		// multi-view
		JMenuItem multiSyncAll = this.getMntmSyncAllViews();
		JCheckBoxMenuItem multiSync = this.getChckbxmntmSyncViews();
		JRadioButtonMenuItem divRadio1 = this.getRadioDivision1();
		JRadioButtonMenuItem divRadio2 = this.getRadioDivision2();
		JRadioButtonMenuItem divRadio4 = this.getRadioDivision4();

		// image
		JMenuItem imageOpen = this.getMntmOpenImage();
		// JCheckBoxMenuItem imageShow = this.getChckbxmntmShowImage();

		// map data
		JMenuItem mapReload = this.getMntmReload();
		JCheckBoxMenuItem mapSortByName = this.getChckbxmntmByName();
		JCheckBoxMenuItem mapSortByFile = this.getChckbxmntmByFilename();

		// addtional3D
		JCheckBoxMenuItem additonal3DShow = this.getChckbxmntmShowAdditional3d();
		JMenuItem additonal3DReload = this.getMntmReloadAll();

		// gl
		GLJPanel glPanel = this.getPanelGL();
		window = new GLSplitWindow(glPanel, sceneManager);

		///// setting events ///

		window.setEventListener(new GLSplitWindow.EventListener() {

			@Override
			public void cameraMoved(CameraInfo camera) {
				LatLon latlon = camera.getLatLng();
				cameraLat.setText(doubleFormat.format(latlon.latitude));
				cameraLng.setText(doubleFormat.format(latlon.longitude));
				cameraRoll.setText(doubleFormat.format(camera.roll));
				cameraDistance.setText(Float.toString(camera.getDistance()));
				cameraFov.setText(doubleFormat.format(camera.getFov()));
			}

			@Override
			public void lightMoved(LatLon latlon) {
				lightLat.setText(doubleFormat.format(latlon.latitude));
				lightLng.setText(doubleFormat.format(latlon.longitude));
			}

			@Override
			public void polygonSelected(int polygonID, LatLon info) {
				updateMapInfo(polygonID, info, null);
			}

			@Override
			public void screenChanged(int index) {
				Renderer rendere = window.getActiveRenderer();
				// switch UI to the scene displayed in the active view
				if (rendere.getScene() != scene) {
					scene = rendere.getScene();
					sceneManager.setActiveScene(scene);
					clearModelList();
					buildModelList();
					String title = scene.getTitle();
					setTitle("AiGIS" + (title == null ? "" : " -" + title + "-"));
					// the texture table shows the textures of the active scene
					if (texModel != null) {
						if (getTexInfoTable().getCellEditor() != null) {
							getTexInfoTable().getCellEditor().stopCellEditing();
						}
						texModel.fireTableDataChanged();
					}
				}
				int polygonID = rendere.getPolygonID();
				LatLon info = rendere.selectPolygon(polygonID, false);
				String rowKey = rendere.getCurrentSpectrumKey();
				updateMapInfo(polygonID, info, rowKey);
				viewColorbar.setSelected(rendere.setting.displayColorBar);
				viewLatLon.setSelected(rendere.setting.displayLatLonGrid);
				viewShading.setSelected(rendere.setting.isShading);
				viewFixedLight.setSelected(rendere.setting.fixedLightPos);
				viewAxis.setSelected(rendere.setting.displayAxis);
				projPerspective.setSelected(rendere.setting.isPerspective);
				projOrthographic.setSelected(!rendere.setting.isPerspective);
				selectModelList(rendere.getCuttentSelection());
				additonal3DShow.setSelected(rendere.setting.displayAdditional3D);
				JMenu lookupMenu = getMnLookUp();
				for (int i = 0; i < lookupMenu.getItemCount(); i++) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem) lookupMenu.getItem(i);
					item.setSelected(i == rendere.setting.colorbarIndex);
				}
			}

		});

		// moving camera
		this.getBtnCameraMove().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Logger.Debug(t("j.movecamera") + ":" + cameraLat.getText() + " - " + cameraLng.getText());
					float lat = Float.parseFloat(cameraLat.getText());
					float lng = Float.parseFloat(cameraLng.getText());
					float roll = Float.parseFloat(cameraRoll.getText());
					window.moveCamera(lat, lng, roll);
					if (viewFixedLight.isSelected()) {
						LatLon latlon = window.getActiveRenderer().getLightLatLon();
						lightLat.setText(doubleFormat.format(latlon.latitude));
						lightLng.setText(doubleFormat.format(latlon.longitude));
					}
					glPanel.repaint();
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(MainFrame.this, t("j.invaliddata"), "",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		this.getBtnCameraSet().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Logger.Debug(t("j.setcamera") + " " + t("j.distance") + cameraDistance.getText() + " fov:" + cameraFov.getText());
					float dist = Float.parseFloat(cameraDistance.getText());
					float fov = Float.parseFloat(cameraFov.getText());
					window.setCameraDistance(dist);
					window.setFov(fov);
					glPanel.repaint();
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(MainFrame.this, t("j.invalid"), t("j.error"), JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// moving light
		this.getBtnLightMove().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Logger.Debug(t("j.movelight") + ":" + lightLat.getText() + " - " + lightLng.getText());
					float lat = Float.parseFloat(lightLat.getText());
					float lng = Float.parseFloat(lightLng.getText());
					window.moveLight(lat, lng);
					glPanel.repaint();
				} catch (Exception e2) {
					JOptionPane.showMessageDialog(MainFrame.this, t("j.invaliddata"), t("j.error"),
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});

		// camera info
		this.getBtnCameraInfo().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Renderer rendere = window.getActiveRenderer();
				CameraInfoDialog dialog = new CameraInfoDialog(MainFrame.this, rendere.getCameraInfo());
				dialog.setLocationRelativeTo(MainFrame.this);
				dialog.setVisible(true);
				if (dialog.canceled) {
					return;
				}
				CameraInfo info = dialog.getCameraInfo();
				rendere.setCameraInfo(info);
				LatLon latlon = info.getLatLng();
				cameraLat.setText(doubleFormat.format(latlon.latitude));
				cameraLng.setText(doubleFormat.format(latlon.longitude));
				cameraDistance.setText(Float.toString(info.getDistance()));
				cameraFov.setText(doubleFormat.format(info.getFov()));
				cameraRoll.setText(doubleFormat.format(info.roll));
				latlon = info.getLightLatLng();
				lightLat.setText(doubleFormat.format(latlon.latitude));
				lightLng.setText(doubleFormat.format(latlon.longitude));
				glPanel.repaint();
			}
		});

		// copy info
		this.getBtnInfoCopy().addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				TableModel info = getInfoTable().getModel();
				String out = t("j.polygonid") + " : " + info.getValueAt(0, 1);
				String item = (String) info.getValueAt(1, 1);
				out += "\n" + t("j.latitude") + " : " + (item.equals("-") ? item : item + "°");
				item = (String) info.getValueAt(2, 1);
				out += "\n" + t("j.longitude") + " : " + (item.equals("-") ? item : item + "°");
				out += "\n" +  t("j.distance") + " : " + info.getValueAt(3, 1);
				out += "\nX : " + info.getValueAt(4, 1);
				out += "\nY : " + info.getValueAt(5, 1);
				out += "\nZ : " + info.getValueAt(6, 1);
				TableModel map = getMapTable().getModel();
				for (int i = 0; i < map.getRowCount(); i++) {
					String title = (String) map.getValueAt(i, 0);
					if (title.equals(Const.SPECTRUMKEY_FLAT)) {
						continue;
					}
					out += "\n" + title + " : " + map.getValueAt(i, 1);
				}
				Logger.Debug(out);
				// copy to clipboard
				Toolkit kit = Toolkit.getDefaultToolkit();
				Clipboard clip = kit.getSystemClipboard();
				StringSelection ss = new StringSelection(out);
				clip.setContents(ss, ss);
			}
		});

		// events for file menu
		ActionListener fileAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == fileOpen) {
					openFile(false, false);
				}
				if (e.getSource() == fileOpenNewScene) {
					openFile(false, true);
				}
				if (e.getSource() == fileSaveSS) {
					saveSS();
				}
				if (e.getSource() == fileAbout) {
					AboutDialog dialog = new AboutDialog(MainFrame.this);
					dialog.setVisible(true);
				}
				if (e.getSource() == fileSettings) {
					if (settingDialog == null) {
						settingDialog = new SettingDialog(MainFrame.this);
					}

					Locale before = I18n.getLocale();
					
					String lookupPath = String.valueOf(App.getProp(Const.LOOKUP_PATH_KEY));
					settingDialog.setVisible(true);

					Locale after = I18n.getLocale();

					if (!before.equals(after)) {
						rebuildUI();
                    }

                    if (!lookupPath.equals(App.getProp(Const.LOOKUP_PATH_KEY))) {
                        loadLookUpTable();
                    }
				}
			}
		};
		fileOpen.addActionListener(fileAction);
		fileOpenNewScene.addActionListener(fileAction);
		fileSaveSS.addActionListener(fileAction);
		fileAbout.addActionListener(fileAction);
		fileSettings.addActionListener(fileAction);

		ActionListener projAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Renderer rendere = window.getActiveRenderer();
				rendere.setting.isPerspective = e.getSource() == projPerspective;
				glPanel.repaint();
			}
		};
		projPerspective.addActionListener(projAction);
		projOrthographic.addActionListener(projAction);

		// events for view menu
		ActionListener viewAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Renderer rendere = window.getActiveRenderer();
				if (e.getSource() == viewColorbar) {
					rendere.setting.displayColorBar = viewColorbar.isSelected();
				}
				if (e.getSource() == viewLatLon) {
					rendere.setting.displayLatLonGrid = viewLatLon.isSelected();
				}
				if (e.getSource() == viewAxis) {
					rendere.setting.displayAxis = viewAxis.isSelected();
				}
				if (e.getSource() == viewFixedLight) {
					rendere.setting.fixedLightPos = viewFixedLight.isSelected();
					getBtnLightMove().doClick();
				}
				if (e.getSource() == viewShading) {
					rendere.changeShading(viewShading.isSelected());
				}
				if (e.getSource() == viewReset) {
					window.resetRenderer(true, scene.getModelSize());
				}
				if (e.getSource() == viewRescale) {
					SpectrumMap spec = rendere.getCurrentSpectrum();
					if (spec == null)
						return;
					setEnabled(false);
					RescaleRangeDialog dialog = new RescaleRangeDialog(MainFrame.this);
					dialog.setEventListener(new RescaleRangeDialog.EventListener() {
						@Override
						public void applay(double max, double min) {
							spec.setCustomRange(max, min);
							glPanel.repaint();
						}

						@Override
						public void close() {
							setEnabled(true);
						}

						@Override
						public void reset() {
							spec.clearCustomRange();
							dialog.setValues(spec.orgMaxColor, spec.orgMinColor);
							glPanel.repaint();
						}
					});
					dialog.setValues(spec.maxColor, spec.minColor);
					dialog.setLocationRelativeTo(MainFrame.this);
					dialog.setVisible(true);
				}
				glPanel.repaint();
			}
		};
		viewColorbar.addActionListener(viewAction);
		viewLatLon.addActionListener(viewAction);
		viewShading.addActionListener(viewAction);
		viewReset.addActionListener(viewAction);
		viewRescale.addActionListener(viewAction);
		viewFixedLight.addActionListener(viewAction);
		viewAxis.addActionListener(viewAction);

		// events for multi-view menu
		ActionListener multiViewAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == multiSyncAll) {
					window.sync();
				}
				if (e.getSource() == multiSync) {
					window.isSync = multiSync.isSelected();
				}
			}
		};
		multiSyncAll.addActionListener(multiViewAction);
		multiSync.addActionListener(multiViewAction);

		// events for image menu
		ActionListener imageAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == imageOpen || e.getSource() == MainFrame.this.getBtnAddTex()) {
					if (!scene.textures.canAddTexture()) {
						String msg = t("j.noadd");
						JOptionPane.showMessageDialog(MainFrame.this, msg, t("j.warning"), JOptionPane.WARNING_MESSAGE);
						return;
					}
					OpenImageDialog dialog = new OpenImageDialog(MainFrame.this, scene.getImageMapPath());
					dialog.setLocationRelativeTo(MainFrame.this);
					dialog.setVisible(true);
					if (dialog.canceled) {
						return;
					}
					try {
						scene.textures.addTexture(dialog.imageFile, dialog.infoFile, dialog.rotateAngle,
								dialog.flipType);
						texModel.fireTableDataChanged();
					} catch (Exception ex) {
						Logger.Error(ex);
						String msg = t("j.noopen") + "\n";
						if (ex.getMessage() != null) {
							msg += "[" + ex.getMessage() + "]";
						}
						JOptionPane.showMessageDialog(MainFrame.this, msg, t("j.error"), JOptionPane.ERROR_MESSAGE);

					}
				}
				glPanel.repaint();
			}
		};
		imageOpen.addActionListener(imageAction);
		// imageShow.addActionListener(imageAction);

		// events for mapdata menu
		ActionListener mapAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reloadMapData();
				glPanel.repaint();
			}
		};
		mapReload.addActionListener(mapAction);
		mapSortByName.addActionListener(mapAction);
		mapSortByFile.addActionListener(mapAction);

		// changing division
		ActionListener divAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (e.getSource() == divRadio1) {
					window.divide(1);
				}
				if (e.getSource() == divRadio2) {
					window.divide(2);
				}
				if (e.getSource() == divRadio4) {
					window.divide(4);
				}
			}
		};
		divRadio1.addActionListener(divAction);
		divRadio2.addActionListener(divAction);
		divRadio4.addActionListener(divAction);

		// events for additonal3D
		ActionListener additonal3DAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Renderer rendere = window.getActiveRenderer();
				if (e.getSource() == additonal3DShow) {
					rendere.setting.displayAdditional3D = additonal3DShow.isSelected();
				}
				if (e.getSource() == additonal3DReload) {
					try {
						scene.loadGeneral3D();
						if (getGeneral3DTable().getCellEditor() != null) {
							getGeneral3DTable().getCellEditor().stopCellEditing();
						}
						g3dModel.fireTableDataChanged();
					} catch (Exception ex) {
						Logger.Error(ex);
						String msg = t("j.noopen") + "\n";
						if (ex.getMessage() != null) {
							msg += "[" + ex.getMessage() + "]";
						}
						JOptionPane.showMessageDialog(MainFrame.this, msg, t("j.error"), JOptionPane.ERROR_MESSAGE);
					}
				}
				glPanel.repaint();
			}
		};
		additonal3DShow.addActionListener(additonal3DAction);
		additonal3DReload.addActionListener(additonal3DAction);

		JTable infoTable = this.getInfoTable();
		// @formatter:off
		String[][] values = { 
				{ t("j.polygonid"), "-" }, 
				{ t("j.latitude"), "-" }, 
				{ t("j.longitude"), "-" }, 
				{ t("j.distance"), "-" },
				{ "X", "-" }, 
				{ "Y", "-" }, 
				{ "Z", "-" } };
		// @formatter:on
		String[] titles = { "", "" };
		infoTable.setModel(new DefaultTableModel(values, titles));

		mapModel = new MapTableModel();
		mapTable.setModel(mapModel);
		ButtonCellEditor mapCellEditor = new ButtonCellEditor(t("j.plot"), new CellEventListener() {

			@Override
			public void actionPerformed(JTable table, int row, int column, boolean check) {
				Renderer rendere = window.getActiveRenderer();
				int polygonID = rendere.getPolygonID();
				if (polygonID < 0) {
					return;
				}
				String title = (String) table.getModel().getValueAt(row, 0);
				ModelSelection selection = rendere.getCuttentSelection();
				String key = sceneManager.indexOf(scene) + ":" + selection.index + "-" + title + "-"
						+ selection.resolution;
				ChartData data = scene.getChartData(title, selection);
				ChartFrame frame;
				if (chartFrames.containsKey(key)) {
					frame = chartFrames.get(key);
				} else {
					frame = new ChartFrame();
					frame.getPanel().setData(data);
					frame.getPanel().setChartInfo(data.mapName, data.xLabel, data.yLabel);
					chartFrames.put(key, frame);
				}
				frame.getPanel().addPolygonID(polygonID);
				frame.setVisible(true);

			}

		}, false);

		String[] mapHeaders = { t("j.name"), t("j.value"), t("j.plot") };
		for (int i = 0; i < mapHeaders.length; i++) {
			TableColumn column = mapTable.getColumnModel().getColumn(i);
			column.setHeaderValue(mapHeaders[i]);
			if (i == 2) {
				column.setMinWidth(40);
				column.setMaxWidth(40);
				column.setCellEditor(mapCellEditor);
				column.setCellRenderer(mapCellEditor);
			}
		}

		ListSelectionModel cellSelectionModel = mapTable.getSelectionModel();
		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {

			@Override
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }

                int row = mapTable.getSelectedRow();
				String key = null;
            
				if(row >= 0) {
                    key = (String) mapTable.getModel().getValueAt(row, 0);
                    window.getActiveRenderer().changeSpectrum(key);
                    viewRescale.setEnabled(row > 0);
                    glPanel.repaint();
				}

				if (spectrumInfoPanel.isVisible()) {
                    try {
                        String lang = I18n.getLocale().getLanguage();
                        String targetKey = key;

                        if (targetKey == null || targetKey.isEmpty() || "#None".equals(targetKey)) {
                            String parentFileName = getParentFileName();

                            if (parentFileName != null && !parentFileName.isEmpty()) {
                                String baseName = parentFileName.replaceFirst("\\.[^.]+$", "");
                                String normalizedBaseName = baseName.toLowerCase(Locale.ROOT);

                                if (normalizedBaseName.startsWith("itokawa")) {
                                    targetKey = "Itokawa";
                                } else if (normalizedBaseName.startsWith("ryugu")) {
                                    targetKey = "Ryugu";
                                } else {
                                    targetKey = parentFileName.replace(".", "_");
                                }
                            }
                        }

                        URL url = null;
                            if (targetKey != null && !targetKey.isEmpty()) {
                            url = App.class.getResource("/aigis/res/spectrum/" + lang + "/" + targetKey + ".html");
                        }

                        if (url != null) {
                           try (Reader reader = new InputStreamReader(url.openStream(),StandardCharsets.UTF_8)) {
                                spectrumDescriptionArea.setContentType("text/html; charset=UTF-8");
                                spectrumDescriptionArea.read(reader, null);
                            }
                        } else {
                                spectrumDescriptionArea.setText("<html><body>" + I18n.t("j.noexplanation") + "</body></html>");
                        }

                    } catch (Exception ex) {
                        Logger.Error(ex);
                        spectrumDescriptionArea.setText("<html><body>" + I18n.t("j.noexplanation") + "</body></html>");
                    }
                }
            }
		});

		// Texture table
		JTable texInfoTable = this.getTexInfoTable();
		texModel = new TexTableModel();
		texInfoTable.setModel(texModel);
		texInfoTable.setRowHeight(24);
		texInfoTable.setRowSelectionAllowed(false);

		texInfoTable.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					TexPrevDialog dialog = new TexPrevDialog(MainFrame.this);
					Point pt = e.getPoint();
					int idx = texInfoTable.rowAtPoint(pt);
					if (idx < 0)
						return;
					int row = texInfoTable.convertRowIndexToModel(idx);
					Setting tex = scene.textures.getTexsInfo().get(row);
					dialog.setTextureSetting(tex);
					dialog.setVisible(true);
				}
			}
		});

		// cell for checkbox
		ButtonCellEditor texCheckCellEditor = new ButtonCellEditor(null, new CellEventListener() {
			@Override
			public void actionPerformed(JTable table, int row, int column, boolean check) {
				if (column == 5) {
					scene.textures.getTexsInfo().get(row).showFrustum = check;
				} else {
					scene.textures.activate(row, column - 1, check);
				}
				glPanel.repaint();
			}
		}, true);

		// cell for change order button
		ButtonCellEditor texUpCellEditor = new ButtonCellEditor("↑", new CellEventListener() {
			@Override
			public void actionPerformed(JTable table, int row, int column, boolean check) {
				scene.textures.upOrder(row);
				texInfoTable.changeSelection(row, 0, true, false);
				texInfoTable.changeSelection(row - 1, 0, true, false);
				glPanel.repaint();
			}
		}, false);

		// cell for remove button
		ButtonCellEditor texDelCellEditor = new ButtonCellEditor("x", new CellEventListener() {
			@Override
			public void actionPerformed(JTable table, int row, int column, boolean check) {
				scene.textures.removeTexture(row);
				glPanel.repaint();
				EventQueue.invokeLater(new Runnable() {
					public void run() {
						texModel.fireTableDataChanged();
					}
				});
			}
		}, false);

		// table settings
		String[] headers = { t("j.name"), "1", "2", "3", "4", "Frus", "", "" };
		for (int i = 0; i < headers.length; i++) {
			TableColumn column = texInfoTable.getColumnModel().getColumn(i);
			column.setHeaderValue(headers[i]);
			if (i == 0)
				continue;
			column.setMinWidth(30);
			column.setMaxWidth(30);
			if (i < 6) {
				column.setCellEditor(texCheckCellEditor);
				column.setCellRenderer(texCheckCellEditor);
			}
			if (i == 6) {
				column.setCellEditor(texUpCellEditor);
				column.setCellRenderer(texUpCellEditor);
			}
			if (i == 7) {
				column.setCellEditor(texDelCellEditor);
				column.setCellRenderer(texDelCellEditor);
			}
		}

		// Add-Texture Button
		this.getBtnAddTex().addActionListener(imageAction);

		ButtonCellEditor genCheckCellEditor = new ButtonCellEditor(null, new CellEventListener() {
			@Override
			public void actionPerformed(JTable table, int row, int column, boolean check) {
				scene.generals.get(row).active = check;
				glPanel.repaint();
			}
		}, true);

		ButtonCellEditor genReloadCellEditor = new ButtonCellEditor(t("j.reload"), new CellEventListener() {
			@Override
			public void actionPerformed(JTable table, int row, int column, boolean check) {
				try {
					String name = scene.generals.get(row).name;
					scene.loadGeneral3D(name);
					if (getGeneral3DTable().getCellEditor() != null) {
						getGeneral3DTable().getCellEditor().stopCellEditing();
					}
					g3dModel.fireTableDataChanged();
					glPanel.repaint();
				} catch (Exception ex) {
					Logger.Error(ex);
					String msg = t("j.noopen") + "\n";
					if (ex.getMessage() != null) {
						msg += "[" + ex.getMessage() + "]";
					}
					JOptionPane.showMessageDialog(MainFrame.this, msg, t("j.error"), JOptionPane.ERROR_MESSAGE);
				}

			}
		}, false);

		// Texture table
		JTable general3DTable = this.getGeneral3DTable();
		g3dModel = new General3DTableModel();
		general3DTable.setModel(g3dModel);
		general3DTable.setRowHeight(24);

		String[] general3DHeaders = { t("j.view"), t("j.filename"), "" };
		for (int i = 0; i < general3DHeaders.length; i++) {
			TableColumn column = general3DTable.getColumnModel().getColumn(i);
			column.setHeaderValue(general3DHeaders[i]);
			if (i == 0) {
				column.setMinWidth(40);
				column.setMaxWidth(40);
				column.setCellEditor(genCheckCellEditor);
				column.setCellRenderer(genCheckCellEditor);
			} else if (i == 1) {
			} else {
				column.setMinWidth(70);
				column.setMaxWidth(70);
				column.setCellEditor(genReloadCellEditor);
				column.setCellRenderer(genReloadCellEditor);
			}
		}

		//
		loadLookUpTable();
	}

	/***
	 * open the file at startup
	 */
	public void openFirst() {
		if (!openFile(true, false)) {
//			System.exit(0);
		}
	}

	/**
	 * Open the file based on the setting property file.
	 * 
	 * @param isStartUp
	 * @param newScene open the folder as a new scene
	 * @return
	 */
	private boolean openFile(boolean isStartUp, boolean newScene) {
		// get path from property
		String defaultDataPath = App.getProp(Const.DATA_PATH_KEY);
		File checkedfile = null;
		if (defaultDataPath != null) {
			checkedfile = new File(defaultDataPath);
		}
		// If there is no directory, it opens from the current directory
		if (checkedfile == null || checkedfile.exists() == false || defaultDataPath.equals("")) {
			// in the case of mac
			File workingDirectory = App.getWorkingDir();
			if (App.isMacExe) {
				workingDirectory = workingDirectory.getParentFile().getParentFile().getParentFile();
			}
			return openDialog(workingDirectory, newScene);
		} else {
			// Whether there is SETTING_TXT in the directory
			File file = new File(defaultDataPath);
			String files[] = file.list();

			if (files != null && Arrays.asList(files).contains(Const.SETTING_TXT)) {
				if (isStartUp) {
					loadFile(file, newScene);
					return true;
				} else {
					String parentPath;
					if (defaultDataPath.equals("/")) {
						// for root path
						parentPath = defaultDataPath;
					} else {
						parentPath = file.getParent();
					}
					File workingDirectory = new File(parentPath);
					return openDialog(workingDirectory, newScene);
				}
			} else {
				return openDialog(file, newScene);
			}
		}
	}

	/***
	 * Capture the screenshot and save it in a png file.
	 */
	private void saveSS() {
		String path = null;
		// get path from property
		String savePngPath = App.getProp(Const.SS_PATH_KEY);
		if (savePngPath != null && !savePngPath.equals("")) {
			File checkedfile = new File(savePngPath);
			if (checkedfile.exists()) {
				path = savePngPath;
			}
		}
		// If there is no directory, it opens from the home directory
		if (path == null || path.isEmpty()) {
			path = System.getProperty("user.home");
		}
		// Save with a time name
		Date date = new Date();
		String DATE_PATTERN = "yyyy-MM-dd'T'HH'.'mm'.'ss";
		String timeStr = new SimpleDateFormat(DATE_PATTERN).format(date);
		timeStr = Const.APP_NAME + "_" + timeStr;
		path = path + File.separator + timeStr + ".png";
		File workingDirectory = new File(path);
		try {
			window.screenshot(workingDirectory);
		} catch (Exception e) {
			Logger.Error(e);
			JOptionPane.showMessageDialog(MainFrame.this, t("j.nosnapshot") + "\n[" + e.getMessage() + "]",
					t("j.error"), JOptionPane.ERROR_MESSAGE);
			return;
		}

		JOptionPane.showMessageDialog(this, t("j.savesnapshot") + "\n'" + path + "'", t("j.completed"),
				JOptionPane.INFORMATION_MESSAGE);

	}

	/**
	 * Show the open dialog.
	 * 
	 * @param dir
	 * @return
	 */
	private boolean openDialog(File dir, boolean newScene) {
		JFileChooser chooser = App.showOpenDialog(this, dir.getAbsolutePath(), JFileChooser.DIRECTORIES_ONLY, null,
				true);
		if (chooser != null) {
			loadFile(chooser.getSelectedFile(), newScene);
			return true;
		}
		return false;
	}

	/**
	 * Update the current polygon info & mapdata.
	 * 
	 * @param polygonID
	 * @param info
	 */
	private void updateMapInfo(int polygonID, LatLon info, String selectedRow) {

		// Info
		JTable infoTable = this.getInfoTable();
		if (polygonID >= 0) {
			infoTable.getModel().setValueAt(Integer.toString(polygonID + 1), 0, 1);
			infoTable.getModel().setValueAt(doubleFormat.format(info.latitude), 1, 1);
			infoTable.getModel().setValueAt(doubleFormat.format(info.longitude), 2, 1);
			infoTable.getModel().setValueAt(Float.toString(info.distance), 3, 1);
			infoTable.getModel().setValueAt(Float.toString(info.xPosition), 4, 1);
			infoTable.getModel().setValueAt(Float.toString(info.yPosition), 5, 1);
			infoTable.getModel().setValueAt(Float.toString(info.zPosition), 6, 1);
		} else {
			for (int i = 0; i < 7; i++) {
				infoTable.getModel().setValueAt("-", i, 1);
			}
		}

		// MapData
		JTable mapTable = this.getMapTable();
		mapModel.update();
		if (selectedRow != null) {
			for (int i = 0; i < mapModel.getRowCount(); i++) {
				if (selectedRow.equals(mapModel.getValueAt(i, 0))) {
					mapTable.setRowSelectionInterval(i, i);
				}
			}
		}
		mapTable.revalidate();
		mapTable.repaint();
	}

	/**
	 * Load lookup-table files.
	 */
	private void loadLookUpTable() {
		try {
			for (Scene s : sceneManager.getScenes()) {
				s.loadLookUpTable();
			}
		} catch (Exception e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			return;
		}
		this.window.resetColorbar();
		// update lookup menu items
		JMenu lookupMenu = getMnLookUp();
		ButtonGroup buttonGroupLookUp = new ButtonGroup();
		ActionListener lookupAction = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Renderer rendere = window.getActiveRenderer();
				for (int i = 0; i < lookupMenu.getItemCount(); i++) {
					JRadioButtonMenuItem item = (JRadioButtonMenuItem) lookupMenu.getItem(i);
					if (e.getSource() == item) {
						rendere.changeColorbar(i);
					}
				}
				getPanelGL().repaint();
			}
		};
		lookupMenu.removeAll();
		for (String color : scene.colorbar.getTitles()) {
			JRadioButtonMenuItem item = new JRadioButtonMenuItem(color);
			item.addActionListener(lookupAction);
			lookupMenu.add(item);
			buttonGroupLookUp.add(item);
		}
		buttonGroupLookUp.getElements().nextElement().setSelected(true);
		
	}

	/**
	 * Load files.
	 *
	 * @param file
	 * @param newScene open the folder as a new scene
	 */
	private void loadFile(File file, boolean newScene) {
		this.parentFileName = file.getName();
		Logger.Debug(file.getAbsolutePath());
		LoadingDialog dialog = new LoadingDialog(this);
		setEnabled(false);

		// show loading dialog
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				if (!newScene) {
					for (ChartFrame frame : chartFrames.values()) {
						frame.setVisible(false);
					}
					chartFrames.clear();
					scene.textures.clearTextures();
				}

				dialog.setLocationRelativeTo(MainFrame.this);
				dialog.setVisible(true);
				// load files in another thread
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							JCheckBoxMenuItem mapSortByName = getChckbxmntmByName();
							if (newScene) {
								// load into a new scene and show it in the active view
								Scene loaded = sceneManager.loadNewScene(file, dialog, mapSortByName.isSelected());
								setTitle("AiGIS");
								clearModelList();
								updateMapInfo(-1, null, null);
								mapModel.clear();
								scene = loaded;
								window.setSceneToActiveView(loaded, loaded.getModelSize());
							} else {
								setTitle("AiGIS");
								clearModelList();
								updateMapInfo(-1, null, null);
								mapModel.clear();
								scene.load(file, dialog, mapSortByName.isSelected());
								window.resetRenderer(true, scene.getModelSize());
							}
							getPanelGL().repaint();
						} catch (Exception e) {
							Logger.Error(e);
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									setEnabled(true);
									dialog.setVisible(false);
									JOptionPane.showMessageDialog(MainFrame.this,
											t("j.noload") + "\n[" + e.getMessage() + "]", t("j.error"),
											JOptionPane.ERROR_MESSAGE);
								}
							});
							return;
						}
						// update UI
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								g3dModel.fireTableDataChanged();
								setEnabled(true);
								String title = scene.getTitle();
								if (title == null) {
									title = "";
								} else {
									title = " -" + title + "-";
								}
								setTitle("AiGIS" + title);
								buildModelList();
								dialog.setVisible(false);
							}
						});
					}
				}).start();
			}
		});
	}

	/**
	 * Reload the map data.
	 */
	private void reloadMapData() {
		LoadingDialog dialog = new LoadingDialog(this);
		setEnabled(false);

		// show loading dialog
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				for (ChartFrame frame : chartFrames.values()) {
					frame.setVisible(false);
				}
				chartFrames.clear();

				dialog.setLocationRelativeTo(MainFrame.this);
				dialog.setVisible(true);
				// load files in another thread
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							JCheckBoxMenuItem mapSortByName = getChckbxmntmByName();
							scene.loadMapData(dialog, mapSortByName.isSelected());
							window.resetRenderer(false, scene.getModelSize());
							getPanelGL().repaint();
						} catch (Exception e) {
							Logger.Error(e);
							EventQueue.invokeLater(new Runnable() {
								public void run() {
									setEnabled(true);
									dialog.setVisible(false);
									JOptionPane.showMessageDialog(MainFrame.this,
											t("j.noload") + "\n[" + e.getMessage() + "]", t("j.error"),
											JOptionPane.ERROR_MESSAGE);
								}
							});
							return;
						}
						// update UI
						EventQueue.invokeLater(new Runnable() {
							public void run() {
								setEnabled(true);
								dialog.setVisible(false);
							}
						});
					}
				}).start();
			}
		});

	}

	private final ButtonGroup buttonGroupModel = new ButtonGroup();
	private final HashMap<ModelSelection, JRadioButton> modelListButtons = new HashMap<ModelSelection, JRadioButton>();

	private void buildRadioButton(ModelSelection sel, String title) {
		JRadioButton button = new JRadioButton(title);
		button.setMinimumSize(new Dimension(30, 23));
		button.setHorizontalAlignment(SwingConstants.LEFT);
		button.setSelected(true);
		buttonGroupModel.add(button);
		getPanelToolsModel().add(button);
		ActionListener action = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				Renderer rendere = window.getActiveRenderer();
				rendere.changeModel(sel);
				getPanelGL().repaint();
				updateMapInfo(-1, null, rendere.getCurrentSpectrumKey());
			}
		};
		button.addActionListener(action);
		modelListButtons.put(sel, button);
	}

	private void clearModelList() {
		getPanelToolsModel().removeAll();
		for (JRadioButton button : modelListButtons.values()) {
			buttonGroupModel.remove(button);
		}
		modelListButtons.clear();
	}

	private void buildModelList() {
		for (ModelSelection sel : scene.getRegisteredModels().getKeys()) {
			ShapePathData path = scene.getShapePath(sel);
			Path fileName = Paths.get(path.path).getFileName();
			buildRadioButton(sel, fileName.toString());
		}
		getPanelToolsModel().updateUI();;
	}

	private void selectModelList(ModelSelection select) {
		JRadioButton button = modelListButtons.get(select);
		if (button != null) {
			buttonGroupModel.clearSelection();
			button.setSelected(true);
		}
	}

	/**
	 * Table model for textures.
	 */
	private class TexTableModel extends AbstractTableModel {

		@Override
		public int getColumnCount() {
			return 8;
		}

		@Override
		public int getRowCount() {
			if (scene.textures == null)
				return 0;
			ArrayList<Setting> texs = scene.textures.getTexsInfo();
			return texs.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			Setting tex = scene.textures.getTexsInfo().get(row);
			if (col == 0) {
				return tex.imageFile.getName();
			} else {
				if (col < 5 && tex.active[col - 1]) {
					return ButtonCellEditor.CHECKED;
				}
				if (col == 5 && tex.showFrustum) {
					return ButtonCellEditor.CHECKED;
				}
				if (row == 0 && col == 6) {
					return null;
				}
				return ButtonCellEditor.NORMAL;
			}
		}

		public boolean isCellEditable(int row, int col) {
			return col > 0;
		}

	}

	/**
	 * Table model for the map info.
	 */
	private class MapTableModel extends AbstractTableModel {
		private SpectrumMaps maps;
		private ArrayList<Map.Entry<String, SpectrumMap>> spectrums;

		public void clear() {
			maps = null;
			spectrums = null;
		}

		public void update() {
			Renderer rendere = window.getActiveRenderer();
			Model model = rendere.getCurrentModel();
			if (model == null)
				return;
			maps = model.getAvailableSpectrumMaps();
			if (maps != null) {
				spectrums = new ArrayList<Map.Entry<String, SpectrumMap>>(maps.getEntrySet());
			}
		}

		public int getRowCount() {
			if (maps == null)
				return 0;
			return maps.getSize();
		}

		public int getColumnCount() {
			return 3;
		}

		public Object getValueAt(int row, int col) {
			SpectrumMap spec = spectrums.get(row).getValue();
			if (col == 0) {
				return spectrums.get(row).getKey();
			}
			if (col == 1) {
				Renderer rendere = window.getActiveRenderer();
				int polygonID = rendere.getPolygonID();
				if (polygonID >= 0 && !spectrums.get(row).getKey().equals(Const.SPECTRUMKEY_FLAT)) {
					float data = spec.getSpectrumData(polygonID);
					return data == Float.NEGATIVE_INFINITY ? "-" : (data + " [" + spec.getUnitRepresentation() + "]");
				}
				return "-";
			}
			if (col == 2) {
				Renderer rendere = window.getActiveRenderer();
				ChartData data = scene.getChartData(spec.getName(), rendere.getCuttentSelection());
				if (data != null) {
					int polygonID = rendere.getPolygonID();
					if (polygonID >= 0 && !spectrums.get(row).getKey().equals(Const.SPECTRUMKEY_FLAT)) {
						return ButtonCellEditor.NORMAL;
					}
					return ButtonCellEditor.DISABLED;
				}
			}
			return "";
		}

		public boolean isCellEditable(int row, int col) {
			return col == 2;
		}
	};

	/**
	 * Table model for general 3D.
	 */
	private class General3DTableModel extends AbstractTableModel {

		@Override
		public int getColumnCount() {
			return 3;
		}

		@Override
		public int getRowCount() {
			return scene.generals == null ? 0 : scene.generals.size();
		}

		@Override
		public Object getValueAt(int row, int col) {
			General3D gen = scene.generals.get(row);
			if (col == 0) {
				if (gen.active) {
					return ButtonCellEditor.CHECKED;
				} else {
					return ButtonCellEditor.NORMAL;
				}
			}
			if (col == 1) {
				return scene.generals.get(row).name;
			}
			return ButtonCellEditor.NORMAL;
		}

		public boolean isCellEditable(int row, int col) {
			return col != 1;
		}
	}
}
