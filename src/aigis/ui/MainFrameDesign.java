package aigis.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.SystemColor;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import org.eclipse.wb.swing.FocusTraversalOnArray;

import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLJPanel;

/***
 * Design of the MainFrame. Don't edit by hand. Use the SwingDesigner.
 */
@SuppressWarnings("serial")
public class MainFrameDesign extends JFrame {

	private JPanel contentPane;
	private final JSplitPane splitPane = new JSplitPane();
	private final ButtonGroup buttonGroupDivision = new ButtonGroup();
	private final ButtonGroup buttonGroupSort = new ButtonGroup();
	private final ButtonGroup buttonGroupProjection = new ButtonGroup();
	private JTextField textCameraLat;
	private JTextField textCameraLng;
	private JTextField textLightLat;
	private JTextField textLightLng;
	private JButton btnCameraMove;
	private JButton btnLightMove;
	private JTable mapTable;
	private GLJPanel panelGL;
	private JMenuItem mntmOpen;
	private JMenuItem mntmOpenNewScene;
	private JTable infoTable;
	private JRadioButtonMenuItem radioDivision1;
	private JRadioButtonMenuItem radioDivision2;
	private JRadioButtonMenuItem radioDivision4;
	private JCheckBoxMenuItem chckbxmntmSyncViews;
	private JMenuItem mntmSyncAllViews;
	private JCheckBoxMenuItem chckbxmntmLatLonGrid;
	private JCheckBoxMenuItem chckbxmntmColorBar;
	private JCheckBoxMenuItem chckbxmntmShading;
	private JMenuItem mntmReset;
	private JMenuItem mntmRescaleRange;
	private JMenuItem mntmSaveSS;
	private JMenuItem mntmOpenImage;
	private JButton btnInfoCopy;
	private JMenuItem mntmAbout;
	private JSeparator separator;
	private JTable texInfoTable;
	private JButton btnAddTex;
	private JCheckBoxMenuItem chckbxmntmFixedLight;
	private JCheckBoxMenuItem chckbxmntmShowAxis;
	private JCheckBoxMenuItem chckbxmntmByName;
	private JCheckBoxMenuItem chckbxmntmByFilename;
	private JMenuItem mntmReload;
	private JMenuItem mntmSettings;
	private JMenu mnLookUp;
	private JLabel lblFov;
	private JTextField textFov;
	private JLabel lblCameraDistance;
	private JTextField textCameraDistance;
	private JButton btnCameraSet;
	private JSeparator separator_1;
	private JMenu mnProjection;
	private JCheckBoxMenuItem chckbxmntmPerspective;
	private JCheckBoxMenuItem chckbxmntmOrthographic;
	private JToolBar toolBarCamera;
	private JLabel lblCamera;
	private Component horizontalGlue_4;
	private Component horizontalGlue_5;
	private JButton btnCameraInfo;
	private Component horizontalStrut_1;
	private JPanel panel3D;
	private JLabel lblNewLabel;
	private JScrollPane general3DScrollPane;
	private JTable general3DTable;
	private JMenu mnAdditional3d;
	private JMenuItem mntmReloadAll;
	private JCheckBoxMenuItem chckbxmntmShowAdditional3d;
	private JLabel lblCameraRoll;
	private JTextField textCameraRoll;
	private JPanel panelToolsModel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrameDesign frame = new MainFrameDesign();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrameDesign() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JMenuBar menuBar = new JMenuBar();
		setJMenuBar(menuBar);

		JMenu mnFileMenu = new JMenu("File");
		menuBar.add(mnFileMenu);

		mntmOpen = new JMenuItem("Open...");
		mnFileMenu.add(mntmOpen);

		mntmOpenNewScene = new JMenuItem("Open in New Scene...");
		mnFileMenu.add(mntmOpenNewScene);

		mntmSaveSS = new JMenuItem("Save Snapshot");
		mnFileMenu.add(mntmSaveSS);

		separator = new JSeparator();
		mnFileMenu.add(separator);

		mntmSettings = new JMenuItem("Settings...");
		mnFileMenu.add(mntmSettings);

		mntmAbout = new JMenuItem("About...");
		mnFileMenu.add(mntmAbout);

		JMenu mnViewMenu = new JMenu("View");
		menuBar.add(mnViewMenu);

		mnProjection = new JMenu("Projection");
		mnViewMenu.add(mnProjection);

		chckbxmntmPerspective = new JCheckBoxMenuItem("Perspective");
		buttonGroupProjection.add(chckbxmntmPerspective);
		chckbxmntmPerspective.setSelected(true);
		mnProjection.add(chckbxmntmPerspective);

		chckbxmntmOrthographic = new JCheckBoxMenuItem("Orthographic");
		buttonGroupProjection.add(chckbxmntmOrthographic);
		mnProjection.add(chckbxmntmOrthographic);

		chckbxmntmLatLonGrid = new JCheckBoxMenuItem("Show Lat-Lon Grid");
		mnViewMenu.add(chckbxmntmLatLonGrid);

		chckbxmntmColorBar = new JCheckBoxMenuItem("Show Color Bar");
		mnViewMenu.add(chckbxmntmColorBar);

		chckbxmntmShowAxis = new JCheckBoxMenuItem("Show Axis");
		mnViewMenu.add(chckbxmntmShowAxis);

		mntmRescaleRange = new JMenuItem("Rescale Color Range...");
		mntmRescaleRange.setEnabled(false);
		mnViewMenu.add(mntmRescaleRange);

		mnLookUp = new JMenu("Look-up Table");
		mnViewMenu.add(mnLookUp);

		chckbxmntmShading = new JCheckBoxMenuItem("Shading");
		mnViewMenu.add(chckbxmntmShading);

		mntmReset = new JMenuItem("Reset View");
		mnViewMenu.add(mntmReset);

		chckbxmntmFixedLight = new JCheckBoxMenuItem("Fixed Light");
		mnViewMenu.add(chckbxmntmFixedLight);

		JMenu mnMultiMenu = new JMenu("Multi-View");
		menuBar.add(mnMultiMenu);

		JMenu mnDivision = new JMenu("Division");
		mnMultiMenu.add(mnDivision);

		radioDivision1 = new JRadioButtonMenuItem("1");
		buttonGroupDivision.add(radioDivision1);
		radioDivision1.setSelected(true);
		mnDivision.add(radioDivision1);

		radioDivision2 = new JRadioButtonMenuItem("2");
		buttonGroupDivision.add(radioDivision2);
		mnDivision.add(radioDivision2);

		radioDivision4 = new JRadioButtonMenuItem("4");
		buttonGroupDivision.add(radioDivision4);
		mnDivision.add(radioDivision4);

		chckbxmntmSyncViews = new JCheckBoxMenuItem("Sync Views");
		chckbxmntmSyncViews.setSelected(true);
		mnMultiMenu.add(chckbxmntmSyncViews);

		mntmSyncAllViews = new JMenuItem("Sync All Views");
		mnMultiMenu.add(mntmSyncAllViews);

		JMenu mnImageMenu = new JMenu("Image");
		menuBar.add(mnImageMenu);

		mntmOpenImage = new JMenuItem("Open Image/Map Image...");
		mnImageMenu.add(mntmOpenImage);

		JMenu mnMapdata = new JMenu("MapData");
		menuBar.add(mnMapdata);

		JMenu mnSort = new JMenu("Sort");
		mnMapdata.add(mnSort);

		chckbxmntmByName = new JCheckBoxMenuItem("By Name");
		buttonGroupSort.add(chckbxmntmByName);
		chckbxmntmByName.setSelected(true);
		mnSort.add(chckbxmntmByName);

		chckbxmntmByFilename = new JCheckBoxMenuItem("By FileName");
		buttonGroupSort.add(chckbxmntmByFilename);
		mnSort.add(chckbxmntmByFilename);

		mntmReload = new JMenuItem("Reload");
		mnMapdata.add(mntmReload);

		mnAdditional3d = new JMenu("3D Object");
		menuBar.add(mnAdditional3d);

		chckbxmntmShowAdditional3d = new JCheckBoxMenuItem("Show Additional 3D Object");
		mnAdditional3d.add(chckbxmntmShowAdditional3d);

		mntmReloadAll = new JMenuItem("ReloadAll");
		mnAdditional3d.add(mntmReloadAll);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		contentPane.add(splitPane, BorderLayout.CENTER);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setForeground(Color.black);
		splitPane.setLeftComponent(tabbedPane);

		JPanel panelTools = new JPanel();
		tabbedPane.add(panelTools);
		tabbedPane.setEnabledAt(0, true);
		tabbedPane.setTitleAt(0, "General");
		panelTools.setLayout(new BoxLayout(panelTools, BoxLayout.Y_AXIS));

		JPanel panelToolsTop = new JPanel();
		panelTools.add(panelToolsTop);
		panelToolsTop.setLayout(new BoxLayout(panelToolsTop, BoxLayout.Y_AXIS));

		toolBarCamera = new JToolBar();
		toolBarCamera.setAlignmentY(0.5f);
		toolBarCamera.setFloatable(false);
		panelToolsTop.add(toolBarCamera);

		horizontalStrut_1 = Box.createHorizontalStrut(20);
		horizontalStrut_1.setPreferredSize(new Dimension(30, 0));
		toolBarCamera.add(horizontalStrut_1);

		horizontalGlue_4 = Box.createHorizontalGlue();
		toolBarCamera.add(horizontalGlue_4);

		lblCamera = new JLabel("Camera");
		toolBarCamera.add(lblCamera);

		horizontalGlue_5 = Box.createHorizontalGlue();
		toolBarCamera.add(horizontalGlue_5);

		btnCameraInfo = new JButton("Info");
		toolBarCamera.add(btnCameraInfo);

		JPanel panelToolsCamera = new JPanel();
		panelToolsTop.add(panelToolsCamera);
		GridBagLayout gbl_panelToolsCamera = new GridBagLayout();
		gbl_panelToolsCamera.columnWeights = new double[] { 0.0, 1.0, 0.0 };
		panelToolsCamera.setLayout(gbl_panelToolsCamera);

		JLabel lblCameraLat = new JLabel("Latitude");
		GridBagConstraints gbc_lblCameraLat = new GridBagConstraints();
		gbc_lblCameraLat.fill = GridBagConstraints.BOTH;
		gbc_lblCameraLat.insets = new Insets(0, 5, 5, 5);
		gbc_lblCameraLat.gridx = 0;
		gbc_lblCameraLat.gridy = 0;
		panelToolsCamera.add(lblCameraLat, gbc_lblCameraLat);

		textCameraLat = new JTextField();
		textCameraLat.setMinimumSize(new Dimension(30, 26));
		GridBagConstraints gbc_textCameraLat = new GridBagConstraints();
		gbc_textCameraLat.weightx = 1.0;
		gbc_textCameraLat.fill = GridBagConstraints.BOTH;
		gbc_textCameraLat.insets = new Insets(0, 0, 5, 5);
		gbc_textCameraLat.gridx = 1;
		gbc_textCameraLat.gridy = 0;
		panelToolsCamera.add(textCameraLat, gbc_textCameraLat);

		btnCameraMove = new JButton("Move");
		GridBagConstraints gbc_btnCameraMove = new GridBagConstraints();
		gbc_btnCameraMove.insets = new Insets(0, 0, 5, 0);
		gbc_btnCameraMove.gridheight = 3;
		gbc_btnCameraMove.fill = GridBagConstraints.BOTH;
		gbc_btnCameraMove.gridx = 2;
		gbc_btnCameraMove.gridy = 0;
		panelToolsCamera.add(btnCameraMove, gbc_btnCameraMove);

		JLabel lblCameraLng = new JLabel("Longitude");
		GridBagConstraints gbc_lblCameraLng = new GridBagConstraints();
		gbc_lblCameraLng.fill = GridBagConstraints.BOTH;
		gbc_lblCameraLng.insets = new Insets(0, 5, 5, 5);
		gbc_lblCameraLng.gridx = 0;
		gbc_lblCameraLng.gridy = 1;
		panelToolsCamera.add(lblCameraLng, gbc_lblCameraLng);

		textCameraLng = new JTextField();
		GridBagConstraints gbc_textCameraLng = new GridBagConstraints();
		gbc_textCameraLng.fill = GridBagConstraints.BOTH;
		gbc_textCameraLng.insets = new Insets(0, 0, 5, 5);
		gbc_textCameraLng.gridx = 1;
		gbc_textCameraLng.gridy = 1;
		panelToolsCamera.add(textCameraLng, gbc_textCameraLng);

		lblCameraRoll = new JLabel("Roll");
		GridBagConstraints gbc_lblCameraRoll = new GridBagConstraints();
		gbc_lblCameraRoll.fill = GridBagConstraints.BOTH;
		gbc_lblCameraRoll.insets = new Insets(0, 5, 5, 5);
		gbc_lblCameraRoll.gridx = 0;
		gbc_lblCameraRoll.gridy = 2;
		panelToolsCamera.add(lblCameraRoll, gbc_lblCameraRoll);

		textCameraRoll = new JTextField();
		GridBagConstraints gbc_textCameraRoll = new GridBagConstraints();
		gbc_textCameraRoll.insets = new Insets(0, 0, 5, 5);
		gbc_textCameraRoll.fill = GridBagConstraints.HORIZONTAL;
		gbc_textCameraRoll.gridx = 1;
		gbc_textCameraRoll.gridy = 2;
		panelToolsCamera.add(textCameraRoll, gbc_textCameraRoll);

		separator_1 = new JSeparator();
		GridBagConstraints gbc_separator_1 = new GridBagConstraints();
		gbc_separator_1.fill = GridBagConstraints.BOTH;
		gbc_separator_1.gridwidth = 3;
		gbc_separator_1.insets = new Insets(0, 15, 5, 15);
		gbc_separator_1.gridx = 0;
		gbc_separator_1.gridy = 3;
		panelToolsCamera.add(separator_1, gbc_separator_1);

		lblCameraDistance = new JLabel("Distance");
		GridBagConstraints gbc_lblCameraDistance = new GridBagConstraints();
		gbc_lblCameraDistance.fill = GridBagConstraints.BOTH;
		gbc_lblCameraDistance.anchor = GridBagConstraints.EAST;
		gbc_lblCameraDistance.insets = new Insets(0, 5, 5, 5);
		gbc_lblCameraDistance.gridx = 0;
		gbc_lblCameraDistance.gridy = 4;
		panelToolsCamera.add(lblCameraDistance, gbc_lblCameraDistance);

		textCameraDistance = new JTextField();
		GridBagConstraints gbc_textCameraDistance = new GridBagConstraints();
		gbc_textCameraDistance.insets = new Insets(0, 0, 5, 5);
		gbc_textCameraDistance.fill = GridBagConstraints.HORIZONTAL;
		gbc_textCameraDistance.gridx = 1;
		gbc_textCameraDistance.gridy = 4;
		panelToolsCamera.add(textCameraDistance, gbc_textCameraDistance);
		textCameraDistance.setColumns(10);

		btnCameraSet = new JButton("Set");
		GridBagConstraints gbc_btnCameraSet = new GridBagConstraints();
		gbc_btnCameraSet.fill = GridBagConstraints.BOTH;
		gbc_btnCameraSet.gridheight = 2;
		gbc_btnCameraSet.gridx = 2;
		gbc_btnCameraSet.gridy = 4;
		panelToolsCamera.add(btnCameraSet, gbc_btnCameraSet);

		lblFov = new JLabel("FOV");
		GridBagConstraints gbc_lblFov = new GridBagConstraints();
		gbc_lblFov.anchor = GridBagConstraints.EAST;
		gbc_lblFov.fill = GridBagConstraints.BOTH;
		gbc_lblFov.insets = new Insets(0, 5, 0, 5);
		gbc_lblFov.gridx = 0;
		gbc_lblFov.gridy = 5;
		panelToolsCamera.add(lblFov, gbc_lblFov);

		textFov = new JTextField();
		GridBagConstraints gbc_textFov = new GridBagConstraints();
		gbc_textFov.insets = new Insets(0, 0, 0, 5);
		gbc_textFov.fill = GridBagConstraints.HORIZONTAL;
		gbc_textFov.gridx = 1;
		gbc_textFov.gridy = 5;
		panelToolsCamera.add(textFov, gbc_textFov);
		textFov.setColumns(10);

		JSeparator separator_2 = new JSeparator();
		panelToolsTop.add(separator_2);

		JLabel lblLight = new JLabel("Light");
		panelToolsTop.add(lblLight);
		lblLight.setAlignmentX(Component.CENTER_ALIGNMENT);

		JPanel panelToolsLight = new JPanel();
		panelToolsTop.add(panelToolsLight);
		GridBagLayout gbl_panelToolsLight = new GridBagLayout();
		gbl_panelToolsLight.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panelToolsLight.rowHeights = new int[] { 0, 0, 0 };
		gbl_panelToolsLight.columnWeights = new double[] { 0.0, 0.0, 0.0, Double.MIN_VALUE };
		gbl_panelToolsLight.rowWeights = new double[] { 0.0, 0.0, Double.MIN_VALUE };
		panelToolsLight.setLayout(gbl_panelToolsLight);

		JLabel lblLightLat = new JLabel("Latitude");
		GridBagConstraints gbc_lblLightLat = new GridBagConstraints();
		gbc_lblLightLat.fill = GridBagConstraints.BOTH;
		gbc_lblLightLat.insets = new Insets(0, 5, 0, 5);
		gbc_lblLightLat.gridx = 0;
		gbc_lblLightLat.gridy = 0;
		panelToolsLight.add(lblLightLat, gbc_lblLightLat);

		textLightLat = new JTextField();
		GridBagConstraints gbc_textLightLat = new GridBagConstraints();
		gbc_textLightLat.weightx = 1.0;
		gbc_textLightLat.fill = GridBagConstraints.BOTH;
		gbc_textLightLat.insets = new Insets(0, 0, 0, 5);
		gbc_textLightLat.gridx = 1;
		gbc_textLightLat.gridy = 0;
		panelToolsLight.add(textLightLat, gbc_textLightLat);

		btnLightMove = new JButton("Move");
		GridBagConstraints gbc_btnLightMove = new GridBagConstraints();
		gbc_btnLightMove.fill = GridBagConstraints.BOTH;
		gbc_btnLightMove.gridheight = 2;
		gbc_btnLightMove.gridx = 2;
		gbc_btnLightMove.gridy = 0;
		panelToolsLight.add(btnLightMove, gbc_btnLightMove);

		JLabel lblLightLng = new JLabel("Longitude");
		GridBagConstraints gbc_lblLightLng = new GridBagConstraints();
		gbc_lblLightLng.fill = GridBagConstraints.BOTH;
		gbc_lblLightLng.insets = new Insets(0, 5, 0, 5);
		gbc_lblLightLng.gridx = 0;
		gbc_lblLightLng.gridy = 1;
		panelToolsLight.add(lblLightLng, gbc_lblLightLng);

		textLightLng = new JTextField();
		GridBagConstraints gbc_textLightLng = new GridBagConstraints();
		gbc_textLightLng.fill = GridBagConstraints.BOTH;
		gbc_textLightLng.insets = new Insets(0, 0, 0, 5);
		gbc_textLightLng.gridx = 1;
		gbc_textLightLng.gridy = 1;
		panelToolsLight.add(textLightLng, gbc_textLightLng);

		JSeparator separator_3 = new JSeparator();
		panelToolsTop.add(separator_3);

		JLabel lblModel = new JLabel("Model");
		panelToolsTop.add(lblModel);
		lblModel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		panelToolsModel = new JPanel();
		panelToolsTop.add(panelToolsModel);
		panelToolsModel.setLayout(new GridLayout(0, 1, 0, 0));

		JSeparator separator_4 = new JSeparator();
		separator_4.setMinimumSize(new Dimension(0, 50));
		panelToolsTop.add(separator_4);

		JToolBar toolBar = new JToolBar();
		toolBar.setAlignmentY(Component.CENTER_ALIGNMENT);
		toolBar.setFloatable(false);
		panelToolsTop.add(toolBar);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(40, 0));
		toolBar.add(horizontalStrut);

		Component horizontalGlue = Box.createHorizontalGlue();
		toolBar.add(horizontalGlue);

		JLabel lblInfo = new JLabel("Info");
		toolBar.add(lblInfo);

		Component horizontalGlue_1 = Box.createHorizontalGlue();
		toolBar.add(horizontalGlue_1);

		btnInfoCopy = new JButton("Copy");
		toolBar.add(btnInfoCopy);

		infoTable = new JTable();
		infoTable.setRowHeight(20);
		infoTable.setEnabled(false);
		infoTable.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		infoTable.setRowSelectionAllowed(false);
		infoTable.setGridColor(Color.LIGHT_GRAY);
		infoTable.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		panelTools.add(infoTable);

		JToolBar toolBar_1 = new JToolBar();
		toolBar_1.setFloatable(false);
		panelTools.add(toolBar_1);

		Component horizontalGlue_2 = Box.createHorizontalGlue();
		toolBar_1.add(horizontalGlue_2);

		JLabel lblMapData = new JLabel("MapData");
		toolBar_1.add(lblMapData);

		Component horizontalGlue_3 = Box.createHorizontalGlue();
		toolBar_1.add(horizontalGlue_3);

		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBackground(SystemColor.window);
		scrollPane.setBorder(new EmptyBorder(0, 0, 0, 0));
		panelTools.add(scrollPane);

		mapTable = new JTable();
		mapTable.setRowHeight(20);
		mapTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		mapTable.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		mapTable.setFont(new Font("Lucida Grande", Font.PLAIN, 12));
		mapTable.setGridColor(Color.LIGHT_GRAY);
		scrollPane.setPreferredSize(new Dimension(280, Short.MAX_VALUE));
		scrollPane.setViewportView(mapTable);

		JPanel panelTexture = new JPanel();
		tabbedPane.addTab("Images", null, panelTexture, null);
		panelTexture.setLayout(new BorderLayout(0, 0));

		JLabel lblTexture = new JLabel("ImageList");
		lblTexture.setHorizontalAlignment(SwingConstants.CENTER);
		panelTexture.add(lblTexture, BorderLayout.NORTH);

		JScrollPane texScrollPane = new JScrollPane();
		panelTexture.add(texScrollPane, BorderLayout.CENTER);

		texInfoTable = new JTable();
		texInfoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		texInfoTable.setGridColor(Color.LIGHT_GRAY);
		texScrollPane.setPreferredSize(new Dimension(280, Short.MAX_VALUE));
		texScrollPane.setViewportView(texInfoTable);

		btnAddTex = new JButton("ADD");
		panelTexture.add(btnAddTex, BorderLayout.SOUTH);
		tabbedPane.setEnabledAt(1, true);

		panel3D = new JPanel();
		tabbedPane.addTab("3D Object", null, panel3D, null);
		panel3D.setLayout(new BorderLayout(0, 0));

		lblNewLabel = new JLabel("3D Object List");
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		panel3D.add(lblNewLabel, BorderLayout.NORTH);

		general3DScrollPane = new JScrollPane();
		panel3D.add(general3DScrollPane, BorderLayout.CENTER);

		general3DTable = new JTable();
		general3DTable.setRowSelectionAllowed(false);
		general3DTable.setGridColor(Color.LIGHT_GRAY);
		general3DScrollPane.setPreferredSize(new Dimension(280, Short.MAX_VALUE));
		general3DScrollPane.setViewportView(general3DTable);

		panelGL = new GLJPanel(new GLCapabilities(GLProfile.get(GLProfile.GL2)));
		panelGL.setBackground(Color.BLACK);
		panelGL.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));

		splitPane.setRightComponent(panelGL);

		setFocusTraversalPolicy(new FocusTraversalOnArray(
				new Component[] { lblCamera, textCameraLat, textCameraLng, textCameraRoll, btnCameraMove,
						textCameraDistance, textFov, btnCameraSet, textLightLat, textLightLng, btnLightMove }));
	}

	protected JButton getBtnCameraMove() {
		return btnCameraMove;
	}

	protected JTextField getTextCameraLat() {
		return textCameraLat;
	}

	protected JTextField getTextCameraLng() {
		return textCameraLng;
	}

	protected JButton getBtnLightMove() {
		return btnLightMove;
	}

	protected JTextField getTextLightLat() {
		return textLightLat;
	}

	protected JTextField getTextLightLng() {
		return textLightLng;
	}

	protected JTable getInfoTable() {
		return infoTable;
	}

	protected GLJPanel getPanelGL() {
		return panelGL;
	}

	protected JMenuItem getMntmOpen() {
		return mntmOpen;
	}

	protected JMenuItem getMntmOpenNewScene() {
		return mntmOpenNewScene;
	}

	protected JTable getMapTable() {
		return mapTable;
	}

	protected JRadioButtonMenuItem getRadioDivision1() {
		return radioDivision1;
	}

	protected JRadioButtonMenuItem getRadioDivision2() {
		return radioDivision2;
	}

	protected JRadioButtonMenuItem getRadioDivision4() {
		return radioDivision4;
	}

	protected JCheckBoxMenuItem getChckbxmntmSyncViews() {
		return chckbxmntmSyncViews;
	}

	protected JMenuItem getMntmSyncAllViews() {
		return mntmSyncAllViews;
	}

	protected JCheckBoxMenuItem getChckbxmntmLatLonGrid() {
		return chckbxmntmLatLonGrid;
	}

	protected JCheckBoxMenuItem getChckbxmntmColorBar() {
		return chckbxmntmColorBar;
	}

	protected JCheckBoxMenuItem getChckbxmntmShading() {
		return chckbxmntmShading;
	}

	protected JMenuItem getMntmReset() {
		return mntmReset;
	}

	protected JMenuItem getMntmRescaleRange() {
		return mntmRescaleRange;
	}

	protected JMenuItem getMntmSaveSS() {
		return mntmSaveSS;
	}

	protected JMenuItem getMntmOpenImage() {
		return mntmOpenImage;
	}

	protected JButton getBtnInfoCopy() {
		return btnInfoCopy;
	}

	protected JMenuItem getMntmAbout() {
		return mntmAbout;
	}

	protected JSeparator getSeparator() {
		return separator;
	}

	protected JTable getTexInfoTable() {
		return texInfoTable;
	}

	protected JButton getBtnAddTex() {
		return btnAddTex;
	}

	protected JCheckBoxMenuItem getChckbxmntmFixedLight() {
		return chckbxmntmFixedLight;
	}

	protected JCheckBoxMenuItem getChckbxmntmShowAxis() {
		return chckbxmntmShowAxis;
	}

	protected JCheckBoxMenuItem getChckbxmntmByName() {
		return chckbxmntmByName;
	}

	protected JCheckBoxMenuItem getChckbxmntmByFilename() {
		return chckbxmntmByFilename;
	}

	protected JMenuItem getMntmReload() {
		return mntmReload;
	}

	protected JMenuItem getMntmSettings() {
		return mntmSettings;
	}

	protected JMenu getMnLookUp() {
		return mnLookUp;
	}

	protected JTextField getTextFov() {
		return textFov;
	}

	protected JTextField getTextCameraDistance() {
		return textCameraDistance;
	}

	protected JButton getBtnCameraSet() {
		return btnCameraSet;
	}

	protected JCheckBoxMenuItem getChckbxmntmPerspective() {
		return chckbxmntmPerspective;
	}

	protected JCheckBoxMenuItem getChckbxmntmOrthographic() {
		return chckbxmntmOrthographic;
	}

	protected JButton getBtnCameraInfo() {
		return btnCameraInfo;
	}

	protected JTable getGeneral3DTable() {
		return general3DTable;
	}

	protected JCheckBoxMenuItem getChckbxmntmShowAdditional3d() {
		return chckbxmntmShowAdditional3d;
	}

	protected JMenuItem getMntmReloadAll() {
		return mntmReloadAll;
	}

	protected JTextField getTextCameraRoll() {
		return textCameraRoll;
	}
	
	protected JPanel getPanelToolsModel() {
		return panelToolsModel;
	}
}
