package net.mdrjr.usbtool;

import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Font;

public class MainWindow {

	private JFrame frmUsbTool;
	private FileOps fops;
	private static String currentFolder = "/";
	private JLabel lblSelectedFile;
	private JList<String> list_1;
	private String selected;
	private JLabel lblFolderName;
	private String fullFilePath;
	public USBMagicTools umt;
	
	private JPanel panelMain, pMsg;
	private JTextArea txtArea;
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		if(args.length >=1)
			currentFolder = args[0];		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frmUsbTool.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		umt = new USBMagicTools();
		initialize();
	}
	
	/* Java or Javascript.. msgbox here we go! */
	public int msgbox(String msg) {
		txtArea.setText(msg);
		panelMain.setVisible(false);
		pMsg.setVisible(true);
		return 0;
	}
	
	public boolean isDirectory(String name) {
		boolean ret = false;
		if(name != null && name.startsWith("[D]"))
			ret = true;
		return ret;
	}
	
	public String stripInfo(String name) {
		if(name.length() >= 4)
			return name.substring(4);
		else 
			return name;
	}
	
	public void updateList(String folder) {
		List<String> files = fops.getFolderContets(folder);
		list_1.setListData(files.toArray(new String[files.size()]));
		list_1.setSelectedIndex(0);		
	}

	public void handleList(int keyCode) {
		boolean isDir = isDirectory(selected);
		String stripped = stripInfo(selected);
		
		if(keyCode == 10) {
			/* Go back to the previous directory */
			if(selected.equals("[D] ..")) {
				/* Ok, we need to go back to the parent directory! */
				currentFolder = currentFolder.replaceFirst("\\/[^\\/]*$", "");
				updateList(currentFolder);
				lblFolderName.setText(currentFolder);
				return;
			}
			/* is it a directory? */
			if(isDir) {
				currentFolder = currentFolder + "/" + stripped;
				updateList(currentFolder);
				lblFolderName.setText(currentFolder);
				return;
			}
			
			if(!isDir) {
				/* Ok, Not a directory.. a file!
				 * Lets do it!
				 */
				fullFilePath = currentFolder + "/" + stripInfo(selected);
				msgbox("Continue with file: " + fullFilePath);
				
			}
		} 
	}
	
	
	private void initialize_panel_msg() {
		pMsg = new JPanel();
		pMsg.setVisible(false);
		pMsg.setBounds(new Rectangle(0, 0, 320, 240));
		pMsg.setLayout(null);
		
		
		txtArea = new JTextArea();
		txtArea.setFont(new Font("Ubuntu", Font.PLAIN, 12));
		txtArea.setLineWrap(true);
		txtArea.setWrapStyleWord(true);
		
		txtArea.setBounds(1, 1, 318, 208);
		pMsg.add(txtArea);
		
		JButton btnOK = new JButton("OK");
		btnOK.setFont(new Font("Ubuntu", Font.BOLD, 12));
		btnOK.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				umt.unloadModule();
				umt.loadModule(fullFilePath);
				pMsg.setVisible(false);
				panelMain.setVisible(true);
			}
		});
		btnOK.setBounds(0, 215, 163, 25);
		pMsg.add(btnOK);
		
		JButton btnNewButton = new JButton("Cancel");
		btnNewButton.setFont(new Font("Ubuntu", Font.BOLD, 12));
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				pMsg.setVisible(false);
				panelMain.setVisible(true);
			}
		});
		btnNewButton.setBounds(163, 215, 156, 25);
		pMsg.add(btnNewButton);


	}
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		fops = new FileOps();
		List<String> files = fops.getFolderContets(currentFolder);
		
		frmUsbTool = new JFrame();
		frmUsbTool.setTitle("USB Tool");
		frmUsbTool.setBounds(10, 10, 320, 286);
		frmUsbTool.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmUsbTool.getContentPane().setLayout(null);
		frmUsbTool.setResizable(false);
		initialize_panel_msg();
		frmUsbTool.getContentPane().add(pMsg);
		
		panelMain = new JPanel();
		panelMain.setBounds(0, 0, 320, 230);
		frmUsbTool.getContentPane().add(panelMain);
		panelMain.setLayout(null);
		
		lblSelectedFile = new JLabel("");
		lblSelectedFile.setFont(new Font("Ubuntu", Font.PLAIN, 12));
		lblSelectedFile.setBounds(95, 31, 188, 15);
		panelMain.add(lblSelectedFile);
		
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setBounds(12, 49, 296, 179);
		panelMain.add(scrollPane);
		
		list_1 = new JList<String>();
		list_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if(e.getClickCount() >= 2) {
					handleList(10);
				}
			}
		});
		list_1.setFont(new Font("Ubuntu", Font.PLAIN, 12));
		scrollPane.setViewportView(list_1);
		list_1.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				selected = list_1.getSelectedValue();
				lblSelectedFile.setText(selected);
			}
		});
		list_1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		list_1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				handleList(e.getKeyCode());
			}
		});
		
				
		list_1.setModel(new AbstractListModel<String>() {
			private static final long serialVersionUID = -7369268830973798517L;
			String[] values = new String[] {"lero"};
			public int getSize() {
				return values.length;
			}
			public String getElementAt(int index) {
				return values[index];
			}
		});
		list_1.setListData(files.toArray(new String[files.size()]));
		list_1.setSelectedIndex(0);
		
		
		JLabel lblCurrentFolder = new JLabel("Current Folder:");
		lblCurrentFolder.setFont(new Font("Ubuntu", Font.PLAIN, 12));
		lblCurrentFolder.setBounds(12, 12, 96, 15);
		panelMain.add(lblCurrentFolder);
		
		lblFolderName = new JLabel("New label");
		lblFolderName.setFont(new Font("Dialog", Font.PLAIN, 12));
		lblFolderName.setBounds(108, 12, 175, 15);
		lblFolderName.setText(currentFolder);
		panelMain.add(lblFolderName);
		
		JLabel lblNewLabel = new JLabel("Selected File:");
		lblNewLabel.setFont(new Font("Ubuntu", Font.PLAIN, 12));
		lblNewLabel.setBounds(12, 31, 86, 15);
		panelMain.add(lblNewLabel);
		
		JButton btnSelect = new JButton("Select");
		btnSelect.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				handleList(10);
			}
		});
		btnSelect.setBounds(93, 231, 117, 25);
		frmUsbTool.getContentPane().add(btnSelect);
		
		list_1.requestFocus();
	}
}
