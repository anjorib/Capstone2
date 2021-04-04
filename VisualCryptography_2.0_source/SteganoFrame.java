import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;


public class SteganoFrame extends JFrame implements ActionListener {
	private static final long serialVersionUID = 1L;
	private JPanel pnlAll = new JPanel();
	private JPanel pnlFileFirst = new JPanel();
	private JPanel pnlFileSecond = new JPanel();
	private JPanel pnlFileSecure = new JPanel();
	private JPanel pnlScrolls = new JPanel();
	private JPanel pnlScrollFirstPic = new JPanel();
	private JPanel pnlScrollSecondPic = new JPanel();
	
	private JLabel lblDescr = new JLabel("<html>Add two pictures (png, jpg or gif, will be converted to b/w)  below to encrypt the third one. </html>");
	private JLabel lblFirst = new JLabel(new ImageIcon(), JLabel.CENTER);
	private JLabel lblSecond = new JLabel(new ImageIcon(), JLabel.CENTER);
	private JTextField tfFirst = new JTextField();
	private JTextField tfSecond = new JTextField();
	private JTextField tfSecure = new JTextField();
	private JButton btnSelectFirst = new JButton("Select first image");
	private JButton btnSelectSecond = new JButton("Select second image");
	private JButton btnSelectSecure = new JButton("Select image to hide");
	private JButton btnHide = new JButton("Hide");
	private JButton btnSaveFirst = new JButton("Save first image to file");
	private JButton btnSaveSecond = new JButton("Save second image to file");
	private JScrollPane scrFirst = new JScrollPane(lblFirst);
	private JScrollPane scrSecond = new JScrollPane(lblSecond);
	
	private JFileChooser fileChooser = new JFileChooser();
	private BufferedImage imgFirst = null;
	private BufferedImage imgSecond = null;
	File fFirstFile = null;
	File fSecondFile = null;
	File fSecureFile = null;
	
	public SteganoFrame(JFrame parent) {
		// size
		tfFirst.setMaximumSize(new Dimension(tfFirst.getMaximumSize().width, tfFirst.getPreferredSize().height));
		tfSecond.setMaximumSize(new Dimension(tfSecond.getMaximumSize().width, tfSecond.getPreferredSize().height));
		tfSecure.setMaximumSize(new Dimension(tfSecure.getMaximumSize().width, tfSecure.getPreferredSize().height));
		
		// orientation
		lblDescr.setAlignmentX(LEFT_ALIGNMENT);
		pnlFileFirst.setAlignmentX(LEFT_ALIGNMENT);
		pnlFileSecond.setAlignmentX(LEFT_ALIGNMENT);
		pnlFileSecure.setAlignmentX(LEFT_ALIGNMENT);
		pnlScrolls.setAlignmentX(LEFT_ALIGNMENT);
		
		// action listener
		btnSelectFirst.addActionListener(this);
		btnSelectSecond.addActionListener(this);
		btnSelectSecure.addActionListener(this);
		btnHide.addActionListener(this);
		btnSaveFirst.addActionListener(this);
		btnSaveSecond.addActionListener(this);
		
		tfFirst.setEditable(false);
		tfSecond.setEditable(false);
		tfSecure.setEditable(false);
		btnSaveFirst.setEnabled(false);
		btnSaveSecond.setEnabled(false);
		
		fileChooser.setFileFilter(new FileFilter() {
			public boolean accept(File arg0) {
				if (arg0.isDirectory()) return true;
				if (arg0.getName().toLowerCase().endsWith(".png")) return true;
				if (arg0.getName().toLowerCase().endsWith(".jpg")) return true;
				if (arg0.getName().toLowerCase().endsWith(".gif")) return true;
				return false;
			}

			public String getDescription() {
				return "Image";
			}
		});

		pnlFileFirst.setLayout(new BoxLayout(pnlFileFirst, BoxLayout.X_AXIS));
		pnlFileFirst.add(tfFirst);
		pnlFileFirst.add(Box.createRigidArea(new Dimension(10, 0)));
		pnlFileFirst.add(btnSelectFirst);
		pnlFileSecond.setLayout(new BoxLayout(pnlFileSecond, BoxLayout.X_AXIS));
		pnlFileSecond.add(tfSecond);
		pnlFileSecond.add(Box.createRigidArea(new Dimension(10, 0)));
		pnlFileSecond.add(btnSelectSecond);
		pnlFileSecure.setLayout(new BoxLayout(pnlFileSecure, BoxLayout.X_AXIS));
		pnlFileSecure.add(tfSecure);
		pnlFileSecure.add(Box.createRigidArea(new Dimension(10, 0)));
		pnlFileSecure.add(btnSelectSecure);
		
		pnlScrollFirstPic.setLayout(new BoxLayout(pnlScrollFirstPic, BoxLayout.Y_AXIS));
		pnlScrollFirstPic.add(scrFirst);
		pnlScrollFirstPic.add(Box.createRigidArea(new Dimension(0, 10)));
		pnlScrollFirstPic.add(btnSaveFirst);
		
		pnlScrollSecondPic.setLayout(new BoxLayout(pnlScrollSecondPic, BoxLayout.Y_AXIS));
		pnlScrollSecondPic.add(scrSecond);
		pnlScrollSecondPic.add(Box.createRigidArea(new Dimension(0, 10)));
		pnlScrollSecondPic.add(btnSaveSecond);
		
		pnlScrolls.setLayout(new BoxLayout(pnlScrolls, BoxLayout.X_AXIS));
		pnlScrolls.add(pnlScrollFirstPic);
		pnlScrolls.add(Box.createRigidArea(new Dimension(10, 0)));
		pnlScrolls.add(pnlScrollSecondPic);
		
		btnHide.setEnabled(false);
		
		pnlAll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		pnlAll.setLayout(new BoxLayout(pnlAll, BoxLayout.Y_AXIS));
		pnlAll.add(lblDescr);
		pnlAll.add(pnlFileFirst);
		pnlAll.add(pnlFileSecond);
		pnlAll.add(pnlFileSecure);
		pnlAll.add(btnHide);
		pnlAll.add(Box.createVerticalStrut(10));
		pnlAll.add(pnlScrolls);
		
		add(pnlAll);
		setSize(500, 500);
		setMinimumSize(new Dimension(384, 253));
		setLocationRelativeTo(parent);
		setTitle("Visual Cryptography - Steganography");
		setVisible(true);
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == btnHide) {
			if (fFirstFile == null || !fFirstFile.exists() || fSecondFile == null || !fSecondFile.exists()
					|| fSecureFile == null || !fSecureFile.exists()) {
				JOptionPane.showMessageDialog(this, "File not found", "ERROR", JOptionPane.ERROR_MESSAGE);
				return;
			}
			this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			
			// predominantly load the tree image files just to get their size
			Dimension d = null;
			try {
				d = getBiggestDimension(fFirstFile, fSecondFile, fSecureFile);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			
			// load the three given images
			BufferedImage firstImg = null;
			BufferedImage secondImg = null;
			BufferedImage secureImg = null;
			if (d != null) {
				firstImg = Crypting.loadAndCheckSource(fFirstFile,(int)d.getWidth(), (int)d.getHeight(), true);
				secondImg = Crypting.loadAndCheckSource(fSecondFile,(int)d.getWidth(), (int)d.getHeight(), true);
				secureImg = Crypting.loadAndCheckSource(fSecureFile,(int)d.getWidth(), (int)d.getHeight(), true);
			}
			
			if (firstImg == null || secondImg == null || secureImg == null) {
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				JOptionPane.showMessageDialog(this, "One of the given files is not fit for steganography", "ERROR", JOptionPane.ERROR_MESSAGE);
				return;
			}
			
			BufferedImage images[] = Crypting.hideImage(firstImg, secondImg, secureImg);
			
			imgFirst = images[0];
			imgSecond = images[1];
			
			if (imgFirst == null || imgSecond == null) {
				JOptionPane.showMessageDialog(this, "Error while encrypting (should never happen :( )", "ERROR", JOptionPane.ERROR_MESSAGE);
				this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				return;
			}
			
			lblFirst.setIcon(new ImageIcon(imgFirst));
			lblSecond.setIcon(new ImageIcon(imgSecond));
			
			btnSaveFirst.setEnabled(true);
			btnSaveSecond.setEnabled(true);
			this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
		} else if (e.getSource() == btnSaveFirst) {
			if (imgFirst == null) return;
			fileChooser.setSelectedFile(new File(""));
		    fileChooser.setDialogTitle("Save key as..");
		    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
		    	this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		    	File f = fileChooser.getSelectedFile();
		    	if (!f.toString().endsWith(".png")) {
		    		f = new File(f.toString() + ".png");
		    	}
		    	try {
					ImageIO.write(imgFirst, "png", f);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(this, "Could not Save file because: " + e1.getLocalizedMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
				} finally {
					this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
		    }
		} else if (e.getSource() == btnSaveSecond) {
			if (imgSecond == null) return;
			fileChooser.setSelectedFile(new File(""));
		    fileChooser.setDialogTitle("Save encrypted image as..");
		    this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		    if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
		    	File f = fileChooser.getSelectedFile();
		    	if (!f.toString().endsWith(".png")) {
		    		f = new File(f.toString() + ".png");
		    	}
		    	try {
					ImageIO.write(imgSecond, "png", f);
				} catch (IOException e1) {
					JOptionPane.showMessageDialog(this, "Could not Save file because: " + e1.getLocalizedMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
				} finally {
					this.setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
				}
		    }
		} else if (e.getSource() == btnSelectFirst || e.getSource() == btnSelectSecond || e.getSource() == btnSelectSecure) {
			fileChooser.setDialogTitle("Open image..");
		    if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
		    	if (!fileChooser.getSelectedFile().exists()) return;
		    	//if (!fileChooser.getSelectedFile().getName().endsWith(".png")) return;
		    	if (e.getSource() == btnSelectFirst) {
		    		fFirstFile = fileChooser.getSelectedFile();
		    		tfFirst.setText(fFirstFile.toString());
		    	} else if (e.getSource() == btnSelectSecond) {
		    		fSecondFile = fileChooser.getSelectedFile();
		    		tfSecond.setText(fSecondFile.toString());
		    	} else {
		    		fSecureFile = fileChooser.getSelectedFile();
		    		tfSecure.setText(fSecureFile.toString());
		    	}
		    }
		    
		    if (fFirstFile != null && fSecondFile != null && fSecureFile != null) {
		    	btnHide.setEnabled(true);
		    }
		}
	}
	
	private Dimension getBiggestDimension(File... imageFiles) throws IOException {
		int width = 0;
		int height = 0;
		for (File imageFile : imageFiles) {
			BufferedImage image = ImageIO.read(imageFile);
			int w = image.getWidth();
			int h = image.getHeight();
			width = w > width ? w : width;
			height = h > height ? h : height;
		}
		return new Dimension(width, height);
	}
	
}