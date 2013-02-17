import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;

import org.ccnx.ccn.protocol.ContentName;
import org.ccnx.ccn.protocol.MalformedContentNameStringException;

/**
 * FileSyncNDN: Distributed, Dropbox-like File Sharing Service over NDN
 *  
 * @category Distributed File Sharing
 * @author Jared Lindblom
 * @author Huang (John) Ming-Chun
 * @version 1.0
 */
public class FileSyncNDNGui implements WindowListener{
	static JFrame myWindow;
	static JLabel sharedLabel;
	static JLabel topoLabel;
	static JLabel nsLabel;
	static JTextField sharedPath;
	static JTextField topoName;
	static JTextField nsName;
	static JButton sharedButton;
	static JButton topoButton;
	static JButton nsButton;
	static JButton confirmButton;
	static String oldSharedPath;

	public static void main(String[] args){
		myWindow = new JFrame("NDNDriveGUI");
		GroupLayout gl_contentPanel = new GroupLayout(myWindow.getContentPane());
		myWindow.setLayout(gl_contentPanel);
		sharedLabel = new JLabel(" Shared Folder Path");
		topoLabel = new JLabel(" NDN Topology");
		nsLabel = new JLabel(" NDN Namespace");
		sharedPath = new JTextField(32);
		topoName = new JTextField(32);
		nsName = new JTextField(32);
		sharedButton = new JButton("Search");
		sharedButton.addActionListener(new ActionListener() {      		 
			public void actionPerformed(ActionEvent e)
			{
				JFileChooser chooser = new JFileChooser(sharedPath.getText());
				chooser.setDialogTitle("Select a Shared Folder");
				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				chooser.setMultiSelectionEnabled(false);
				int returnVal = chooser.showOpenDialog(myWindow);

				if(returnVal == JFileChooser.APPROVE_OPTION) {
					sharedPath.setText(chooser.getSelectedFile().getAbsolutePath());
				}
			}});  
		topoButton = new JButton("Validate");
		topoButton.addActionListener(new ActionListener() {      		 
			public void actionPerformed(ActionEvent e)
			{
				try {
					ContentName.fromNative(topoName.getText());
				} 
				catch (MalformedContentNameStringException e1) {
					topoName.setText("");
				}
			}});
		nsButton = new JButton("Validate");
		nsButton.addActionListener(new ActionListener() {      		 
			public void actionPerformed(ActionEvent e)
			{
				try {
					ContentName.fromNative(nsName.getText());
				} 
				catch (MalformedContentNameStringException e1) {
					nsName.setText("");
				}
			}});
		confirmButton = new JButton("Confirm");
		confirmButton.addActionListener(new ActionListener() {
			String sharedDirectoryPath = "", topologyString = "", namespaceString = "";
			ContentName topology = null, namespace = null, snapshot = null;

			public void actionPerformed(ActionEvent e)
			{
				boolean success = true;
				sharedDirectoryPath = sharedPath.getText();
				File SharedDir = new File(sharedDirectoryPath);
				if(!SharedDir.exists()){
					System.out.println("No Shared Folder");	
					sharedPath.setText(oldSharedPath);
					success = false;				
				}
				try {
					topologyString = topoName.getText();
					topology = ContentName.fromNative(topologyString);
				} 
				catch (MalformedContentNameStringException e1) {
					topoName.setText("");
					success = false;
				}

				try {
					namespaceString = nsName.getText();
					namespace = ContentName.fromNative(namespaceString);
					snapshot = ContentName.fromNative(namespaceString + "/snapshot");	
				} 
				catch (MalformedContentNameStringException e1) {
					nsName.setText("");
					success = false;
				}

				if(success){
					myWindow.dispose();
					File removeSetup = new File(FileSyncNDN.NDNSETUP);
					if(removeSetup.exists()) {
						removeSetup.delete();
					}
					BufferedWriter writer;
					try {
						writer = new BufferedWriter(new FileWriter(FileSyncNDN.NDNSETUP));
						writer.write(sharedDirectoryPath+'\n');
						writer.write(topologyString+'\n');
						writer.write(namespaceString);
						writer.close();
					} 
					catch (IOException e1) {
						e1.printStackTrace();
					}

					/** Initialize Parameters */		
					Parameters parameters = new Parameters(sharedDirectoryPath, topology, namespace, snapshot);

					MainProgram MainProgram = new MainProgram(parameters);
					MainProgram.RunProgram();
				}
			}});

		gl_contentPanel.setHorizontalGroup(
				gl_contentPanel.createSequentialGroup()
				.addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
						.addComponent(sharedLabel)
						.addComponent(topoLabel)
						.addComponent(nsLabel))
						.addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
								.addComponent(sharedPath)
								.addComponent(topoName)
								.addComponent(nsName))
								.addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.LEADING)
										.addComponent(sharedButton)
										//.addComponent(topoButton)
										//.addComponent(nsButton)
										.addComponent(confirmButton)));
		gl_contentPanel.setVerticalGroup(
				gl_contentPanel.createSequentialGroup()
				.addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.BASELINE)
						.addComponent(sharedLabel)
						.addComponent(sharedPath)
						.addComponent(sharedButton))
						.addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.BASELINE)
								.addComponent(topoLabel)
								.addComponent(topoName))
								//.addComponent(topoButton))
								.addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.BASELINE)
										.addComponent(nsLabel)
										.addComponent(nsName))
										//.addComponent(nsButton))
										.addGroup(gl_contentPanel.createParallelGroup(GroupLayout.Alignment.BASELINE)  		                       
												.addComponent(confirmButton)));
		myWindow.pack();

		File setupFile = new File(FileSyncNDN.NDNSETUP);

		if(setupFile.exists()) {
			try {
				BufferedReader reader = new BufferedReader(new FileReader(FileSyncNDN.NDNSETUP));
				String line = null;
				List<String> fileContents = new ArrayList<String>();
				while((line=reader.readLine())!=null){
					fileContents.add(line);
				}
				reader.close();
				if(fileContents.size() != FileSyncNDN.LIMIT_NUM_OF_CONTENT) {
					setupFile.delete();
				} 
				else {
					oldSharedPath = fileContents.get(0);
					sharedPath.setText(fileContents.get(0));
					topoName.setText(fileContents.get(1));
					nsName.setText(fileContents.get(2));
				}
			} 
			catch (IOException e) {
				e.printStackTrace();
			}
		}
		myWindow.setVisible(true);
	}

	@Override
	public void windowActivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosed(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowClosing(WindowEvent arg0) {
		// TODO Auto-generated method stub
		myWindow.dispose();
		System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowDeiconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowIconified(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void windowOpened(WindowEvent arg0) {
		// TODO Auto-generated method stub

	}
}
