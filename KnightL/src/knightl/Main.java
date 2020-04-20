package knightl;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.KeyboardFocusManager;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Hashtable;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.OverlayLayout;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;


/**
 * The Main class.
 * 
 * @author Sam Hooper
 *
 */
public class Main {
	
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				setupGUI();
			}
		});
	}
	
	private static JFrame frame;
	private static JPanel 	content, board, controls, movementControls, UXbuttons, sidebar,
					boardSizeControls, authorLabelContainer;
	private static JTextField daInput, dbInput;
	private static JLabel daInputText, dbInputText, movementInputMessage, boardSizeSliderHeader, authorLabel;
	private static JButton 	movementApplyButton, clearTextButton, resetKnightButton, enableTilesButton,
					disableTilesButton, boardSizeApplyButton, instructionsButton;
	private static JSlider boardSizeSlider;
	private static JSplitPane split;
	private static JDialog instructionsDialog;
	private static JEditorPane instructionsEditorPane;
	private static JScrollPane instructionsScrollPane;
	private static Font buttonFont = new Font("Century Schoolbook", Font.PLAIN, 14),
				authorFont = new Font("Times New Roman", Font.ITALIC, 14);
	private static int[] knightLoc; //stores x and y coordinates of the knight
	private static Tile[][] boardTiles;
	private static boolean[][] blockedTiles; //stores greyed tiles - true if greyed; false if allowed
	private static Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
	private static final int WIDTH = screenDimension.width;
	private static final int HEIGHT = screenDimension.height;
	private static final int MAX_BOARD_SIZE = 25, MIN_BOARD_SIZE = 1, DEFAULT_BOARD_SIZE = 8;
	private static int boardSize, largestBoardSizeUsed, knightda, knightdb; //knightda and knightdb are the rules for knight movement (default is 1 and 2)
	private static BufferedImage KNIGHT_IMAGE;
	private static ImageIcon currentKnightImage;
	private static ImageIcon[] renderedKnightImages;
	private static boolean currentlyOnPath;
	private static int knightPathIndex;
	private static ArrayList<int[]> knightPath;
	
	private static void setupGUI() {
		largestBoardSizeUsed = boardSize = DEFAULT_BOARD_SIZE;
		knightda = 1;
		knightdb = 2;
		frame = new JFrame("KnightL");
		frame.setPreferredSize(screenDimension);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setResizable(false);
		
		content = new JPanel(new BorderLayout());
		frame.setContentPane(content);
		
		board = new JPanel(new GridLayout(boardSize, boardSize));
		board.setMinimumSize(new Dimension(HEIGHT, HEIGHT));
		board.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {}

			@Override
			public void keyPressed(KeyEvent e) {
				if(currentlyOnPath) {
					int code = e.getKeyCode();
					if(code == KeyEvent.VK_LEFT) {
						if(knightPathIndex > 0) {
							knightPathIndex--;
							int[] newSpot = knightPath.get(knightPathIndex);
							moveKnight(newSpot[0], newSpot[1]);
						}
					}
					else if(code == KeyEvent.VK_RIGHT) {
						if(knightPathIndex < knightPath.size() - 1) {
							knightPathIndex++;
							int[] newSpot = knightPath.get(knightPathIndex);
							moveKnight(newSpot[0], newSpot[1]);
						}
					}
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {}
			
		});
		board.setFocusable(true);
		
		sidebar = new JPanel(new GridBagLayout());
		sidebar.setMinimumSize(new Dimension(WIDTH / 8, HEIGHT));
		sidebar.setBorder(BorderFactory.createEmptyBorder(15, 10, 0, 10));
		controls = new JPanel(new GridBagLayout());
		instructionsButton = new JButton("Show Instructions");
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.weighty = 0.02;
		c.gridx = 0;
		c.gridy = 0;
		sidebar.add(instructionsButton, c);
		c.weightx = 0.5;
		c.weighty = 0.98;
		c.gridx = 0;
		c.gridy = 1;
		sidebar.add(controls, c);
			
		initializeControls();
		initializeInstructions();
		
		split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, sidebar, board);
		content.add(split);
		
		boardTiles = new Tile[MAX_BOARD_SIZE][MAX_BOARD_SIZE];
		for(int x = 0; x < boardSize; x++) {
			for(int y = 0; y < boardSize; y++) {
				board.add(boardTiles[x][y] = new Tile(x,y));
			}
		}
		blockedTiles = new boolean[MAX_BOARD_SIZE][MAX_BOARD_SIZE];
		
		//attempts to load the knight image - if it cannot, an error window appears and the app is unusable.
		try {
			java.net.URL url = Main.class.getResource("/resources/knightpiece.png");
			KNIGHT_IMAGE = ImageIO.read(url);
		} catch (Exception e1) {
			JDialog errorMessage = new JDialog();
			errorMessage.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			errorMessage.setTitle("KnightL");
			errorMessage.setMinimumSize(new Dimension(WIDTH/2, HEIGHT/2));
			errorMessage.getContentPane().add(new JLabel("Could not load knight Image."));
			errorMessage.setVisible(true);
			return;
		}
		
		currentKnightImage = new ImageIcon(getScaledImage(KNIGHT_IMAGE,
			(int) (WIDTH / (boardSize*2.5)), (int) (WIDTH / (boardSize*2.5))));
		renderedKnightImages = new ImageIcon[MAX_BOARD_SIZE + 1];
		renderedKnightImages[boardSize] = currentKnightImage;
		
		boardTiles[0][0].addKnight();
		knightLoc = new int[]{0, 0};
		currentlyOnPath = false;
		knightPathIndex = -1;
		
		frame.pack();
		frame.setVisible(true);
		
	}
	
	private static void initializeInstructions() {
		instructionsButton.setFocusable(false);
		instructionsEditorPane = new JEditorPane();
		instructionsEditorPane.setEditable(false);
		HTMLEditorKit kit = new HTMLEditorKit();
		instructionsEditorPane.setEditorKit(kit);
		InputStream instructionsInputStream = Main.class.getResourceAsStream("instructions.html");
		try {
			byte[] bytes = instructionsInputStream.readAllBytes();
			String text = new String(bytes);
			Document doc = kit.createDefaultDocument();
			instructionsEditorPane.setDocument(doc);
			instructionsEditorPane.setText(text);
		} catch (IOException e1) {
			instructionsEditorPane.setText("Trouble reading instructions.html - IO Exception");
			e1.printStackTrace();
		}
		
		instructionsDialog = new JDialog(frame, "KnightL Instructions");
		instructionsDialog.setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		instructionsScrollPane = new JScrollPane();
		instructionsScrollPane.getViewport().add(instructionsEditorPane);
		instructionsDialog.getContentPane().add(instructionsScrollPane);
		instructionsDialog.setSize(WIDTH/4, HEIGHT/2);
		instructionsButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if(instructionsDialog.isVisible()) {
					instructionsDialog.setVisible(false);
				}
				else {
					instructionsDialog.setVisible(true);
				}
			}
			
		});
		
		instructionsDialog.addWindowListener(new WindowListener() {

			@Override
			public void windowOpened(WindowEvent e) {}
			@Override
			public void windowClosing(WindowEvent e) {}
			@Override
			public void windowClosed(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			
			@Override
			public void windowActivated(WindowEvent e) {
				instructionsButton.setText("Hide Instructions");
			}

			@Override
			public void windowDeactivated(WindowEvent e) {
				instructionsButton.setText("Show Instructions");
			}
			
		});
		
	}

	private static void initializeControls() {
		GridBagConstraints c = new GridBagConstraints();
		movementControls = new JPanel(new GridBagLayout());
		authorLabel = new JLabel("Made by Sam Hooper");
		authorLabel.setFont(authorFont);
		authorLabelContainer = new JPanel(new GridBagLayout());
		UXbuttons = new JPanel(new GridBagLayout());
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.weighty = 0.8;
		c.gridx = 0;
		c.gridy = 0;
		controls.add(movementControls, c);
		c.weighty = 0.2;
		c.gridx = 0;
		c.gridy = 1;
		controls.add(UXbuttons, c);
		c.weighty = 0.01;
		c.gridx = 0;
		c.gridy = 2;
		controls.add(authorLabelContainer, c);
		c.gridx = 0;
		c.gridy = 0;
		c.weightx = 0.5;
		c.weighty = 0.5;
		c.insets = new Insets(0,5,0,5);
		authorLabelContainer.add(authorLabel);
		Font abInputFont = new Font("Courier New", Font.PLAIN, 16);
		daInput = new JTextField("1"); 
		//daInput.setPreferredSize(new Dimension(50, 30));
		daInput.setFocusable(true);
		daInput.setFont(abInputFont);
		daInput.setColumns(2);
		DocumentListener movementInputTextFieldDL = new DocumentListener() {

			@Override
			public void insertUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void removeUpdate(DocumentEvent e) {
				changedUpdate(e);
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				
				movementApplyButton.setEnabled(true);
				movementInputMessage.setText(" "); 
				//use space instead of an empty string so that it still takes up space on GBL
			}
		};
		daInput.getDocument().addDocumentListener(movementInputTextFieldDL);
		dbInput = new JTextField("2");
		dbInput.setFocusable(true);
		dbInput.setFont(abInputFont);
		dbInput.setColumns(2);
		dbInput.getDocument().addDocumentListener(movementInputTextFieldDL);
		Font abInputTextFont = new Font("Century Schoolbook", Font.PLAIN, 16);
		daInputText = new JLabel("A move: ", SwingConstants.RIGHT);
		daInputText.setFont(abInputTextFont);
		dbInputText = new JLabel("B move: ", SwingConstants.RIGHT);
		dbInputText.setFont(abInputTextFont);
		movementApplyButton = new JButton("Apply");
		movementApplyButton.setFont(buttonFont);
		movementApplyButton.setFocusable(false);
		movementApplyButton.setEnabled(false);
		movementApplyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				
				if(isInteger(daInput.getText()) && isInteger(dbInput.getText())) {
					int potentialda = Integer.parseInt(daInput.getText());
					int potentialdb = Integer.parseInt(dbInput.getText());
					if(potentialda < 0 || potentialdb < 0) {
						movementInputMessage.setText("<html>Input values cannot be negative</html>");
					}
					else if(potentialda == 0 && potentialdb == 0) {
						movementInputMessage.setText("<html>Input values cannot both be 0</html>");
					}
					else{
						knightda = potentialda;
						knightdb = potentialdb;
						daInput.setText(String.valueOf(knightda));
						dbInput.setText(String.valueOf(knightdb));
						clearText();
						if(currentlyOnPath)
							moveKnight(knightPath.get(0)[0], knightPath.get(0)[1]);
						currentlyOnPath = false;
						knightPathIndex = -1;
						knightPath = null;
						board.requestFocusInWindow();
					}
				}
				else {
					movementInputMessage.setText("<html>Input must contain only digits (0-9)</html>");
				}
				movementApplyButton.setEnabled(false);
			}
			
		});
		movementInputMessage = new JLabel(" ");
		clearTextButton = new JButton("Clear Text");
		clearTextButton.setFont(buttonFont);
		clearTextButton.setFocusable(false);
		clearTextButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearText();
				if(currentlyOnPath) {
					moveKnight(knightPath.get(0)[0], knightPath.get(0)[1]);
					currentlyOnPath = false;
					knightPathIndex = -1;
					knightPath = null;
				}
			}
			
		});
		resetKnightButton = new JButton("Reset Knight");
		resetKnightButton.setFont(buttonFont);
		resetKnightButton.setFocusable(false);
		resetKnightButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				clearText();
				currentlyOnPath = false;
				knightPathIndex = -1;
				knightPath = null;
				if(boardTiles[0][0].disabled)
					boardTiles[0][0].setEnabled(true);
				moveKnight(0,0);
			}
			
		});
		enableTilesButton = new JButton("<html><center>Enable All Tiles</center></html>");
		enableTilesButton.setFont(buttonFont);
		enableTilesButton.setFocusable(false);
		enableTilesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(int r = 0; r < boardSize; r++) {
					for(int c = 0; c < boardSize; c++) {
						if(blockedTiles[r][c]) {
							blockedTiles[r][c] = false;
							boardTiles[r][c].setEnabled(true);
						}
					}
				}
			}
		});
		disableTilesButton = new JButton("<html><center>Disable All Tiles</center></html>");
		disableTilesButton.setFont(buttonFont);
		disableTilesButton.setFocusable(false);
		disableTilesButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				for(int r = 0; r < boardSize; r++) {
					for(int c = 0; c < boardSize; c++) {
						if(!blockedTiles[r][c] && (knightLoc[0] != r || knightLoc[1] != c)
								&& (!currentlyOnPath || !isOnPath(r,c))) {
							blockedTiles[r][c] = true;
							boardTiles[r][c].setEnabled(false);
						}
					}
				}
			}
		});
		boardSizeControls = new JPanel();
		boardSizeControls.setLayout(new GridBagLayout());
		boardSizeSliderHeader = new JLabel("Board Size - " + DEFAULT_BOARD_SIZE, SwingConstants.CENTER);
		boardSizeSliderHeader.setFont(new Font("Century Schoolbook", Font.PLAIN, 18));
		boardSizeSlider = new JSlider();
		boardSizeSlider.setMajorTickSpacing(5);
		boardSizeSlider.setMinorTickSpacing(1);
		boardSizeSlider.setMaximum(MAX_BOARD_SIZE);
		boardSizeSlider.setMinimum(MIN_BOARD_SIZE);
		boardSizeSlider.setValue(DEFAULT_BOARD_SIZE);
		Hashtable<Integer, JLabel> labelTable = new Hashtable<>();
		labelTable.put(1, new JLabel("1"));
		for(int i = 5; i <= MAX_BOARD_SIZE; i += 5) {
			labelTable.put(i, new JLabel(String.valueOf(i)));
		}
		boardSizeSlider.setLabelTable(labelTable);
		boardSizeSlider.setPaintTicks(true);
		boardSizeSlider.setPaintLabels(true);
		boardSizeSlider.addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				boardSizeSliderHeader.setText("Board Size - " + boardSizeSlider.getValue());
				boardSizeApplyButton.setEnabled(boardSizeSlider.getValue() != boardSize);
			}
			
		});
		boardSizeApplyButton = new JButton("Apply");
		boardSizeApplyButton.setEnabled(false);
		boardSizeApplyButton.setFont(buttonFont);
		boardSizeApplyButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				boardSizeApplyButton.setEnabled(false);
				changeBoardSize(boardSizeSlider.getValue());
			}
			
		});
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(0,5,0,5);
		c.weightx = 0.5;
		//c.weighty = 0.5;
		//c.anchor = GridBagConstraints.CENTER;
		c.gridx = 0;
		c.gridy = 0;
		movementControls.add(daInputText, c);
		c.gridx = 0;
		c.gridy = 1;
		movementControls.add(dbInputText, c);
		c.gridx = 1;
		c.gridy = 0;
		movementControls.add(daInput, c);
		c.gridx = 1;
		c.gridy = 1;
		movementControls.add(dbInput, c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		movementControls.add(movementApplyButton, c);
		c.gridx = 0;
		c.gridy = 3;
		c.gridwidth = 2;
		movementControls.add(movementInputMessage, c);
		c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 0.5;
		c.weighty = 0.5;
		c.insets = new Insets(5,5,5,5);
		c.gridx = 0;
		c.gridy = 0;
		c.gridwidth = 2;
		UXbuttons.add(boardSizeControls, c);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 1;
		UXbuttons.add(enableTilesButton, c);
		c.gridx = 1;
		UXbuttons.add(disableTilesButton, c);
		c.gridx = 0;
		c.gridy = 2;
		c.gridwidth = 2;
		UXbuttons.add(clearTextButton, c);
		c.gridy = 3;
		UXbuttons.add(resetKnightButton, c);
		c = new GridBagConstraints();
		c.gridy = 0; c.gridy = 0; c.weightx = 0.9; c.weighty = 0.5;
		c.fill = GridBagConstraints.BOTH; c.insets = new Insets(0,0,0,5);
		boardSizeControls.add(boardSizeSliderHeader, c);
		c.gridy = 1; c.gridx = 0;
		boardSizeControls.add(boardSizeSlider, c);
		c.fill = GridBagConstraints.HORIZONTAL; c.insets = new Insets(0,5,0,0);
		c.gridx = 1; c.gridy = 0; c.weightx = 0.1; c.gridheight = 2;
		boardSizeControls.add(boardSizeApplyButton, c);
	}
	
	private static void changeBoardSize(int newSize) {
		if(newSize == boardSize) {
			throw new IllegalArgumentException("Attempting to change board size to its current size (this should not happen)");
		}
		clearText();
		boardSize = newSize;
		boardTiles[knightLoc[0]][knightLoc[1]].removeKnight();
		currentlyOnPath = false;
		knightLoc[0] = knightLoc[1] = -1;
		knightPathIndex = -1;
		knightPath = null;
		if(newSize > largestBoardSizeUsed) {
			//populate boardTiles with new Tiles
			for(int r = 0; r < newSize; r++) {
				for(int c = 0; c < newSize; c++) {
					if(boardTiles[r][c] == null) {
						boardTiles[r][c] = new Tile(r,c);
					}
				}
			}
			largestBoardSizeUsed = newSize;
		}
		board.removeAll();
		board.setLayout(new GridLayout(newSize, newSize));
		for(int r = 0; r < newSize; r++) {
			for(int c = 0; c < newSize; c++) {
				blockedTiles[r][c] = false;
				boardTiles[r][c].setEnabled(true);
				board.add(boardTiles[r][c]);
			}
		}
		knightLoc[0] = knightLoc[1] = 0;
		updateKnightImage(newSize);
		board.revalidate();
		board.repaint();
		boardTiles[0][0].addKnight();
		
	}
	
	private static void updateKnightImage(int desiredBoardSize) {
		if(renderedKnightImages[desiredBoardSize] != null) {
			currentKnightImage = renderedKnightImages[desiredBoardSize];
		}
		else {
			currentKnightImage = renderedKnightImages[desiredBoardSize] =
			new ImageIcon(getScaledImage(KNIGHT_IMAGE,
			(int) (WIDTH / (desiredBoardSize*2.5)), (int) (WIDTH / (desiredBoardSize*2.5))));
		}
	}
	
	private static Image getScaledImage(Image srcImg, int w, int h){
	    BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
	    Graphics2D g2 = resizedImg.createGraphics();

	    g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
	    g2.drawImage(srcImg, 0, 0, w, h, null);
	    g2.dispose();

	    return resizedImg;
	}
	
	//Simple pair of method to determine if a string represents a valid integer.
	private static boolean isInteger(String s) {
	    return isInteger(s,10);
	}
	private static boolean isInteger(String s, int radix) {
	    if(s.isEmpty()) return false;
	    for(int i = 0; i < s.length(); i++) {
	        if(i == 0 && s.charAt(i) == '-') {
	            if(s.length() == 1) return false;
	            else continue;
	        }
	        if(Character.digit(s.charAt(i),radix) < 0) return false;
	    }
	    return true;
	}

	private static synchronized void moveKnight(int x, int y) {
		Tile destinationTile = boardTiles[x][y];
		Tile currentTile = boardTiles[knightLoc[0]][knightLoc[1]];
		currentTile.removeKnight();
		knightLoc[0] = x;
		knightLoc[1] = y;
		destinationTile.addKnight();
	}
	
	private static synchronized void moveKnightMiddleClick(int x, int y) {
		clearText();
		currentlyOnPath = false;
		moveKnight(x,y);
	}
	
	private static boolean isOnPath(int x, int y) {
		if(!currentlyOnPath)
			return false;
		for(int[] move : knightPath) {
			if(move[0] == x && move[1] == y)
				return true;
		}
		return false;
	}
	private static synchronized void clearText() {
		for(int r = 0; r < boardSize; r++) {
			for(int c = 0; c < boardSize; c++) {
				boardTiles[r][c].text.setText("");
			}
		}
	}
	@SuppressWarnings("serial")
	private static class Tile extends JPanel {
		
		private static final Color DARK_COLOR = new Color(184, 255, 168);
		private static final Color LIGHT_COLOR = new Color(255, 254, 217);
		private static final Color DARK_DISABLED_COLOR = new Color(69, 69, 69);
		private static final Color LIGHT_DISABLED_COLOR = new Color(112, 112, 112);
		private static final Font textFont = new Font("Courier New Bold", Font.PLAIN, 36);
		private static MouseListener mouseListener = new MouseListener() {

			@Override
			public void mouseClicked(MouseEvent e) {}

			@Override
			public void mousePressed(MouseEvent e) {
				Tile t = (Tile) e.getSource();
				KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
				board.requestFocusInWindow();
				if(SwingUtilities.isLeftMouseButton(e)) {
					Thread thread = new Thread(new Runnable() {
						public synchronized void run() {
							clearText();
							if(currentlyOnPath)
								moveKnight(knightPath.get(0)[0], knightPath.get(0)[1]);
							ArrayList<int[]> movePath = 
								KnightLOnAChessboard.getMovesBFS(knightda, knightdb,
								knightLoc[0], knightLoc[1], t.x, t.y, boardSize, blockedTiles);
							if(movePath == null) {
								currentlyOnPath = false;
								knightPathIndex = -1;
								knightPath = null;
								boardTiles[t.x][t.y].text.setText("X");
							}
							else {
								int index = 0;
								for(int[] move : movePath) {
									boardTiles[move[0]][move[1]].text.setText(String.valueOf(index));
									index++;
								}
								currentlyOnPath = true;
								knightPathIndex = 0;
								knightPath = movePath;
							}
						}
					});
					thread.start();
				}
				else if(SwingUtilities.isMiddleMouseButton(e)) {
					if(!t.disabled)
						moveKnightMiddleClick(t.x,t.y);
				}
				else if(SwingUtilities.isRightMouseButton(e)) {
					if(t.disabled) {
						blockedTiles[t.x][t.y] = false;
						t.setEnabled(true);
					}
					else if((knightLoc[0] != t.x || knightLoc[1] != t.y) &&
							(!currentlyOnPath || !isOnPath(t.x,t.y))) {
						blockedTiles[t.x][t.y] = true;
						t.setEnabled(false);
					}
				}
			}

			@Override
			public void mouseReleased(MouseEvent e) {}

			@Override
			public void mouseEntered(MouseEvent e) {}

			@Override
			public void mouseExited(MouseEvent e) {}
			
		};
		private JLabel img, text;
		private int x, y;
		private boolean disabled;
		
		public Tile(int x, int y) {
			this.x = x;
			this.y = y;
			disabled = false;
			img = null;
			text = new JLabel();
			text.setAlignmentX(0.5f);
			text.setAlignmentY(0.5f);
			text.setFont(textFont);
			text.setVisible(true);
			this.add(text);
			this.setLayout(new OverlayLayout(this));
			if((x+y) % 2 == 0) {
				this.setBackground(LIGHT_COLOR);
			}
			else {
				this.setBackground(DARK_COLOR);
			}
			this.addMouseListener(mouseListener);
		}
		
		public void addKnight() {
			if(img == null) {
				img = new JLabel(currentKnightImage);
				img.setAlignmentX(0.5f);
				img.setAlignmentY(0.5f);
			}
			else {
				img.setIcon(currentKnightImage);
			}
			text.setVisible(false);
			this.add(img);
			this.revalidate();
			this.repaint();
		}
		public void removeKnight() {
			this.remove(img);
			text.setVisible(true);
			this.revalidate();
			this.repaint();
		}
		
		public void setEnabled(boolean newStatus) {
			newStatus = !newStatus;
			if(newStatus != disabled) {
				if(newStatus) {
					if((x+y) % 2 == 0) {
						this.setBackground(LIGHT_DISABLED_COLOR);
					}
					else {
						this.setBackground(DARK_DISABLED_COLOR);
					}
				}
				else {
					if((x+y) % 2 == 0) {
						this.setBackground(LIGHT_COLOR);
					}
					else {
						this.setBackground(DARK_COLOR);
					}
				}
			}
			disabled = newStatus;
		}
		
	}
}
